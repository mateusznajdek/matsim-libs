package communication.model.serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
@AllArgsConstructor
public class WorkerDataDto implements Serializable {

	/**
	 * Shadow patchIds with owner is this worker
	 */
	List<String> patchIds;

	/**
	 * Connecting parameters with workerId
	 */
	ConnectionDto connectionData;
}
