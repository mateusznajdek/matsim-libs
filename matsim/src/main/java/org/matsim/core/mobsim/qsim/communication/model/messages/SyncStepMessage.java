package org.matsim.core.mobsim.qsim.communication.model.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.matsim.core.mobsim.qsim.communication.model.MessagesTypeEnum;
import org.matsim.core.mobsim.qsim.communication.model.matisim.SerializedQVehicle;

import java.util.List;

@Getter
@AllArgsConstructor
public class SyncStepMessage implements Message, Comparable<SyncStepMessage> {

	String fromWorkerId;
	int random;
	int step;
	private final List<SerializedQVehicle> vehicles;

	@Override
	public MessagesTypeEnum getMessageType() {
		return MessagesTypeEnum.SyncStepMessage;
	}

	@Override
	public int compareTo(SyncStepMessage newVal) {
		return Integer.compare(this.step, newVal.getStep());
	}

	@Override
	public String toString() {
		return "SyncStepMessage{" +
			"workerId='" + fromWorkerId + '\'' +
			", random=" + random +
			", step=" + step +
			'}';
	}
}
