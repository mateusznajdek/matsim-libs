/* *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2020 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.contrib.parking.parkingproxy;

import java.util.Collection;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.parking.parkingproxy.AccessEgressFinder.LegActPair;
import org.matsim.core.controler.events.AfterMobsimEvent;
import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.listener.AfterMobsimListener;
import org.matsim.core.controler.listener.BeforeMobsimListener;

/**
 * <p>
 * This class allows to implicitly apply penalties to plans by changing the duration of the egress walk after car interactions.
 * By construction this only works for plans involving car trips and can not be used if you want to penalize all plans that
 * fulfill a certain condition.
 * </p>
 * <p>
 * More precisely, the class will... </br> 
 * 1. before the mobsim starts alter all egress walk durations depending on the spatial and temporal gridcell the walk starts in
 * and adapt the start time of the following activity accordingly. Since the egress walk is a teleported mode this translates 1:1
 * into a shorter activity time. The amount of penalty is defined by {@linkplain PenaltyCalculator} generated by the provided
 * {@linkplain PenaltyGenerator}. The generator gets reset after it provided its calculator.</br>
 * 2. after the mobsim ends reverse the changes made in 1 so the evolutionary nature of MATSim is preserved.</br>
 * 3. do nothing during the scoring. Since the experienced plans are rated, the shortened activity durations resulting from the
 * longer egress walks already penalize the plan.
 * </p>
 * 
 * @author tkohl / Senozon
 *
 */
class CarEgressWalkChanger implements BeforeMobsimListener, AfterMobsimListener {
	
	private final CarEgressWalkObserver observer;
	private final AccessEgressFinder egressFinder = new AccessEgressFinder(TransportMode.car);
	
	/**
	 * Sets the class up with the {@linkplain PenaltyCalculator.DefaultPenaltyFunction} and the specified {@linkplain PenaltyGenerator}.
	 * 
	 * @param penaltyGenerator
	 */
	public CarEgressWalkChanger(PenaltyGenerator penaltyGenerator) {
		this(penaltyGenerator, new PenaltyCalculator.DefaultPenaltyFunction());
	}
	
	/**
	 * Sets the class up with the specified {@linkplain PenaltyGenerator} and {@linkplain PenaltyFunction}.
	 * 
	 * @param penaltyGenerator
	 * @param penaltyFunction
	 */
	public CarEgressWalkChanger(PenaltyGenerator penaltyGenerator, PenaltyFunction penaltyFunction) {
		this.observer = new CarEgressWalkObserver(penaltyGenerator, penaltyFunction);
	}

	/**
	 * gets a new {@linkplain PenaltyCalculator} and prolongs egress times
	 */
	@Override
	public void notifyBeforeMobsim(BeforeMobsimEvent event) {
		// first we need to update the Penalties to the result of the last iteration
		this.observer.notifyBeforeMobsim(event);
		
		// then we alter the egressWalk times according to the penalty
		this.changeEgressTimes(event.getServices().getScenario().getPopulation().getPersons().values(), false);
	}
	
	/**
	 * resets egress times before scoring / replanning
	 */
	@Override
	public void notifyAfterMobsim(AfterMobsimEvent event) {
		// we need to roll back the changes we made before the mobsim, otherwise we can't apply
		// a different penalty next iteration.
		this.changeEgressTimes(event.getServices().getScenario().getPopulation().getPersons().values(), true);
		// yyyy this is something we do not like: to just "fake" something and take it back afterwards.  Would be good to find some other design
		// eventually.  Not so obvious, though ...   kai, mar'20
	}

	/**
	 * Changes the egress times of all agents using cars according to the penalty for the corresponding space-time-gridcell.
	 * 
	 * @param population All persons with their plan(s)
	 * @param reverse if {@code false}, the egress times get prolonged by the penalty time, if {@code true}, they get shortened
	 * by that time (calling this method twice, first with {@code false}, then with {@code true} should yield the original plans)
	 */
	private void changeEgressTimes(Collection<? extends Person> population, boolean reverse) {
		int sign = reverse ? -1 : 1;
		for (Person p : population) {
			for (LegActPair walkActPair : this.egressFinder.findEgressWalks(p.getSelectedPlan())) {
				double penalty = sign * this.observer.getPenaltyCalculator().getPenalty(
						walkActPair.leg.getDepartureTime().seconds(), walkActPair.act.getCoord());
				walkActPair.leg.setTravelTime(walkActPair.leg.getTravelTime().seconds() + penalty);
				walkActPair.leg.getRoute().setTravelTime(walkActPair.leg.getRoute().getTravelTime().seconds() + penalty);
				walkActPair.act.setStartTime(walkActPair.act.getStartTime().seconds() + penalty);
			}
		}
	}

}
