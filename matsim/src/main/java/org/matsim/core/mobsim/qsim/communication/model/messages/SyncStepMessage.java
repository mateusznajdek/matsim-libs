package org.matsim.core.mobsim.qsim.communication.model.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.matsim.core.mobsim.qsim.communication.model.MessagesTypeEnum;

@Getter
@AllArgsConstructor
public class SyncStepMessage implements Message {

	//  private final List<SerializedCar> cars;
	String workerId;
	int random;
	int step;

	@Override
	public MessagesTypeEnum getMessageType() {
		return MessagesTypeEnum.SyncStepMessage;
	}
}
