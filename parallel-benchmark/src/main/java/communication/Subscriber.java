package communication;

import communication.model.messages.Message;

public interface Subscriber {

	void notify(Message message);
}

