/* *********************************************************************** *
 * project: org.matsim.*
 * SimulationConfigGroup.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.core.config.groups;

import jakarta.validation.constraints.PositiveOrZero;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.core.config.ReflectiveConfigGroup;

import java.util.Map;

/**
 * @author najdek
 */
public final class ParallelizationConfigGroup extends ReflectiveConfigGroup {

	@SuppressWarnings("unused")
	private final static Logger log = LogManager.getLogger(ParallelizationConfigGroup.class);

	public static final String GROUP_NAME = "parallelization";
	private static final String SERVER_IP = "serverIp";
	private static final String SERVER_PORT = "serverPort";
	private static final String WORKER_COUNT = "workerCount";
	private static final String WORKER_ID = "workerId";
	private static final String SERVER_ON_THIS_MACHINE = "serverOnThisMachine";

	private String serverIp = "127.0.0.1";
	private int serverPort = 8081;

	@PositiveOrZero
	private int workerCount;


	private String workerId = "";

	private boolean serverOnThisMachine = false;


	@Override
	public final Map<String, String> getComments() {
		Map<String, String> map = super.getComments();
		return map;
	}


	public ParallelizationConfigGroup() {
		super(GROUP_NAME);
	}


	@StringSetter(SERVER_IP)
	public void setServerIp(String value) {
		this.serverIp = value;
	}

	@StringSetter(SERVER_PORT)
	private void setServerPort(int value) {
		this.serverPort = value;
	}

	@StringSetter(WORKER_COUNT)
	private void setWorkerCount(int value) {
		this.workerCount = value;
	}

	@StringSetter(WORKER_ID)
	public void setWorkerId(String value) {
		this.workerId = value;
	}

	@StringSetter(SERVER_ON_THIS_MACHINE)
	public void setServerOnThisMachine(boolean value) {
		this.serverOnThisMachine = value;
	}

	@StringGetter(SERVER_IP)
	public String getServerIp() {
		return serverIp;
	}


	@StringGetter(SERVER_PORT)
	public int getServerPort() {
		return this.serverPort;
	}

	@StringGetter(WORKER_COUNT)
	public int getWorkerCount() {
		return this.workerCount;
	}

	@StringGetter(WORKER_ID)
	public String getWorkerId() {
		return this.workerId;
	}

	@StringGetter(SERVER_ON_THIS_MACHINE)
	public boolean getServerOnThisMachine() {
		return this.serverOnThisMachine;
	}


}
