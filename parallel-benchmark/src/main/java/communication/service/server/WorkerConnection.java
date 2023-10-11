package communication.service.server;


import communication.model.MessagesTypeEnum;
import communication.model.messages.Message;
import communication.model.messages.WorkerConnectionMessage;
import lombok.Getter;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class WorkerConnection implements Runnable {
	private final static Logger LOG = LogManager.getLogger(WorkerConnection.class);

	@Getter
	private final String workerId;
	@Getter
	private final int port;
	@Getter
	private final String address;
	private final MessagePropagationService messagePropagationService;
	private final DataOutputStream outputStream;
	private final DataInputStream inputStream;

	public WorkerConnection(DataInputStream inputStream, MessagePropagationService messagePropagationService, WorkerConnectionMessage workerConnectionMessage)
		throws IOException {
		this.messagePropagationService = messagePropagationService;
		this.workerId = workerConnectionMessage.getWorkerId();
		port = workerConnectionMessage.getPort();
		address = workerConnectionMessage.getAddress();

		this.inputStream = inputStream;
		Socket socket = new Socket(address, port);
		this.outputStream = new DataOutputStream(socket.getOutputStream());
	}

	@Override
	public void run() {
		try {
			Message message = null;

			do {
				int length = inputStream.readInt();
				byte[] bytes = inputStream.readNBytes(length);
				message = (Message) SerializationUtils.deserialize(bytes);

				messagePropagationService.propagateMessage(message, workerId);
			} while (message.getMessageType() != MessagesTypeEnum.WorkerDisconnectMessage || message.getMessageType() == MessagesTypeEnum.ShutDownMessage);
		} catch (Exception e) {
			LOG.error("Fail messageHandler for worker id: " + workerId, e);
		}
	}

	void send(Message message) {
		try {
			byte[] bytes = SerializationUtils.serialize(message);
			int size = bytes.length;
			outputStream.writeInt(size);
			outputStream.flush();
			outputStream.write(bytes);
			outputStream.flush();
		} catch (IOException e) {
			LOG.error("Can not send message to workerId: " + workerId, e);
		}
	}
}
