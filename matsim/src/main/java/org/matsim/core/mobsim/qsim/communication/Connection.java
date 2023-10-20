package org.matsim.core.mobsim.qsim.communication;

import org.matsim.core.mobsim.qsim.communication.model.WorkerId;
import org.matsim.core.mobsim.qsim.communication.model.messages.Message;
import org.matsim.core.mobsim.qsim.communication.model.serializable.ConnectionDto;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

public class Connection {

	private final static Logger LOG = LogManager.getLogger(Connection.class);

	private DataOutputStream output;
	private final WorkerId id;

	public Connection(ConnectionDto message) {
		id = new WorkerId(message.getId());
		for (int i = 0; i < 10; i++) {
			try {
				Socket socket = new Socket(message.getAddress(), message.getPort());
				output = new DataOutputStream(socket.getOutputStream());
				return;
			} catch (IOException e) {
				LOG.warn("Error connection with neighbour {}", message.getId(), e);
				try {
					Thread.sleep(1000 * (i + 1));
				} catch (InterruptedException ex) {
					LOG.error("Thread error");
				}
			}
		}
		LOG.warn("Error connection with neighbour {}", message.getId());
	}

	public synchronized void send(Message message) throws IOException {
		if (Objects.isNull(output)) {
			LOG.info("Connection with worker " + id + " not exist");
			return;
		}
		byte[] bytes = SerializationUtils.serialize(message);
		int size = bytes.length;

		output.writeInt(size);
		output.flush();
		output.write(bytes);
		output.flush();
	}
}
