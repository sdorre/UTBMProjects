package com.bdd;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class is used to manage Maps in the database. Requests needed are all
 * defined in static way.
 * 
 * It provides functions for: -Send a JSON containing all maps in the database
 * -Send the image of a map -Send a Map with the ID and the description of a map
 * -Insert a new Map
 */

public class MapsInterface {

	private static String selectMaps = "SELECT * FROM maps;";
	private static String selectContentMaps = "SELECT content FROM maps WHERE id=?;";
	private static String insertMap = "INSERT INTO maps(description, px_width, px_height, meters_width, meters_height, content) "
			+ "VALUES(?, ?, ?, ?, ?, decode(?, 'base64'));";

	private BDDInterface bddinterface = null;
	private Statement statement = null;
	private ResultSet result = null;

	/**
	 * Create a Connection to the database and initialize a new Statement to
	 * perform requests.
	 */
	public MapsInterface() {
		bddinterface = new BDDInterface();
		statement = bddinterface.getStatement();
	}

	/**
	 * Query the database for the mobile device. It get all maps available. And
	 * create an Array with the dfferent information for each map. Each array is
	 * identified by the id of the map.
	 * 
	 * @return JSONObject containing all maps in the database.
	 */
	public JSONObject getMaps() {

		JSONObject json = new JSONObject();

		try {
			result = statement.executeQuery(selectMaps);

			while (result.next()) {

				String id = result.getString("id");
				String descr = result.getString("description");
				int px_w = result.getInt("px_width");
				int px_h = result.getInt("px_height");
				double meters_w = result.getDouble("meters_width");
				double meters_h = result.getDouble("meters_height");
				// String content = resultat.getString("content");

				try {
					JSONArray arr = new JSONArray();
					arr.put(descr);
					arr.put(px_w);
					arr.put(px_h);
					arr.put(meters_w);
					arr.put(meters_h);
					// arr.put(content);

					json.put(id, arr);
				} catch (JSONException e) {
					e.printStackTrace();
				}

				System.out.println("MAPS - Données retournées : id = " + id
						+ ", description = " + descr + ", px_width = " + px_w
						+ ", pw_height = " + px_h + " meters_width = "
						+ meters_w + " meters_height = " + meters_h + ".");
			}
			result.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		return json;
	}

	/**
	 * Query the database to get one map.
	 * 
	 * @param id
	 *           id of the map you want
	 * @return the image of the wanted map
	 */
	public BufferedImage getMap(String id) {

		BufferedImage img = null;
		PreparedStatement req = null;
		try {

			req = bddinterface.getConnection().prepareStatement(
					selectContentMaps);
			req.setInt(1, Integer.valueOf(id));

			result = req.executeQuery();

			if (result.next()) {
				InputStream is = result.getBinaryStream(1);
				img = ImageIO.read(is);
			}

			result.close();
			req.close();

		} catch (SQLException e) {
			System.out.println("MAPS - Erreur lors de la connexion ; "
					+ e.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return img;
	}

	/**
	 * Function to get id and description of each map in the database. key = id,
	 * value = description of the map
	 * 
	 * @return a HashMap containing the data.
	 */
	public HashMap<Integer, String> getMapIds() {

		HashMap<Integer, String> list = new HashMap<Integer, String>();

		try {
			result = statement.executeQuery(selectMaps);

			while (result.next()) {
				list.put(result.getInt("id"), result.getString("description"));
			}
			result.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}

	/**
	 * Insert a new map in the database.
	 * 
	 * @param description
	 *            of the map
	 * @param px_w
	 *            (int) is the width in pixel
	 * @param px_h
	 *            (int) is the height in pixel
	 * @param meters_w
	 *            (Double) is the width in meters
	 * @param meters_h
	 *            (Double) is the height in meters
	 * @param content
	 *            (String) image encoded in base64
	 * @return -1:insert failed, else the number of new record in the database.
	 */
	public int insertMaps(String description, int px_w, int px_h,
			double meters_w, double meters_h, String content) {

		int result = -1;
		PreparedStatement req = null;

		try {
			req = bddinterface.getConnection().prepareStatement(insertMap);

			// define the different parameters in each "?" of the request.
			req.setString(1, description);
			req.setInt(2, px_w);
			req.setInt(3, px_h);
			req.setDouble(4, meters_w);
			req.setDouble(5, meters_h);
			req.setString(6, content);

			result = req.executeUpdate();

			req.close();
			if (result == 1) {
				System.out.println("MAPS - insertion new Map : name : "
						+ description + " meters_w : " + meters_w
						+ " meters_h : " + meters_h);
			} else {
				System.out.println("MAPS - no new maps.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

}
