package org.matsim.core.mobsim.qsim.communication.model.matisim;

import org.matsim.api.core.v01.Coord;
import org.matsim.core.mobsim.qsim.communication.model.serializable.CustomSerializable;

public class SerializedCoord implements CustomSerializable<Coord> {
	private double x;
	private double y;
//	private double z;

	public SerializedCoord(Coord coord) {
		this.x = coord.getX();
		this.y = coord.getY();
//		this.z = coord.hasZ() || coord.getZ() == Double.NEGATIVE_INFINITY ? coord.getZ() : Double.NEGATIVE_INFINITY;
	}

	@Override
	public Coord toRealObject() {
		return new Coord(x, y);
	}
}
