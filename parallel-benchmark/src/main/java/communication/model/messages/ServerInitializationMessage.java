package communication.model.messages;

import communication.model.MessagesTypeEnum;
import communication.model.serializable.WorkerDataDto;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ServerInitializationMessage implements Message {

	/**
	 * Worker patchIds list
	 */
	List<String> patchIds;
	/**
	 * Neighbouring info
	 */
	List<WorkerDataDto> workerInfo;

	/**
	 * Worker generated more new car then other
	 */
	boolean bigWorker;

	@Override
	public MessagesTypeEnum getMessageType() {
		return MessagesTypeEnum.ServerInitializationMessage;
	}
}
