/*
 *  *********************************************************************** *
 *  * project: org.matsim.*
 *  * PlansCalcRouteWithTollOrNot.java
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  * copyright       : (C) 2015 by the members listed in the COPYING, *
 *  *                   LICENSE and WARRANTY file.                            *
 *  * email           : info at matsim dot org                                *
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  *   This program is free software; you can redistribute it and/or modify  *
 *  *   it under the terms of the GNU General Public License as published by  *
 *  *   the Free Software Foundation; either version 2 of the License, or     *
 *  *   (at your option) any later version.                                   *
 *  *   See also COPYING, LICENSE and WARRANTY file                           *
 *  *                                                                         *
 *  * *********************************************************************** 
 */

package org.matsim.contrib.roadpricing;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.population.algorithms.PlanAlgorithm;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.router.PlanRouter;
import org.matsim.core.router.TripRouter;
import org.matsim.core.router.TripStructureUtils;

import javax.inject.Inject;
import javax.inject.Provider;

 class PlansCalcRouteWithTollOrNot implements PlanAlgorithm {

	static final String CAR_WITH_PAYED_AREA_TOLL = "car_with_payed_area_toll";
	private RoadPricingScheme roadPricingScheme;
//	private Provider<TripRouter> tripRouterProvider;
	private final PlanRouter planRouter ;

	@Inject
	PlansCalcRouteWithTollOrNot(RoadPricingScheme roadPricingScheme, Provider<TripRouter> tripRouterProvider) {
		this.roadPricingScheme = roadPricingScheme;
//		this.tripRouterProvider = tripRouterProvider;
		this.planRouter = new PlanRouter( tripRouterProvider.get() ) ;
	}

	@Override
	public void run(final Plan plan) {
		handlePlan(plan);
	}

	private void handlePlan(Plan plan) {
		// This calculates a best-response plan from the two options, paying area toll or not.
		// From what I understand, it may be simpler/better to just throw a coin and produce
		// one of the two options.
		replaceCarModeWithTolledCarMode(plan);
//		PlanRouter untolledPlanRouter = new PlanRouter(tripRouterProvider.get());
//		untolledPlanRouter.run(plan);
		planRouter.run( plan );
		double areaToll = roadPricingScheme.getTypicalCosts().iterator().next().amount;
		double routeCostWithAreaToll = sumNetworkModeCosts(plan) + areaToll;
		replaceTolledCarModeWithCarMode(plan);
//		new PlanRouter(tripRouterProvider.get()).run(plan);
		planRouter.run( plan );
		double routeCostWithoutAreaToll = sumNetworkModeCosts(plan);
		if (routeCostWithAreaToll < routeCostWithoutAreaToll) {
			replaceCarModeWithTolledCarMode(plan);
//			untolledPlanRouter.run(plan);
			planRouter.run( plan );
		}
	}

	private void replaceCarModeWithTolledCarMode(Plan plan) {
		for (PlanElement planElement : plan.getPlanElements()) {
			if (planElement instanceof Leg) {
				if (((Leg) planElement).getMode().equals(TransportMode.car)) {
//					((Leg) planElement).setMode(CAR_WITH_PAYED_AREA_TOLL);
					TripStructureUtils.setRoutingMode( (Leg)planElement , CAR_WITH_PAYED_AREA_TOLL );
				}
			}
		}
//		throw new RuntimeException( "the above is not consistent with routing mode." ) ;
	}

	private void replaceTolledCarModeWithCarMode(Plan plan) {
		for (PlanElement planElement : plan.getPlanElements()) {
			if (planElement instanceof Leg) {
//				if (((Leg) planElement).getMode().equals(CAR_WITH_PAYED_AREA_TOLL)) {
				if (((Leg) planElement).getMode().equals( TransportMode.car )) {
//					((Leg) planElement).setMode("car");
					TripStructureUtils.setRoutingMode( (Leg)planElement , TransportMode.car );
				}
			}
		}
//		throw new RuntimeException( "the above is not consistent with routing mode." ) ;
	}

	private double sumNetworkModeCosts(Plan plan) {
		double sum = 0.0;
		for (PlanElement planElement : plan.getPlanElements()) {
			if (planElement instanceof Leg) {
				Leg leg = (Leg) planElement;
				if (leg.getRoute() instanceof NetworkRoute) {
					NetworkRoute networkRoute = (NetworkRoute) leg.getRoute();
					sum += networkRoute.getTravelCost();
				}
			}
		}
		return sum;
	}

}

