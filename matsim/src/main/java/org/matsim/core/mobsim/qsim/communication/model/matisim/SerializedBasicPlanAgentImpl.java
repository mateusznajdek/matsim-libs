package org.matsim.core.mobsim.qsim.communication.model.matisim;

import lombok.Getter;
import lombok.Setter;
import org.matsim.api.core.v01.Id;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.qsim.agents.BasicPlanAgentImpl;
import org.matsim.core.mobsim.qsim.communication.model.serializable.CustomMatSimSerializable;
import org.matsim.core.population.PersonImpl;

@Getter
@Setter
public class SerializedBasicPlanAgentImpl implements CustomMatSimSerializable<BasicPlanAgentImpl, World> {

	private int finalActHasDpTimeWrnCnt = 0;
	private int noRouteWrnCnt = 0;

	// private MobsimVehicle vehicle ; // back reference

	private int currentPlanElementIndex = 0;
	private int currentLinkIndex = 0;
	@Getter
	private SerializedPerson serializedPerson;
	private final boolean firstTimeToGetModifiablePlan;
	private double activityEndTime;
	private String state;
	private String /*Id<Link>*/ currentLinkId = null;

	public SerializedBasicPlanAgentImpl(BasicPlanAgentImpl basicPlanAgent) {
		this.finalActHasDpTimeWrnCnt = BasicPlanAgentImpl.finalActHasDpTimeWrnCnt;
		this.noRouteWrnCnt = BasicPlanAgentImpl.noRouteWrnCnt;
		this.currentPlanElementIndex = basicPlanAgent.getCurrentPlanElementIndex();
		this.currentLinkIndex = basicPlanAgent.getCurrentLinkIndex();
		this.firstTimeToGetModifiablePlan = basicPlanAgent.isFirstTimeToGetModifiablePlan();
		this.currentLinkId = basicPlanAgent.getCurrentLinkId().toString();
		this.activityEndTime = basicPlanAgent.getActivityEndTime();
		this.state = basicPlanAgent.getState().toString();
		this.serializedPerson = new SerializedPerson((PersonImpl) basicPlanAgent.getPlan().getPerson());
	}

	@Override
	public BasicPlanAgentImpl toRealObject(World world) {
		BasicPlanAgentImpl basicPlanAgent = new BasicPlanAgentImpl(
			serializedPerson.toRealObject().getSelectedPlan(),
			world.getScenario(),
			world.getEvents(),
			world.getSimTimer(),
			world.getTimeInterpretation()
		);
		BasicPlanAgentImpl.finalActHasDpTimeWrnCnt = finalActHasDpTimeWrnCnt;
		BasicPlanAgentImpl.noRouteWrnCnt = noRouteWrnCnt;
		basicPlanAgent.setFirstTimeToGetModifiablePlan(firstTimeToGetModifiablePlan);
		basicPlanAgent.setCurrentLinkId(Id.createLinkId(currentLinkId));
		MobsimAgent.State stateTmp;
		if (state.equals("LEG"))
			stateTmp = MobsimAgent.State.LEG;
		else if (state.equals("ACTIVITY"))
			stateTmp = MobsimAgent.State.ACTIVITY;
		else
			stateTmp = MobsimAgent.State.ABORT;
		basicPlanAgent.setState(stateTmp);
		basicPlanAgent.setCurrentPlanElementIndex(currentPlanElementIndex);
		basicPlanAgent.setCurrentLinkIndex(currentLinkIndex);
		basicPlanAgent.setActivityEndTime(activityEndTime);
		return basicPlanAgent;
	}
}
