package org.matsim.core.mobsim.qsim.communication.model.messages;

import org.matsim.core.mobsim.qsim.communication.model.MessagesTypeEnum;
import lombok.Value;

@Value
public class FinishSimulationMessage implements Message {

  String workerId;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.FinishSimulationMessage;
  }
}
