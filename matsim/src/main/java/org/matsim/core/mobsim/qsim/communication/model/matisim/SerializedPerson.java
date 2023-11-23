package org.matsim.core.mobsim.qsim.communication.model.matisim;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.mobsim.qsim.communication.model.serializable.CustomSerializable;
import org.matsim.core.population.PersonImpl;

import java.util.List;
import java.util.stream.Collectors;

public class SerializedPerson implements CustomSerializable<PersonImpl> {

	private String /*Id<Person>*/ id;
	private boolean locked;

	private int selectedPlanIndex; // this should be change to index of below collection
	private List<SerializedPlan> plans;

	public SerializedPerson(PersonImpl person) {
		this.id = person.getId().toString();
		this.locked = person.isLocked();
		this.selectedPlanIndex = person.getPlans().indexOf(person.getSelectedPlan());
		this.plans = person.getPlans().stream().map(SerializedPlan::new).collect(Collectors.toList());
	}

	@Override
	public PersonImpl toRealObject() {
		PersonImpl person = new PersonImpl(Id.createPersonId(id));
		person.setLocked(this.locked);
		List<Plan> plansTmp = this.plans.stream().map(plan -> plan.toRealObject(person)).collect(Collectors.toList());
		person.setPlans(plansTmp);
		person.setSelectedPlan(plansTmp.get(selectedPlanIndex));
		return person;
	}
}
