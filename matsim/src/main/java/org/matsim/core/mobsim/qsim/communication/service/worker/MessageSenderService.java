package org.matsim.core.mobsim.qsim.communication.service.worker;


import com.google.inject.Inject;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.core.config.groups.ParallelizationConfigGroup;
import org.matsim.core.mobsim.qsim.communication.Connection;
import org.matsim.core.mobsim.qsim.communication.Subscriber;
import org.matsim.core.mobsim.qsim.communication.model.MessagesTypeEnum;
import org.matsim.core.mobsim.qsim.communication.model.WorkerId;
import org.matsim.core.mobsim.qsim.communication.model.messages.Message;
import org.matsim.core.mobsim.qsim.communication.model.messages.ServerInitializationMessage;
import org.matsim.core.mobsim.qsim.communication.model.serializable.ConnectionDto;
import org.matsim.core.mobsim.qsim.communication.model.serializable.WorkerDataDto;
import org.matsim.core.mobsim.qsim.communication.service.worker.sync.NeighbourManager;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

public class MessageSenderService implements Subscriber {
	private final static Logger LOG = LogManager.getLogger(MessageSenderService.class);

	private final Map<WorkerId, Connection> neighbourRepository = new HashMap<>();
	private final Map<WorkerId, Connection> connectionMap = new HashMap<>();
	@Getter
	private final Map<WorkerId, ConnectionDto> connectionDtoMap = new HashMap<>();
	private final ParallelizationConfigGroup configuration;
	private final NeighbourManager neighbourManager;
	private Connection serverConnection;
	private final WorkerSubscriptionService subscriptionService;

	@Inject
	public MessageSenderService(WorkerSubscriptionService subscriptionService,
								ParallelizationConfigGroup configuration,
								NeighbourManager neighbourManager) {
		this.subscriptionService = subscriptionService;
		this.configuration = configuration;
		this.neighbourManager = neighbourManager;
		init();
	}

	//	@PostConstruct
	void init() {
		subscriptionService.subscribe(this, MessagesTypeEnum.ServerInitializationMessage);
		neighbourManager.getMyNeighboursIds()
			.stream()
			.map(rawWorkerId -> new WorkerId(String.valueOf(rawWorkerId)))
			.filter(connectionMap::containsKey)
			.forEach(workerId -> neighbourRepository.put(workerId, connectionMap.get(workerId)));

		// vs

//		connectionMap.forEach((workerId, connection) -> {
//			if (neighbourManager.getMyNeighboursIds().contains(Integer.valueOf(workerId.getId())))
//				neighbourRepository.put(workerId, connectionMap.get(workerId));
//		});
	}

	/**
	 * @param workerId - unique worker id
	 * @param message  - message to send
	 * @throws IOException <p>Method send message to specific client</p>
	 */
	public void send(WorkerId workerId, Message message) throws IOException {
		LOG.debug("Worker send message to: " + workerId + " message type: " + message.getMessageType());
		connectionMap.get(workerId).send(message);
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
		connectionMap.forEach((workerId, connection) -> {
			try {
				connection.send(message);
			} catch (SocketException e) {
				System.out.println("dupa :(");
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	// TODO think if implement it this way!
	public void sendToNeighbours(Message message) {
		neighbourRepository.forEach((workerId, connection) -> {
			try {
				connection.send(message);
			} catch (SocketException e) {
				System.out.println("dupa :(");
				e.printStackTrace();
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
				connectionMap.put(new WorkerId(c.getId()), connection);
			});
	}
}
