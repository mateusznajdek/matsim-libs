package org.matsim.core.mobsim.qsim.communication.model.messages;

import org.matsim.core.mobsim.qsim.communication.model.MessagesTypeEnum;

import java.io.Serializable;

public interface Message extends Serializable {

	MessagesTypeEnum getMessageType();
}
