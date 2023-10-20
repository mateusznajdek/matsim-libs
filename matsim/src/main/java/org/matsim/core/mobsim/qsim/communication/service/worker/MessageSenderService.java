package org.matsim.core.mobsim.qsim.communication.service.worker;


import com.google.inject.Inject;
import org.matsim.core.mobsim.qsim.communication.Configuration;
import org.matsim.core.mobsim.qsim.communication.Connection;
import org.matsim.core.mobsim.qsim.communication.Subscriber;
import org.matsim.core.mobsim.qsim.communication.model.WorkerId;
import org.matsim.core.mobsim.qsim.communication.model.MessagesTypeEnum;
import org.matsim.core.mobsim.qsim.communication.model.messages.Message;
import org.matsim.core.mobsim.qsim.communication.model.messages.ServerInitializationMessage;
import org.matsim.core.mobsim.qsim.communication.model.serializable.ConnectionDto;
import org.matsim.core.mobsim.qsim.communication.model.serializable.WorkerDataDto;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MessageSenderService implements Subscriber {
	private final static Logger LOG = LogManager.getLogger(MessageSenderService.class);

	private final Map<WorkerId, Connection> neighbourRepository = new HashMap<>();
	@Getter
	private final Map<WorkerId, ConnectionDto> connectionDtoMap = new HashMap<>();
	private final Configuration configuration;
	private Connection serverConnection;
	private final WorkerSubscriptionService subscriptionService;

	@Inject
	public MessageSenderService(WorkerSubscriptionService subscriptionService,
								Configuration configuration) {
		this.subscriptionService = subscriptionService;
		this.configuration = configuration;
		init();
	}

	//	@PostConstruct
	void init() {
		subscriptionService.subscribe(this, MessagesTypeEnum.ServerInitializationMessage);
	}

	/**
	 * @param workerId - unique worker id
	 * @param message       - message to send
	 * @throws IOException <p>Method send message to specific client</p>
	 */
	public void send(WorkerId workerId, Message message) throws IOException {
		LOG.debug("Worker send message to: " + workerId + " message type: " + message.getMessageType());
		neighbourRepository.get(workerId).send(message);
	}

	public void sendServerMessage(Message message) throws IOException {
		if (serverConnection == null) {
			createServerConnection();
		}
		LOG.debug("Worker send message to: SERVER message type: " + message.getMessageType());
		serverConnection.send(message);
	}

	private void createServerConnection() {
//		String ip = CollectionUtils.isEmpty(HiPUTS.globalInitArgs) ? "127.0.0.1" : HiPUTS.globalInitArgs.get(0);
		String ip = this.configuration.getServerIp(); // TODO fix it to extract from parameter value
		LOG.info("Server address {}:{}", ip, configuration.getServerPort());
		ConnectionDto connectionDto = ConnectionDto.builder()
			.port(configuration.getServerPort())
			.address(ip)
			.id("SERVER")
			.build();
		serverConnection = new Connection(connectionDto);
	}

	/**
	 * @param message - message to send
	 *
	 *                <p>Method send message to all existing worker</p>
	 */
	public void broadcast(Message message) {
		neighbourRepository.values().forEach(n -> {
			try {
				n.send(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public void notify(Message message) {
		switch (message.getMessageType()) {
			case ServerInitializationMessage -> handleWorkerConnectionMessage(message);
		}

	}

	private void handleWorkerConnectionMessage(Message message) {
		ServerInitializationMessage serverInitializationMessage = (ServerInitializationMessage) message;
		serverInitializationMessage.getWorkerInfo()
			.stream()
			.map(WorkerDataDto::getConnectionData)
			.forEach(c -> {
				Connection connection = new Connection(c);
				connectionDtoMap.put(new WorkerId(c.getId()), c);
				neighbourRepository.put(new WorkerId(c.getId()), connection);
			});
	}
}
