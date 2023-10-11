package communication.model.messages;


import communication.model.MessagesTypeEnum;

public class ShutDownMessage implements Message{

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.ShutDownMessage;
  }
}
