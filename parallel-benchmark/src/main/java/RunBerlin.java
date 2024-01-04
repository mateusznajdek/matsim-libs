//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.matsim.api.core.v01.Scenario;
//import org.matsim.core.config.Config;
//import org.matsim.core.controler.Controler;
//import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//public class RunBerlin {
//	private final static Logger LOG = LogManager.getLogger(RunBerlin.class);
//
//	public static void main(String[] args) {
//		// TODO and to config file the parallel params
//		Config config = RunBerlinScenario.prepareConfig(args);
//
//		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
//		config.controler().setLastIteration(0);
//
//		// possibly modify config here
//		String newOutputDirectory = config.controler().getOutputDirectory() + "/" + getCurrentTimeStamp() + "/"
//			+ config.parallelization().getWorkerId();
//		config.controler().setOutputDirectory(newOutputDirectory);
//		LOG.info("Output directory changed to " + newOutputDirectory);
//		// ---
//
//		Scenario scenario = RunBerlinScenario.prepareScenario(config);
//		Controler controler = RunBerlinScenario.prepareControler(scenario);
//		controler.addOverridingModule(new ParallelizationModule());
//		controler.run();
//	}
//
//	public static String getCurrentTimeStamp() {
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd;HH:mm");
//		Date now = new Date();
//		String strDate = sdf.format(now);
//		return strDate;
//	}
//}
