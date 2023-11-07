import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.examples.ExamplesUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {
	private final static Logger LOG = LogManager.getLogger(Main.class);

	public static void main(String[] args) {
		Config config;
		if (args == null || args.length == 0 || args[0] == null) {
			LOG.info("App started without any arguments");
			config = ConfigUtils.loadConfig(IOUtils.extendUrl(ExamplesUtils.getTestScenarioURL("equil"), "config.xml"));
		} else if (args.length == 1) {
			LOG.info("App started with single argument");
			LOG.info("Argument: " + args[0]);
			config = ConfigUtils.loadConfig(IOUtils.extendUrl(ExamplesUtils.getTestScenarioURL("equil"), "config.xml"));
			config.parallelization().setWorkerId(args[0]);
		} else {
			LOG.info("App started with more than 1 arguments");
			config = ConfigUtils.loadConfig(args);
		}

		System.out.println(config.network());

		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		config.controler().setLastIteration(2);

		LOG.info("CONFIGS:");
		LOG.info(config.parallelization().getServerIp());
		LOG.info(config.parallelization().getServerPort());
		LOG.info(config.parallelization().getWorkerCount());
		LOG.info(config.parallelization().getServerOnThisMachine());

		// possibly modify config here

		//TODO this is not enough in cluster pid will have collisions
		String newOutputDirectory = config.controler().getOutputDirectory() + "/" + getCurrentTimeStamp() + "/"
			+ config.parallelization().getWorkerId();
		config.controler().setOutputDirectory(newOutputDirectory);
		LOG.info("Output directory changed to " + newOutputDirectory);
		// ---

		Scenario scenario = ScenarioUtils.loadScenario(config);

		// possibly modify scenario here
		scenario.getNetwork().getNodes();
//		scenario.getNetwork().getLinks().values().forEach(link -> System.out.println(link.getAttributes().getAttribute("partition")));
		// ---

		Controler controler = new Controler(scenario);

		// possibly modify controler here

//		controler.addOverridingModule( new OTFVisLiveModule() ) ;

//		controler.addOverridingModule( new SimWrapperModule() );

		controler.addOverridingModule(new ParallelizationModule());
		// ---

		controler.run();
		LOG.info("Output: " + newOutputDirectory);
	}

	public static String getCurrentTimeStamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd;HH:mm");
		Date now = new Date();
		String strDate = sdf.format(now);
		return strDate;
	}
}
