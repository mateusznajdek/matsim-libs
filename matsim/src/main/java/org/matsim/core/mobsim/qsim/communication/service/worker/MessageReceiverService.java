package org.matsim.core.mobsim.qsim.communication.service.worker;


import org.matsim.core.mobsim.qsim.communication.Subscriber;
import org.matsim.core.mobsim.qsim.communication.model.MessagesTypeEnum;
import org.matsim.core.mobsim.qsim.communication.model.messages.Message;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * Socket where all messages addressed to the given client are received.
 */
public class MessageReceiverService {
	private final static Logger LOG = LogManager.getLogger(MessageReceiverService.class);

	private final Map<MessagesTypeEnum, List<Subscriber>> subscriberRepository = new HashMap<>();
	private final ThreadPoolExecutor connectionExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
	private final ExecutorService listenerExecutor = newSingleThreadExecutor();
	@Getter
	private int port;

	public MessageReceiverService() {
		Arrays.stream(MessagesTypeEnum.values())
			.forEach(messagesType -> subscriberRepository.put(messagesType, new LinkedList<>()));
		initSocket();
	}

	//	@PostConstruct // TODO PostConstruct is not working here :(
	private void initSocket() {
		listenerExecutor.submit(new Listener());
	}

	public void addNewSubscriber(Subscriber subscriber, MessagesTypeEnum messagesEnum) {
		subscriberRepository.get(messagesEnum).add(subscriber);
	}

	public void propagateMessage(Message message) {
		LOG.debug("Worker receive message: " + message.getMessageType());
		subscriberRepository.get(message.getMessageType()).forEach(subscriber -> subscriber.notify(message));
	}

	private class Listener implements Runnable {

		@Override
		public void run() {
			try {
				Random random = new Random();
				int portSeed = 10000 + Math.abs(random.nextInt() % 40000);
				ServerSocket ss = null;

				while (true) {
					try {
						ss = new ServerSocket(portSeed);
						portSeed = 10000 + Math.abs(random.nextInt() % 40000);
						break;
					} catch (Exception e) {
						LOG.warn("Port: " + portSeed + " is not available", e);
					}
				}
				port = ss.getLocalPort();
				LOG.info("Listening port is open on " + port);
				while (true) {
					Socket s = ss.accept();
					LOG.info("Setting up new connection on " + s);
					connectionExecutor.submit(new SingleConnectionExecutor(s));
				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				LOG.error("Unexpected exception occurred", e);
			}
		}
	}

	@RequiredArgsConstructor
	private class SingleConnectionExecutor implements Runnable {

		private final Socket clientSocket;

		@Override
		public void run() {
			try {
				DataInputStream dataInputStream = createDataInputStreamOrThrowException();

				while (true) {
					Message message = readMessageOrThrowException(dataInputStream);
					propagateMessage(message);
				}
			} catch (RuntimeException e) {
				LOG.error("Exception occurred in message handling thread", e);
			}

		}

		private DataInputStream createDataInputStreamOrThrowException() {
			try {
				return new DataInputStream(clientSocket.getInputStream());
			} catch (IOException | NullPointerException e) {
				LOG.error(e.getMessage());
				throw new RuntimeException(e);
			}
		}

		private Message readMessageOrThrowException(DataInputStream dataInputStream) {
			try {
				int length = dataInputStream.readInt();
				byte[] bytes = dataInputStream.readNBytes(length);
				return (Message) SerializationUtils.deserialize(bytes);
			} catch (IOException | NullPointerException e) {
				LOG.error(e.getMessage());
				throw new RuntimeException(e);
			}
		}
	}
}
