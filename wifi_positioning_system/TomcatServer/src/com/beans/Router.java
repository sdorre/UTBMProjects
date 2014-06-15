package com.beans;

/**
 * Represent an Access Point like it is stored in the database.
 */

public class Router {
	private int id;
	private String name;
	private String mac_addr;
	private String ip_addr;
	private String map_id;

	public Router(int id, String name, String mac_addr, String ip_addr,
			String map_id) {
		this.id = id;
		this.name = name;
		this.mac_addr = mac_addr;
		this.ip_addr = ip_addr;
		this.map_id = map_id;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getMac_addr() {
		return mac_addr;
	}

	public String getIp_addr() {
		return ip_addr;
	}

	public String getMap_id() {
		return map_id;
	}
}
