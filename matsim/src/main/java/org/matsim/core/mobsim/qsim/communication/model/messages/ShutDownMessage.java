package org.matsim.core.mobsim.qsim.communication.model.messages;


import org.matsim.core.mobsim.qsim.communication.model.MessagesTypeEnum;

public class ShutDownMessage implements Message{

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.ShutDownMessage;
  }
}
