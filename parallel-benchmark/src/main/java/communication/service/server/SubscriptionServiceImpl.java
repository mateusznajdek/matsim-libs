package communication.service.server;

import com.google.inject.Inject;
import communication.Subscriber;
import communication.model.MessagesTypeEnum;
import communication.model.messages.Message;
import lombok.RequiredArgsConstructor;

import javax.annotation.PostConstruct;
import java.util.*;

public class SubscriptionServiceImpl implements SubscriptionService, MessagePropagationService {

	private final Map<MessagesTypeEnum, List<Subscriber>> subscriberRepository = new HashMap<>();
	private final WorkerSynchronisationService workerSynchronisationService;

	@Inject
	public SubscriptionServiceImpl(WorkerSynchronisationService workerSynchronisationService) {
		this.workerSynchronisationService = workerSynchronisationService;
		init();
	}

//	@PostConstruct
	private void init() {
		Arrays.stream(MessagesTypeEnum.values())
			.forEach(messagesType -> subscriberRepository.put(messagesType, new LinkedList<>()));
	}

	@Override
	public void subscribe(Subscriber subscriber, MessagesTypeEnum messagesEnum) {
		subscriberRepository.get(messagesEnum).add(subscriber);
	}

	@Override
	public void propagateMessage(Message message, String workerId) {
		subscriberRepository.get(message.getMessageType()).forEach(subscriber -> subscriber.notify(message));
		workerSynchronisationService.handleWorker(message.getMessageType(), workerId);
	}
}
