package org.matsim.core.mobsim.qsim.communication.model.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.tuple.Triple;
import org.matsim.core.mobsim.qsim.communication.model.MessagesTypeEnum;
import org.matsim.core.mobsim.qsim.communication.model.matisim.SerializedBasicPlanAgentImpl;

import java.util.List;

@Getter
@AllArgsConstructor
@ToString
public class TeleportationMessage implements Message {

	String fromWorkerId;

	private final List<Triple<Double, SerializedBasicPlanAgentImpl, String/*Id<Link> */>> teleportedAgents;

	@Override
	public MessagesTypeEnum getMessageType() {
		return MessagesTypeEnum.TeleportationMessage;
	}


}
