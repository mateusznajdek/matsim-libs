package communication.model.messages;

import communication.model.MessagesTypeEnum;

import java.io.Serializable;

public interface Message extends Serializable {

	MessagesTypeEnum getMessageType();
}
