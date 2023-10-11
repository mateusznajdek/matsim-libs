package communication.service.server;

import com.google.inject.Inject;
import communication.model.messages.Message;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class MessageSenderServerService {
	private final static Logger LOG = LogManager.getLogger(MessageSenderServerService.class);

	private final WorkerRepository workerRepository;

	@Inject
	public MessageSenderServerService(WorkerRepository workerRepository) {
		this.workerRepository = workerRepository;
	}

	/**
	 * @param workerId - unique worker id
	 * @param message - message to send
	 *
	 * @throws IOException <p>Method send message to specific client</p>
	 */
	public void send(String workerId, Message message) {
		workerRepository.get(workerId).send(message);
	}

	/**
	 * @param message - message to send
	 *
	 *     <p>Method send message to all existing worker</p>
	 */
	public void broadcast(Message message) {
		LOG.info("Broadcasting message " + message.getMessageType());
		workerRepository.getAll()
			.forEach(n -> n.send(message));
	}
}
