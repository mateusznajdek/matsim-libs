/* *********************************************************************** *
 * project: org.matsim.*
 * HbefaWarmEmissionFactor.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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
package org.matsim.contrib.emissions;


/**
 * @author benjamin
 *
 */
class HbefaWarmEmissionFactor {

	private double speed;
	private double warmEmissionFactor;

	/*package-private*/ HbefaWarmEmissionFactor(){
	}

	public double getSpeed() {
		return speed;
	}
	
	public void setSpeed(double speed) {
		this.speed = speed;
	}

	/*package-private*/ double getWarmEmissionFactor() {
		return this.warmEmissionFactor;
	}

	/*package-private*/ void setWarmEmissionFactor(double warmEmissionFactor) {
		this.warmEmissionFactor = warmEmissionFactor;
	}
}
