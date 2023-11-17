import com.google.inject.Singleton;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.mobsim.qsim.communication.model.WorkerId;
import org.matsim.core.mobsim.qsim.communication.service.server.*;
import org.matsim.core.mobsim.qsim.communication.service.worker.MessageReceiverService;
import org.matsim.core.mobsim.qsim.communication.service.worker.MessageSenderService;
import org.matsim.core.mobsim.qsim.communication.service.worker.MyWorkerId;
import org.matsim.core.mobsim.qsim.communication.service.worker.WorkerSubscriptionService;
import org.matsim.core.mobsim.qsim.communication.service.worker.sync.StepSynchronizationService;
import org.matsim.core.mobsim.qsim.communication.service.worker.sync.StepSynchronizationServiceImpl;
import org.matsim.core.mobsim.qsim.communication.service.worker.sync.NeighbourManager;
import org.matsim.core.mobsim.qsim.communication.service.worker.sync.NeighbourManagerImpl;
import org.matsim.core.mobsim.qsim.communication.startingup.ServerStrategyService;
import org.matsim.core.mobsim.qsim.communication.startingup.StrategySelectionService;
import org.matsim.core.mobsim.qsim.communication.startingup.WorkerStrategyService;

public class ParallelizationModule extends AbstractModule {
	@Override
	public void install() {
		bind(StrategySelectionService.class).in(Singleton.class);
		bind(WorkerStrategyService.class).in(Singleton.class);
		bind(ServerStrategyService.class).in(Singleton.class);
		bind(WorkerSubscriptionService.class).in(Singleton.class);
		bind(MessageSenderService.class).in(Singleton.class);
		bind(MessageReceiverService.class).in(Singleton.class);
		bind(MessageSenderServerService.class).in(Singleton.class);
		bind(WorkerRepository.class).in(Singleton.class);
		bind(ConnectionInitializationService.class).in(Singleton.class);
		bind(MessagePropagationService.class).to(SubscriptionServiceImpl.class).in(Singleton.class);
		bind(StepSynchronizationService.class).to(StepSynchronizationServiceImpl.class).in(Singleton.class);
		bind(WorkerSynchronisationService.class).to(WorkerSynchronisationMessageImpl.class).in(Singleton.class);
		bind(MyWorkerId.class).in(Singleton.class);
	}
}
