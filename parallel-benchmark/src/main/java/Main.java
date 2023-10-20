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

		//TODO this is not enough in cluster pid will have collisions
		String newOutputDirectory = config.controler().getOutputDirectory() + "/" + ProcessHandle.current().pid();
		config.controler().setOutputDirectory(newOutputDirectory);
		LOG.info("Output directory changed to " + newOutputDirectory);
		// ---

		Scenario scenario = ScenarioUtils.loadScenario(config);

		// possibly modify scenario here

		// ---

		Controler controler = new Controler(scenario);

		// possibly modify controler here

//		controler.addOverridingModule( new OTFVisLiveModule() ) ;

//		controler.addOverridingModule( new SimWrapperModule() );

		controler.addOverridingModule(new ParallelizationModule());
		// ---

		controler.run();
	}
}
