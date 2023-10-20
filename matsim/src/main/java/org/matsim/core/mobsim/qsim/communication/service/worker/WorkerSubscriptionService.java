package org.matsim.core.mobsim.qsim.communication.service.worker;

import com.google.inject.Inject;
import org.matsim.core.mobsim.qsim.communication.Subscriber;
import org.matsim.core.mobsim.qsim.communication.model.MessagesTypeEnum;

public class WorkerSubscriptionService {

	private final MessageReceiverService receiverService;

	@Inject
	public WorkerSubscriptionService(MessageReceiverService receiverService) {
		this.receiverService = receiverService;
	}

	public void subscribe(Subscriber subscriber, MessagesTypeEnum messagesEnum) {
		receiverService.addNewSubscriber(subscriber, messagesEnum);
	}
}
