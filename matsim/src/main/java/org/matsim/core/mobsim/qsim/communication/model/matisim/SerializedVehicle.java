package org.matsim.core.mobsim.qsim.communication.model.matisim;

import org.matsim.api.core.v01.Id;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.mobsim.qsim.communication.model.serializable.CustomSerializable;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleImpl;
import org.matsim.vehicles.VehicleUtils;

public class SerializedVehicle implements CustomSerializable<VehicleImpl> {
	//	NOTE: NOT USED - can be poplated with default value
	//	private VehicleType type;
	private final String /**Id<Vehicle>**/ id;

//	NOTE: THIS I don't know if necessary
//	private Attributes attributes;

	public SerializedVehicle(Vehicle vehicle) {
		this.id = vehicle.getId().toString();
	}


	@Override
	public VehicleImpl toRealObject() {
		Gbl.assertNotNull(id);
//		Gbl.assertNotNull(type);
		return new VehicleImpl(Id.createVehicleId(this.id), VehicleUtils.getDefaultVehicleType());
	}
}
