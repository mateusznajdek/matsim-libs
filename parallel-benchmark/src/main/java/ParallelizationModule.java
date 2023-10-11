import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import communication.Configuration;
import communication.service.server.*;
import communication.service.worker.MessageReceiverService;
import communication.service.worker.MessageSenderService;
import communication.service.worker.WorkerSubscriptionService;
import communication.startingup.ServerStrategyService;
import communication.startingup.StrategySelectionService;
import communication.startingup.WorkerStrategyService;

public class ParallelizationModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(Configuration.class).toInstance(Configuration.getDefault());
		bind(StrategySelectionService.class).in(Singleton.class);
		bind(WorkerStrategyService.class).in(Singleton.class);
		bind(ServerStrategyService.class).in(Singleton.class);
		bind(WorkerSubscriptionService.class).in(Singleton.class);
		bind(MessageSenderService.class).in(Singleton.class);
		bind(MessageReceiverService.class).in(Singleton.class);
		bind(WorkerRepository.class).in(Singleton.class);
		bind(ConnectionInitializationService.class).in(Singleton.class);
		bind(MessagePropagationService.class).to(SubscriptionServiceImpl.class).in(Singleton.class);
		bind(WorkerSynchronisationService.class).to(WorkerSynchronisationMessageImpl.class).in(Singleton.class);
	}
}
