package org.matsim.core.mobsim.qsim.communication.service.server;

import org.matsim.core.mobsim.qsim.communication.Subscriber;
import org.matsim.core.mobsim.qsim.communication.model.MessagesTypeEnum;

public interface SubscriptionService {
	void subscribe(Subscriber subscriber, MessagesTypeEnum messagesEnum);
}

