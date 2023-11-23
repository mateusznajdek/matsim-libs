package org.matsim.core.mobsim.qsim.communication.model.matisim;

import com.google.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.mobsim.qsim.interfaces.Netsim;
import org.matsim.core.utils.timing.TimeInterpretation;

import java.util.Map;

@Getter
public class World {
	private final Map<Id<Link>, ? extends Link> links;
	private final Scenario scenario;
	private final EventsManager events;
	@Setter
	private MobsimTimer simTimer;
	@Setter
	private Netsim simulation;
	private final TimeInterpretation timeInterpretation;

	@Inject
	public World(Network network,
				 Scenario scenario,
				 EventsManager events,
				 TimeInterpretation timeInterpretation) {
		this.links = network.getLinks();
		this.scenario = scenario;
		this.events = events;
		this.timeInterpretation = timeInterpretation;
	}
}
