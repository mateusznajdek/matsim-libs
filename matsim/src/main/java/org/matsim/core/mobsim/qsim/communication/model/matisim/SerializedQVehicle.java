package org.matsim.core.mobsim.qsim.communication.model.matisim;

import lombok.Getter;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.mobsim.framework.DriverAgent;
import org.matsim.core.mobsim.framework.PassengerAgent;
import org.matsim.core.mobsim.qsim.communication.model.serializable.CustomSerializable;
import org.matsim.core.mobsim.qsim.interfaces.MobsimVehicle;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicleImpl;
import org.matsim.vehicles.Vehicle;

import java.util.Collection;

public class SerializedQVehicle implements CustomSerializable<QVehicleImpl> {

	private static int warnCount = 0;

	private double linkEnterTime = 0. ;
	private double earliestLinkExitTime = 0;
	private DriverAgent driver = null;
	private Collection<PassengerAgent> passengers = null;
	private final String /**Id<Vehicle>**/ vehicleId;
	@Getter
	private final String /**Id<Link>**/ currentLinkId;
	private final SerializedVehicle vehicle;
	private final int passengerCapacity = 4; // Don't care for now

	public SerializedQVehicle(QVehicle qvehicle) {
		this.earliestLinkExitTime = qvehicle.getEarliestLinkExitTime();
		this.linkEnterTime = qvehicle.getLinkEnterTime();
		this.vehicleId = qvehicle.getId().toString();
		this.currentLinkId = qvehicle.getCurrentLink().getId().toString();
		this.vehicle = new SerializedVehicle(qvehicle.getVehicle());
	}


	@Override
	public QVehicleImpl toRealObject() {
		// NOTE: this is not fully populated at this point
		// please refer to DeserializeVehicleUtil
		QVehicleImpl qVehicle = new QVehicleImpl(vehicle.toRealObject());
		qVehicle.setLinkEnterTime(this.linkEnterTime);
		qVehicle.setEarliestLinkExitTime(this.earliestLinkExitTime);
		return qVehicle;
	}

}
