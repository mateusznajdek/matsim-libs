package communication.service.server;


import communication.model.messages.Message;

public interface MessagePropagationService {
	void propagateMessage(Message message, String workerId);

}
