package org.matsim.core.mobsim.qsim.communication.model.matisim;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.mobsim.qsim.communication.model.serializable.CustomSerializable;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.LegImpl;

public class SerializedPlanElement implements CustomSerializable<PlanElement> {

	private String implementationType; // Activity vs Leg
	// LEG:
	private SerializedRoute route;
	private double leg_depTime;
	private double leg_travTime;
	private String leg_mode;
	private String leg_routingMode;

	// ACTIVITY:

	private double activ_endTime;
	private double activ_startTime;
	private double activ_dur;
	private String activ_type;
	private SerializedCoord activ_coord;
	private String /*Id<Link>*/ activ_linkId;
//	private String /*Id<ActivityFacility>*/ activ_activityId;

	public SerializedPlanElement(PlanElement plan) {
		if (plan instanceof ActivityImpl) {
			this.implementationType = "ACTIVITY";
			this.activ_startTime = ((ActivityImpl) plan).getStartTime().orElse(0); // TODO
			this.activ_endTime = ((ActivityImpl) plan).getEndTime().orElse(0); // TODO
			this.activ_dur = ((ActivityImpl) plan).getDur();
			this.activ_type = ((ActivityImpl) plan).getType();
			this.activ_coord = new SerializedCoord(((ActivityImpl) plan).getCoord());
			this.activ_linkId = ((ActivityImpl) plan).getLinkId().toString();
//			this.activ_activityId = ((ActivityImpl) plan).getFacilityId() != null ? ((ActivityImpl) plan).getFacilityId().toString() : "";
		} else if (plan instanceof LegImpl) {
			this.implementationType = "LEG";
			this.route = new SerializedRoute(((LegImpl) plan).getRoute());
			this.leg_depTime = ((LegImpl) plan).getDepTime();
			this.leg_travTime = ((LegImpl) plan).getTravTime();
			this.leg_mode = ((LegImpl) plan).getMode();
			this.leg_routingMode = ((LegImpl) plan).getRoutingMode();

		} else {
			System.err.println("Whole world is burning because PlanElement is neither:" +
				" ActivityImpl nor LegImpl");
			throw new RuntimeException("It has break along the way....");
		}

	}

	@Override
	public PlanElement toRealObject() {
		if (this.implementationType.equals("LEG")) {
			LegImpl leg = new LegImpl(this.leg_mode);
			leg.setRoutingMode(this.leg_routingMode);
			leg.setDepartureTime(this.leg_depTime);
			leg.setTravelTime(this.leg_travTime);
			leg.setRoute(this.route.toRealObject());
			return leg;
		} else if (this.implementationType.equals("ACTIVITY")) {
			ActivityImpl activity = new ActivityImpl(this.activ_type);
			activity.setStartTime(this.activ_startTime);
			activity.setEndTime(this.activ_endTime);
			activity.setDur(this.activ_dur);
			activity.setCoord(this.activ_coord.toRealObject());
			activity.setLinkId(Id.createLinkId(this.activ_linkId));
			return activity;
		} else {
			System.err.println("Whole world is burning because element is neither:" +
				" Activity nor Leg");
			throw new RuntimeException("It has break along the way....");
		}
	}
}
