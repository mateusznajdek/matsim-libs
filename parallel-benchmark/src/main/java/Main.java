import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import communication.Configuration;
import communication.service.server.*;
import communication.service.worker.MessageReceiverService;
import communication.service.worker.MessageSenderService;
import communication.service.worker.WorkerSubscriptionService;
import communication.startingup.ServerStrategyService;
import communication.startingup.StrategySelectionService;
import communication.startingup.WorkerStrategyService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.examples.ExamplesUtils;

public class Main {
	private final static Logger LOG = LogManager.getLogger(Main.class);

	public static void main(String[] args) {
		Config config;
		if (args == null || args.length == 0 || args[0] == null) {
			config = ConfigUtils.loadConfig(IOUtils.extendUrl(ExamplesUtils.getTestScenarioURL("equil"), "config.xml"));
		} else {
			config = ConfigUtils.loadConfig(args);
		}

		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		config.controler().setLastIteration(0);
		System.out.println("ProcessHandle.current().pid()"); //TODO this is not enough
		System.out.println(ProcessHandle.current().pid());
//		config.controler().setOutputDirectory()

		// possibly modify config here

		// ---

		Scenario scenario = ScenarioUtils.loadScenario(config);

		// possibly modify scenario here

		// ---

		Controler controler = new Controler(scenario);

		// possibly modify controler here

//		controler.addOverridingModule( new OTFVisLiveModule() ) ;

//		controler.addOverridingModule( new SimWrapperModule() );

		// ---


		// Part of parallelization
		LOG.info("start");
		Injector injector = Guice.createInjector(new ParallelizationModule());
		StrategySelectionService strategySelectionService = injector.getInstance(StrategySelectionService.class);

		strategySelectionService.selectModeAndStartSimulation();
		LOG.info("end");
		// -----

		controler.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				bind(Configuration.class).toInstance(Configuration.getDefault());
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
				bind(WorkerSynchronisationService.class).to(WorkerSynchronisationMessageImpl.class).in(Singleton.class);
			}
		});

		controler.run();
	}
}
