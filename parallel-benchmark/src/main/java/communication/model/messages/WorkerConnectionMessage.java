package communication.model.messages;

import communication.model.MessagesTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class WorkerConnectionMessage implements Message {

	private String address;
	private int port;
	private String workerId;

	@Override
	public MessagesTypeEnum getMessageType() {
		return MessagesTypeEnum.WorkerConnectionMessage;
	}
}
