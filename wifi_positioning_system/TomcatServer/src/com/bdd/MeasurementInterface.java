package com.bdd;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;

import org.json.JSONArray;

import com.beans.CalibrationPoint;
import com.beans.Point;

/**
 * Class to manage measurements performed by the Access Points. It provide a set
 * of function for get, insert and delete in tables : location, rssi, and
 * temprssi all the request needed are declared in a static way.
 */

public class MeasurementInterface {

	private static String insertLoc = "INSERT INTO location ( x, y, map_id) VALUES (?, ?, ?);";

	private static String deleteTempValues = "DELETE FROM temprssi WHERE client_mac = ?;";
	private static String insertTemp = "INSERT INTO temprssi ( ap_id, client_mac, avg_val) VALUES (?, ?, ?);";
	private static String selectNbTempValues = "SELECT count(*) FROM temprssi WHERE client_mac=?;";
	private static String selectTempValues = "SELECT ap_id, client_mac, avg_val FROM temprssi WHERE client_mac=?;";

	private static String deleteRssiValues = "DELETE FROM rssi WHERE exists(SELECT 1 FROM location WHERE id_loc=location.id AND map_id=?);";
	private static String insertCalibPoint = "INSERT INTO rssi ( id_loc, id_ap, avg_val ) VALUES ( ?, ?, ?);";
	private static String selectCountCalibPoint = "SELECT count(id_ap) FROM rssi WHERE id_loc = ?;";
	private static String selectCalibPoints = "SELECT id_loc, x, y, map_id, id_ap, avg_val FROM rssi, location "
			+ "WHERE rssi.id_loc = location.id AND map_id = ? ORDER BY id_loc ASC;";

	private BDDInterface bddinterface;
	private ResultSet result;

	public MeasurementInterface() {
		bddinterface = new BDDInterface();
	}

	/**
	 * Insert a new Point in the database.
	 * 
	 * @param p
	 *            : a Point Object
	 * @return 0 if insertion failed, the id of the point created else.
	 */
	public int insertLocation(Point p) {

		int statut = 0;
		try {
			PreparedStatement preparedStatement = bddinterface.getConnection()
					.prepareStatement(insertLoc,
							Statement.RETURN_GENERATED_KEYS);
			preparedStatement.setDouble(1, p.getX());
			preparedStatement.setDouble(2, p.getY());
			preparedStatement.setInt(3, p.getMapId());

			statut = preparedStatement.executeUpdate();

			// if the operation return 0, insertion failed so, we return 0
			// else we retrieve the id generated for this new Point
			if (statut != 0) {
				result = preparedStatement.getGeneratedKeys();
				while (result.next()) {
					statut = result.getInt(1);
				}
			}

			result.close();
			preparedStatement.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return statut;
	}

	/**
	 * Query the database in order to know if the Point passed in parameter is
	 * already in the database or not.
	 * 
	 * @param p
	 *            : Point we want to know if it exists or not
	 * @return 0 if the Point doesn't exist, the id of the Point else.
	 */
	public int locationExist(Point p) {
		int id = 0;
		try {
			result = bddinterface.getStatement().executeQuery(
					"SELECT id FROM location WHERE x=" + p.getX() + " AND y="
							+ p.getY() + " AND map_id=" + p.getMapId());
			if (result.next()) {
				id = result.getInt("id");
			}
			;

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return id;
	}

	/**
	 * Insert a new entity in the table temprssi of the database. Each record is
	 * defined with the ID of the Access Point, the MAC address of the mobile
	 * device and the Measurement of the signal received by the AP.
	 * 
	 * @param id_ap
	 *            : the ID of the access point.
	 * @param mac_mobile
	 *            : MAC address of the mobile device
	 * @param value
	 *            : power of the signal received by the AP
	 * @return the number of new record in the database.
	 */
	public int insertTempMeasure(int id_ap, String mac_mobile, String value) {

		int status = 0;
		PreparedStatement preparedStatement;

		try {
			preparedStatement = bddinterface.getConnection().prepareStatement(
					insertTemp);
			preparedStatement.setInt(1, id_ap);
			preparedStatement.setString(2, mac_mobile);
			preparedStatement.setDouble(3, Double.parseDouble(value));

			status = preparedStatement.executeUpdate();

			System.out.println("Measure - Insert new value for MAC : "
					+ mac_mobile + " values = " + value);

			preparedStatement.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return status;
	}

	/**
	 * Insert Calibration Points in the table rssi of the database. Each record
	 * is defined with the ID of the Point, the ID of the Access Point and the
	 * Measurement of the signal received by the AP. calibration points are
	 * defined with values stored in the temprssi table. Values stored in the
	 * TempRssi Table are deleted.
	 * 
	 * @param id_loc
	 *            : the ID of the mobile device's location
	 * @param mac_mobile
	 *            : the MAC address of the mobile device
	 * @return the number of calibration points inserted
	 */
	public int insertCalibrationPoint(int id_loc, String mac_mobile) {

		int nb = 0;

		// get values stored in the temprssi table
		result = getTempValues(mac_mobile);
		PreparedStatement preparedStatement;

		try {
			preparedStatement = bddinterface.getConnection().prepareStatement(
					insertCalibPoint);

			// while there are values in the temp table, we insert a new
			// calibration point
			while (result.next()) {
				preparedStatement.setInt(1, id_loc);
				preparedStatement.setInt(2, result.getInt("ap_id"));
				preparedStatement.setDouble(3, result.getDouble("avg_val"));

				nb += preparedStatement.executeUpdate();
			}

			result.close();
			preparedStatement.close();

			// clean the values in the temp table.
			deleteTempValues(mac_mobile);

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return nb;
	}

	/**
	 * Get the measurement sent by all the access points and stored in the
	 * temprssi table. It collects values for a given MAC address.
	 * 
	 * @param mac_mobile
	 *            : MAC address of the mobile device for which we want the
	 *            measurements.
	 * @return a ResultSet containing the data stored in the database.
	 */
	public ResultSet getTempValues(String mac_mobile) {

		PreparedStatement preparedStatement;

		try {
			preparedStatement = bddinterface.getConnection().prepareStatement(
					selectTempValues);
			preparedStatement.setString(1, mac_mobile);

			result = preparedStatement.executeQuery();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Clear the table temprssi. It drops data related with the MAC address
	 * given in parameter.
	 * 
	 * @param mac_mobile
	 *            : MAC address for which it drops data
	 */
	public void deleteTempValues(String mac_mobile) {

		PreparedStatement preparedStatement;

		try {
			preparedStatement = bddinterface.getConnection().prepareStatement(
					deleteTempValues);
			preparedStatement.setString(1, mac_mobile);

			preparedStatement.execute();

			preparedStatement.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Drop all the calibration points for a given map.
	 * 
	 * @param map_id
	 *            : ID of the map containing the calibration.
	 * @return the number of points deleted, -1 if it failed
	 */
	public int deleteCalibrationValues(int map_id) {

		PreparedStatement preparedStatement;
		int nb = -1;
		try {
			preparedStatement = bddinterface.getConnection().prepareStatement(
					deleteRssiValues);
			preparedStatement.setInt(1, map_id);

			nb = preparedStatement.executeUpdate();

			preparedStatement.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return nb;
	}

	/**
	 * Ask database to know how many values are stored in temprssi table for a
	 * given MAC address.
	 * 
	 * @param mac_mobile
	 *            : MAC address of the mobile device
	 * @return the number of values found in the database.
	 */
	public int getNumberOfTempValues(String mac_mobile) {

		PreparedStatement preparedStatement;
		int nb = 0;
		try {
			preparedStatement = bddinterface.getConnection().prepareStatement(
					selectNbTempValues);
			preparedStatement.setString(1, mac_mobile);

			result = preparedStatement.executeQuery();
			result.next();
			nb = result.getInt("count");

			preparedStatement.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return nb;
	}

	/**
	 * Get the number of calibration points for a given Location. There are one
	 * calibration point for each Router.
	 * 
	 * @param point_id
	 *            : ID of the Location
	 * @return the number of calibration points found
	 */
	public int getCountCalibrationPoints(int point_id) {

		int nb = 0;
		PreparedStatement preparedStatement;

		try {
			preparedStatement = bddinterface.getConnection().prepareStatement(
					selectCountCalibPoint);
			preparedStatement.setInt(1, point_id);

			result = preparedStatement.executeQuery();
			result.next();
			nb = result.getInt("count");

			preparedStatement.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return nb;
	}

	/**
	 * Retrieve all the Calibration Points inserted in the given Map. Create a
	 * CalibrationPoint object for each entity in the database.
	 * 
	 * @param map_id
	 *            : ID of the map
	 * @return a List of CalibrationPoint Objects
	 */
	public ArrayList<CalibrationPoint> getCalibrationPoints(int map_id) {

		ArrayList<CalibrationPoint> list = new ArrayList<CalibrationPoint>();
		CalibrationPoint calibPoint = null;

		PreparedStatement preparedStatement;
		int id = 0;

		try {
			preparedStatement = bddinterface.getConnection().prepareStatement(
					selectCalibPoints);
			preparedStatement.setInt(1, map_id);
			result = preparedStatement.executeQuery();

			// within a calibration point, there are one measurement for each
			// access point.
			while (result.next()) {
				if (id != result.getInt("id_loc")) {
					// if the id is different, the calibration point is
					// complete.
					// Add to the list the previous point and create a new.
					if (calibPoint != null) {
						list.add(calibPoint);
					}

					id = result.getInt("id_loc");
					Point p = new Point(result.getInt("id_loc"),
							result.getDouble("x"), result.getDouble("y"),
							result.getInt("map_id"));
					calibPoint = new CalibrationPoint(p);
					calibPoint.addRssi(result.getInt("id_ap"),
							result.getDouble("avg_val"));

				} else {
					// the id is the same, it just needs to add a value in the
					// calibration point.
					calibPoint.addRssi(result.getInt("id_ap"),
							result.getDouble("avg_val"));
				}
			}

			// add the last calibration point
			if (calibPoint != null) {
				list.add(calibPoint);
			}

			preparedStatement.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * Retrieve all the Calibration Points inserted in the given Map. Fill in a
	 * JSON with each entity in the database. Use the property toJSON() on each
	 * element of the list given by getCalibrationPoints(map_id) function.
	 * 
	 * @param map_id
	 *            : ID of the map
	 * @return a JSON containing all calibration Points
	 */
	public JSONArray getJSONCalibration(int map_id) {
		JSONArray list = new JSONArray();
		ArrayList<CalibrationPoint> calibPoints = this
				.getCalibrationPoints(map_id);

		if (calibPoints.isEmpty()) {
			return list;
		} else {
			for (CalibrationPoint point : calibPoints) {
				list.put(point.getPoint().toJSON());
			}
			return list;
		}
	}

	/**
	 * Collect all measurement stored in the table temprssi for a given MAC
	 * address. Create a Table with this structure: key: ID of the AP value:
	 * value of the measurement
	 * 
	 * @param mac_mobile
	 *            : MAC address of the mobile device
	 * @return the table of measurements.
	 */
	public Hashtable<Integer, Double> getMeasure(String mac_mobile) {
		Hashtable<Integer, Double> measure = new Hashtable<Integer, Double>();
		PreparedStatement preparedStatement;

		try {
			preparedStatement = bddinterface.getConnection().prepareStatement(
					selectTempValues);
			preparedStatement.setString(1, mac_mobile);
			result = preparedStatement.executeQuery();

			while (result.next()) {
				measure.put(result.getInt("ap_id"), result.getDouble("avg_val"));
			}
			preparedStatement.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return measure;
	}

	/**
	 * Close the Connection with the database.
	 */
	public void close() {

		try {
			if (result != null) {
				result.close();
			}
			bddinterface.closeDataBase();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
