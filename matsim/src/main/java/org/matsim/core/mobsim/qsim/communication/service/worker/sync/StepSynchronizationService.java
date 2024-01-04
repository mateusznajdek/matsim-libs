package org.matsim.core.mobsim.qsim.communication.service.worker.sync;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.mobsim.qsim.TeleportationEngine;
import org.matsim.core.mobsim.qsim.interfaces.Netsim;
import org.matsim.core.mobsim.qsim.interfaces.NetsimNetwork;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicleImpl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface StepSynchronizationService {

	void sendSyncMessageToNeighbours();

	void sendFinishMessageToServer();

	void getSyncMessagesAndProcess(NetsimNetwork netsimNetwork, TeleportationEngine teleportationEngine);

	void prepareDataToBeSend(Collection<QVehicle> outGoingVehicles, Map<Id<Link>, Double> usedSpaceIncomingLanes);

	int getNumberOfNeighbours();

	void setupNeighboursConnections();

	void setNetsim(Netsim simulation);

	void setMobsimTimer(MobsimTimer mobsimTimer);

	void addMosibAgentForTeleportation(double now, MobsimAgent agent, Id<Link> linkId);
}
