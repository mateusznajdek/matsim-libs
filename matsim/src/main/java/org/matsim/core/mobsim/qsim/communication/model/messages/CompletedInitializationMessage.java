package org.matsim.core.mobsim.qsim.communication.model.messages;


import org.matsim.core.mobsim.qsim.communication.model.MessagesTypeEnum;

public class CompletedInitializationMessage implements Message {
    @Override
    public MessagesTypeEnum getMessageType() {
        return MessagesTypeEnum.CompletedInitializationMessage;
    }
}
