package communication.model.messages;

import communication.model.MessagesTypeEnum;
import lombok.Value;

@Value
public class FinishSimulationMessage implements Message {

  String workerId;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.FinishSimulationMessage;
  }
}
