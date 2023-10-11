import com.google.inject.Guice;
import com.google.inject.Injector;
import communication.startingup.StrategySelectionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
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

		controler.run();
	}
}
