package communication.service.server;

import communication.Subscriber;
import communication.model.MessagesTypeEnum;

public interface SubscriptionService {
	void subscribe(Subscriber subscriber, MessagesTypeEnum messagesEnum);
}

