package org.matsim.core.mobsim.qsim.communication.model.serializable;

import java.io.Serializable;

public interface CustomSerializable<E> extends Serializable {

	E toRealObject();

}
