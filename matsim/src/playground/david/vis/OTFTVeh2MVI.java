package playground.david.vis;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.matsim.basic.v01.Id;
import org.matsim.gbl.Gbl;
import org.matsim.mobsim.QueueNetworkLayer;
import org.matsim.mobsim.snapshots.PositionInfo;
import org.matsim.network.MatsimNetworkReader;
import org.matsim.utils.io.IOUtils;
import org.matsim.world.World;

import playground.david.vis.handler.OTFAgentsListHandler;

public class OTFTVeh2MVI extends OTFQuadFileHandlerZIP{
	private final   String netFileName = "";
	private  String vehFileName = "";
	private  String outFileName = "";
	private static final int BUFFERSIZE = 100000000;


	private final OTFAgentsListHandler.Writer writer = new OTFAgentsListHandler.Writer();
	
	public OTFTVeh2MVI(QueueNetworkLayer net, String vehFileName, String outFileName, double startTime, double intervall_s) {
		super(intervall_s, net, outFileName);
		this.vehFileName = vehFileName;
		this.outFileName = outFileName;
		this.intervall_s = intervall_s;
	}

	@Override
	protected void onAdditionalQuadData() {
		quad.addAdditionalElement(writer);
	}

	ByteBuffer buf = ByteBuffer.allocate(BUFFERSIZE);
	private int cntPositions=0;
	private double lastTime=-1;
	private int cntTimesteps=0;
	
	private void convert() {

		open();
		// read and convert data from veh-file
		
		BufferedReader reader = null;
		try {
			reader = IOUtils.getBufferedReader(this.vehFileName);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		try {
			reader.readLine(); // header, we do not use it
			String line = null;
			while ( (line = reader.readLine()) != null) {
	
				String[] result = line.split("\t");
				if (result.length == 16) {
					double easting = Double.parseDouble(result[11]);
					double northing = Double.parseDouble(result[12]);
					if (easting >= quad.getMinEasting() && easting <= quad.getMaxEasting() && northing >= quad.getMinNorthing() && northing <= quad.getMaxNorthing()) {
						String agent = result[0];
						String time = result[1];
//					String dist = result[5];
						String speed = result[6];
						String elevation = result[13];
						String azimuth = result[14];
						PositionInfo position = new PositionInfo(new Id(agent), easting, northing,
								Double.parseDouble(elevation), Double.parseDouble(azimuth), Double.parseDouble(speed), PositionInfo.VehicleState.Driving);
						addVehicle(Double.parseDouble(time), position);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		finish();
	}

	private void addVehicle(double time, PositionInfo position) {
		this.cntPositions++;

		// Init lastTime with first occurence of time!
		if (lastTime == -1) lastTime = time;
		
		if (time != this.lastTime) {
			this.cntTimesteps++;

			// the time changes
				// this is a dumpable timestep
				try {
					dump((int)this.lastTime);
					writer.positions.clear();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			this.lastTime = time;
		}			

		if (time == nextTime) {
			writer.positions.add(position);
		}
	}
	
	List<PositionInfo> listAgents = new LinkedList<PositionInfo>();
	

	private void finish() {
	try {
		close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}
	
	
	public static void main(String[] args) {

		String netFileName = "../studies/schweiz/2network/ch.xml"; 
		String vehFileName = "../runs/run168/run168.it210.T.veh"; 
		String outFileName = "output/testSWI1.mvi.gz";
		
		Gbl.createConfig(null);
		Gbl.startMeasurement();
		World world = Gbl.createWorld();

		QueueNetworkLayer net = new QueueNetworkLayer();
		new MatsimNetworkReader(net).readFile(netFileName);
		world.setNetworkLayer(net);

		
		OTFTVeh2MVI test  = new OTFTVeh2MVI(net, vehFileName, outFileName, 0, 300);
		test.convert();
	}


}
