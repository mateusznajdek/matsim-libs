package org.matsim.core.mobsim.qsim.communication.service.server;


import org.matsim.core.mobsim.qsim.communication.model.messages.Message;

public interface MessagePropagationService {
	void propagateMessage(Message message, String workerId);

}
