package org.matsim.core.mobsim.qsim.communication.model.matisim;

import com.google.inject.Inject;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.mobsim.qsim.interfaces.MobsimVehicle;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicleImpl;

import java.util.Map;

public class DeserializeVehicleUtil {

	private final Map<Id<Link>, ? extends Link> links;

	@Inject
	public DeserializeVehicleUtil(Network network) {
		this.links = network.getLinks();
	}

	public QVehicleImpl deserializeQVehicle(SerializedQVehicle serializedQVehicle) {
		QVehicleImpl qVehicle = serializedQVehicle.toRealObject();
		Link currentLinkForVehicle = links.get(Id.createLinkId(serializedQVehicle.getCurrentLinkId()));
		qVehicle.setCurrentLink(currentLinkForVehicle);
		return qVehicle;
	}
}
