package com.beans;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Give a representation of a Location in the database. Composed by an ID, 2
 * coordinates and the ID of the maps.
 */

public class Point {
	private int id;
	private double x;
	private double y;
	private int map_id;

	public Point(int id, double x, double y, int map_id) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.map_id = map_id;
	}

	public Point(int id, String x, String y, String map_id) {
		this.id = id;
		this.x = Double.parseDouble(x);
		this.y = Double.parseDouble(y);
		this.map_id = Integer.parseInt(map_id);
	}

	public int getId() {
		return id;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public int getMapId() {
		return map_id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}

	public void setMapId(int map_id) {
		this.map_id = map_id;
	}

	public boolean hasAnID() {
		if (id != 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Create a JSONObject with the Point.
	 * 
	 * @return the JSON that represent the Point.
	 */
	public JSONObject toJSON() {
		JSONObject obj = new JSONObject();

		try {
			obj.put("id", id);
			obj.put("x", x);
			obj.put("y", y);
			obj.put("map_id", map_id);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;
	}
}
