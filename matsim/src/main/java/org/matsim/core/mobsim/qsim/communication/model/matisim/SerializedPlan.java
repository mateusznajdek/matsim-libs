package org.matsim.core.mobsim.qsim.communication.model.matisim;

import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.mobsim.qsim.communication.model.serializable.CustomMatSimSerializable;
import org.matsim.core.population.PlanImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SerializedPlan implements CustomMatSimSerializable<PlanImpl, Person> {
	private double score;
	private List<SerializedPlanElement> actLegs;
	private String type;

	public SerializedPlan(Plan plan) {
		this.score = plan.getScore() == null ? 0 : plan.getScore(); // TODO should not be 0
		this.actLegs = plan.getPlanElements().stream().map(SerializedPlanElement::new).collect(Collectors.toList());
		this.type = plan.getType();
	}

	@Override
	public PlanImpl toRealObject(Person person) {
		PlanImpl plan = new PlanImpl();
		plan.setType(this.type);
		plan.setScore(this.score);
		plan.setActsLegs(new ArrayList<PlanElement>(this.actLegs.stream().map(SerializedPlanElement::toRealObject).collect(Collectors.toList())));
		plan.setPerson(person);
		return plan;
	}
}
