package communication.model.messages;


import communication.model.MessagesTypeEnum;

public class CompletedInitializationMessage implements Message {
    @Override
    public MessagesTypeEnum getMessageType() {
        return MessagesTypeEnum.CompletedInitializationMessage;
    }
}
