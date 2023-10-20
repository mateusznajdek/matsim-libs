package org.matsim.core.mobsim.qsim.communication;

import org.matsim.core.mobsim.qsim.communication.model.messages.Message;

public interface Subscriber {

	void notify(Message message);
}

