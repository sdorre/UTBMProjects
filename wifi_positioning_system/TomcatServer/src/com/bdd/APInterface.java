package com.bdd;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.beans.Router;

/**
 * This class is used to manage Access Points in the database. It uses a custom
 * Class to handle each entity : the class Router Requests needed are all
 * defined in static way.
 * 
 * It provides functions for: -Insert a new AP -Delete an AP -find all AP in
 * database
 */

public class APInterface {

	private static String selectAccessPoints = "SELECT id, name, mac_addr, ip_addr, map_id FROM accesspoint;";
	private static String deleteAccessPoint = "DELETE FROM accesspoint where id=?;";
	private static String insertAccessPoint = "INSERT INTO accesspoint (name, mac_addr, ip_addr, map_id) VALUES (?, ?, ?, ?);";

	private BDDInterface bddinterface = null;
	private Statement statement = null;
	private ResultSet result = null;

	public APInterface() {
		bddinterface = new BDDInterface();
		statement = bddinterface.getStatement();
	}

	/**
	 * Find all Access Points and create an Object for each entity. It clears
	 * the List if it's not empty and fill with the result of the request
	 * 
	 * @return a List of Router Objects found in database
	 */
	public ArrayList<Router> getAP() {

		ArrayList<Router> list = new ArrayList<Router>();

		try {
			result = statement.executeQuery(selectAccessPoints);

			while (result.next()) {

				Router r = new Router(result.getInt("id"),
						result.getString("name"), result.getString("mac_addr"),
						result.getString("ip_addr"), result.getString("map_id"));
				list.add(r);
			}

			result.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return list;
	}

	/**
	 * Function to delete the Access Points identified by the given ID in the
	 * database. It takes the defined request and completes it with the id
	 * parameter
	 * 
	 * @param id
	 *            is the ID of the Router we want to drop
	 * @return the number of record deleted in the database
	 */
	public int deleteAP(int id) {
		int result = 0;
		PreparedStatement req = null;

		try {
			req = bddinterface.getConnection().prepareStatement(
					deleteAccessPoint);
			req.setInt(1, id);
			result = req.executeUpdate();

			req.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Insert a new Access Points in the database. Parameters are tested with in
	 * order to save a correct AP. It takes the defined request and completes it
	 * with the parameters.
	 * 
	 * @param name
	 *            : name of the AP
	 * @param mac_addr
	 *            : MAC address of the AP
	 * @param ip_addr
	 *            : IP address
	 * @param map_id
	 *            : ID of the map in which the AP is used
	 * @return the number of new record in the database
	 */
	public int insertAP(String name, String mac_addr, String ip_addr, int map_id) {
		int result = -1;
		PreparedStatement req = null;

		try {
			if (name != null && !name.isEmpty() && mac_addr != null
					&& mac_addr.matches("^([0-9A-F]{2}:){5}[0-9A-F]{2}$")) {

				req = bddinterface.getConnection().prepareStatement(
						insertAccessPoint);
				req.setString(1, name);
				req.setString(2, mac_addr);
				req.setString(3, ip_addr);
				req.setInt(4, map_id);
				result = req.executeUpdate();

				req.close();
				System.out.println("AP - insertion new Router : name : " + name
						+ " MAC : " + mac_addr + " IP : " + ip_addr);
			} else {
				System.out
						.println("AP - no insertion - parameters don't match");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

}
