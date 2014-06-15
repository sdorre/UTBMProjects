package com.beans;

import java.util.Hashtable;

/**
 * Representation of the calibration Points It contains a Point Object and a
 * table with a measurement for each access point.
 */

public class CalibrationPoint {

	private Point point = null;
	private Hashtable<Integer, Double> rssi_values = null;

	public CalibrationPoint(Point p) {
		this.point = p;
		rssi_values = new Hashtable<Integer, Double>();
	}

	public Point getPoint() {
		return point;
	}

	public void setPoint(Point p) {
		this.point = p;
	}

	public Hashtable<Integer, Double> getRssiValues() {
		return rssi_values;
	}

	public double getValue(int id_ap) {
		return rssi_values.get(id_ap);
	}

	/**
	 * Insert a measurement in the table, identified by the ID of the AP.
	 * 
	 * @param id_ap
	 *            : ID the the accesspoint.
	 * @param value
	 *            : measurement performed by the AP
	 */
	public void addRssi(int id_ap, double value) {
		rssi_values.put(id_ap, value);
	}

}
