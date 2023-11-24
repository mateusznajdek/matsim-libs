package org.matsim.core.mobsim.qsim.communication.model.matisim;

import lombok.Getter;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.mobsim.qsim.agents.PersonDriverAgentImpl;
import org.matsim.core.mobsim.qsim.communication.model.serializable.CustomMatSimSerializable;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicleImpl;

public class SerializedQVehicle implements CustomMatSimSerializable<QVehicleImpl, World> {

	private static int warnCount = 0;

	private double linkEnterTime = 0.;
	private double earliestLinkExitTime = 0;
	private SerializedDriver driver = null;
	// redundant - vehicle already contains this info
	private final String /**Id<Vehicle>**/
		vehicleId;
	@Getter
	private final String /**Id<Link>**/
		currentLinkId;
	private final SerializedVehicle vehicle;

	public SerializedQVehicle(QVehicle qvehicle) {
		this.earliestLinkExitTime = qvehicle.getEarliestLinkExitTime();
		this.linkEnterTime = qvehicle.getLinkEnterTime();
		this.vehicleId = qvehicle.getId().toString();
		this.currentLinkId = qvehicle.getCurrentLink().getId().toString();
		this.vehicle = qvehicle.getVehicle()!=null ? new SerializedVehicle(qvehicle.getVehicle()) : null;
		if ((PersonDriverAgentImpl) qvehicle.getDriver() == null)
			throw new RuntimeException("Auuuuc");
		this.driver = new SerializedDriver((PersonDriverAgentImpl) qvehicle.getDriver());
	}

	@Override
	public QVehicleImpl toRealObject(World world) {
		// NOTE: this is not fully populated at this point
		// please refer to DeserializeVehicleUtil
		QVehicleImpl qVehicle = new QVehicleImpl(vehicle.toRealObject());
		qVehicle.setLinkEnterTime(this.linkEnterTime);
		qVehicle.setEarliestLinkExitTime(this.earliestLinkExitTime);
		qVehicle.setDriver(this.driver.toRealObject(world));
		Link currentLinkForVehicle = world.getLinks().get(Id.createLinkId(currentLinkId));
		qVehicle.setCurrentLink(currentLinkForVehicle);
		return qVehicle;
	}

}
