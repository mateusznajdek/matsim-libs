package org.matsim.core.mobsim.qsim.communication.model.matisim;

import com.google.inject.Inject;
import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.mobsim.qsim.agents.PersonDriverAgentImpl;
import org.matsim.core.mobsim.qsim.interfaces.Netsim;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicleImpl;

public class DeserializeUtil {

	private final World world;

	@Inject
	public DeserializeUtil(World world) {
		this.world = world;
	}

	public QVehicleImpl deserializeQVehicle(SerializedQVehicle serializedQVehicle) {
		QVehicleImpl vehicle = serializedQVehicle.toRealObject(world);
		// Fix backpointers
		((PersonDriverAgentImpl)vehicle.getDriver()).getBasicAgentDelegate().setVehicle(vehicle);
		return vehicle;
	}

	public void setNetsim(Netsim simulation) {
		this.world.setSimulation(simulation);
	}

	public void setMobsimTimer(MobsimTimer mobsimTimer) {
		this.world.setSimTimer(mobsimTimer);
	}
}
