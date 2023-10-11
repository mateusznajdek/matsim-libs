package communication.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.concurrent.ThreadLocalRandom;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class WorkerId {

	String id;

	public static WorkerId random() {  // TODO make it better -> UID
		return new WorkerId("W" + ThreadLocalRandom.current().nextInt(0, 10000));
	}

	public static WorkerId from(WorkerId workerId) {
		return new WorkerId(workerId.getId());
	}

	public static WorkerId unique(int port) {
		return new WorkerId("W" + port + ThreadLocalRandom.current().nextInt(0, 10000));
	}
}
