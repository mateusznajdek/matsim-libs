package org.matsim.core.mobsim.qsim.communication.model.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.matsim.core.mobsim.qsim.communication.model.MessagesTypeEnum;
import org.matsim.core.mobsim.qsim.communication.model.matisim.SerializedQVehicle;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
@ToString
public class SyncStepMessage implements Message, Comparable<SyncStepMessage> {

	String fromWorkerId;
	int random;
	int step;
	private final List<SerializedQVehicle> vehicles;
	private final Map<String, Double> usedSpaceIncomingLanes;

	@Override
	public MessagesTypeEnum getMessageType() {
		return MessagesTypeEnum.SyncStepMessage;
	}

	@Override
	public int compareTo(SyncStepMessage newVal) {
		return Integer.compare(this.step, newVal.getStep());
	}

}
