/* *********************************************************************** *
 * project: org.matsim.*
 * TransitMultiNodeDijkstra.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
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

package org.matsim.pt.router;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.FastRouterDelegateFactory;
import org.matsim.core.router.MyFastDijkstra;
import org.matsim.core.router.util.PreProcessDijkstra;
import org.matsim.core.router.util.RoutingNetwork;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.utils.collections.PseudoRemovePriorityQueue;
import org.matsim.vehicles.Vehicle;

/*
 * TODO: 
 * - replace CustomDataManager with an array-based structure using the routing networks 
 * 		array indices, e.g. like:
 * 		ArrayRoutingNetworkNode routingNetworkNode = (ArrayRoutingNetworkNode) n;
 *		return this.nodeData[routingNetworkNode.getArrayIndex()];
 */
public class FastTransitMultiNodeDijkstra extends MyFastDijkstra {

	private final CustomDataManager customDataManager = new CustomDataManager();
	
	public FastTransitMultiNodeDijkstra(final Network network, final TravelDisutility costFunction,
			final TravelTime timeFunction, final PreProcessDijkstra preProcessData, 
			final RoutingNetwork routingNetwork, final FastRouterDelegateFactory fastRouterFactory) {
		super(network, costFunction, timeFunction, preProcessData, routingNetwork, fastRouterFactory);
	}
	
	/*
	 * Re-use logic from super class and reset customDataManager.
	 */
	@Override
	public Path calcLeastCostPath(final Node fromNode, final Node toNode, final double startTime, final Person person, final Vehicle vehicle) {
		this.customDataManager.reset();

		return super.calcLeastCostPath(fromNode, toNode, startTime, person, vehicle);
	}
	
	/*
	 * Re-use logic from super class but add some calls to customDataManager.
	 */
	@Override
	protected boolean addToPendingNodes(final Link l, final Node n,
			final PseudoRemovePriorityQueue<Node> pendingNodes, final double currTime,
			final double currCost, final Node toNode) {

		this.customDataManager.initForLink(l);
		
		boolean value = super.addToPendingNodes(l, n, pendingNodes, currTime, currCost, toNode);
		if (value) this.customDataManager.storeTmpData();
		
		return value;
	}
}
