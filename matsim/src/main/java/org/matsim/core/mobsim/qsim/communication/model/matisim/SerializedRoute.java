package org.matsim.core.mobsim.qsim.communication.model.matisim;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Route;
import org.matsim.core.mobsim.qsim.communication.model.serializable.CustomSerializable;
import org.matsim.core.population.routes.GenericRouteImpl;
import org.matsim.core.population.routes.LinkNetworkRouteImpl;

import java.util.List;
import java.util.stream.Collectors;

public class SerializedRoute implements CustomSerializable<Route> {
	private String implementationType; // LinkNetworkRouteImpl vs GenericRouteImpl


	// AbstractRoute is shared for LinkNetworkRouteImpl & GenericRouteImpl
	private String /*Id<Link>*/ startLinkId;
	private String /*Id<Link>*/ endLinkId;
	private boolean locked;
	private double dist;

	protected double travTime;

	// LinkNetworkRouteImpl
	private List<String /*Id<Link>*/> route;
	private List<String /*Id<Link>*/> safeRoute;
	private double travelCost;
	private String /*Id<Vehicle>*/ vehicleId;

	// GenericRouteImpl
	private String routeDescription;

	public SerializedRoute(Route route) {
		if (route instanceof LinkNetworkRouteImpl) {
			this.implementationType = "LinkNetworkRouteImpl";
			this.route = ((LinkNetworkRouteImpl) route).getRoute().stream().map(Object::toString).collect(Collectors.toList());
			this.safeRoute = ((LinkNetworkRouteImpl) route).getSafeRoute().stream().map(Object::toString).collect(Collectors.toList());
			this.travelCost = ((LinkNetworkRouteImpl) route).getTravelCost();
			this.vehicleId = ((LinkNetworkRouteImpl) route).getVehicleId().toString();
			this.startLinkId = route.getStartLinkId().toString();
			this.endLinkId = route.getEndLinkId().toString();
			this.locked = ((LinkNetworkRouteImpl) route).isLocked();
			this.dist = ((LinkNetworkRouteImpl) route).getDist();
			this.travTime = ((LinkNetworkRouteImpl) route).getTravTime();
		} else if (route instanceof GenericRouteImpl) {
			this.implementationType = "GenericRouteImpl";
			this.routeDescription = route.getRouteDescription();
			this.startLinkId = route.getStartLinkId().toString();
			this.endLinkId = route.getEndLinkId().toString();
			this.locked = ((GenericRouteImpl) route).isLocked();
			this.dist = ((GenericRouteImpl) route).getDist();
			this.travTime = ((GenericRouteImpl) route).getTravTime();
		} else {
			System.err.println("SerializedRoute -> unknown instanceof Route");
			throw new RuntimeException("SerializedRoute -> unknown instanceof Route");
		}
	}

	@Override
	public Route toRealObject() {
		if (implementationType.equals("LinkNetworkRouteImpl")) {
			LinkNetworkRouteImpl route = new LinkNetworkRouteImpl(
				Id.createLinkId(this.startLinkId),
				this.route.stream().map(Id::createLinkId).collect(Collectors.toList()),
				Id.createLinkId(this.endLinkId)
			);
			route.setTravelCost(this.travelCost);
			route.setVehicleId(Id.createVehicleId(this.vehicleId));
			route.setLocked(this.locked);
			route.setDist(this.dist);
			//		route.setStartLinkId(this.startLinkId); this should be done in constructor
			//		route.setEndLinkId(this.endLinkId);
			return route;
		} else if (implementationType.equals("GenericRouteImpl")) {
			return new GenericRouteImpl(
				Id.createLinkId(this.startLinkId),
				Id.createLinkId(this.endLinkId)
			);
		} else {
			System.err.println("SerializedRoute -> unknown instanceof Route");
			throw new RuntimeException("SerializedRoute -> unknown instanceof Route");
		}
	}
}
