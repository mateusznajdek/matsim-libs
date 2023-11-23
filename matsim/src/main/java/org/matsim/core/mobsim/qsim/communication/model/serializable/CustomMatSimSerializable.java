package org.matsim.core.mobsim.qsim.communication.model.serializable;

import org.matsim.core.mobsim.qsim.communication.model.matisim.World;

import java.io.Serializable;

public interface CustomMatSimSerializable<E, T> extends Serializable {

	E toRealObject(T injected);

}
