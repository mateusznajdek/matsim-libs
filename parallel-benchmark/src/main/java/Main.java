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
			// 164880 - steps for berlin
			config = ConfigUtils.loadConfig(IOUtils.extendUrl(ExamplesUtils.getTestScenarioURL("berlin5"), "config.xml"));
			config.parallelization().setWorkerId(args[0]);
		} else {
			LOG.info("App started with more than 1 arguments");
			config = ConfigUtils.loadConfig(args);
		}

		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		config.controler().setLastIteration(0);

		// possibly modify config here
		String newOutputDirectory = config.controler().getOutputDirectory() + "/" + getCurrentTimeStamp() + "/"
			+ config.parallelization().getWorkerId();
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

	public static String getCurrentTimeStamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd;HH:mm");
		Date now = new Date();
		String strDate = sdf.format(now);
		return strDate;
	}
}
