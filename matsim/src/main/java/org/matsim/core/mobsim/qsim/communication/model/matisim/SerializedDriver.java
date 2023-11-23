package org.matsim.core.mobsim.qsim.communication.model.matisim;

import org.matsim.core.mobsim.qsim.agents.PersonDriverAgentImpl;
import org.matsim.core.mobsim.qsim.communication.model.serializable.CustomMatSimSerializable;

public class SerializedDriver implements CustomMatSimSerializable<PersonDriverAgentImpl, World> {

	private SerializedBasicPlanAgentImpl serializedBasicPlanAgent;
	// This seems to be only cache, so skip
//	private String cachedNextLinkId; // PlanBasedDriverAgentImpl


	public SerializedDriver(PersonDriverAgentImpl personDriverAgent) {
		this.serializedBasicPlanAgent = new SerializedBasicPlanAgentImpl(personDriverAgent.getBasicAgentDelegate());
//		this.cachedNextLinkId = personDriverAgent.getDriverAgentDelegate().getCachedNextLinkId() != null ?
//			personDriverAgent.getDriverAgentDelegate().getCachedNextLinkId().toString() : null;
	}

	@Override
	public PersonDriverAgentImpl toRealObject(World world) {
		return new PersonDriverAgentImpl(
			serializedBasicPlanAgent.getSerializedPerson().toRealObject().getSelectedPlan(),
			world.getSimulation(),
			world.getTimeInterpretation()
		);
	}
}
