package communication.service.server;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class WorkerRepository {

	private final Map<String, WorkerConnection> workerRepository = new HashMap<>();

	void addWorker(String workerId, WorkerConnection workerConnection){
		workerRepository.put(workerId, workerConnection);
	}

	public WorkerConnection get(String workerId){
		return workerRepository.get(workerId);
	}

	Collection<WorkerConnection> getAll(){
		return workerRepository.values();
	}

	public int countWorker() {
		return workerRepository.size();
	}

	public Collection<String> getAllWorkersIds() {
		return getAll().stream().map(WorkerConnection::getWorkerId).collect(Collectors.toList());
	}
}
