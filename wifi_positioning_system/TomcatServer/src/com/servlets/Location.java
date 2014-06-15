package com.servlets;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.process.LocationProcess;

/**
 * Servlet that handles the location request. It displays a JSON Object
 * containing the status of the location (0 = OK, -1=FAILED) if the location
 * succeeds, the JSON includes also the ID of the map and x and y coordinates
 * where the mobile device is located.
 * 
 * example of response: {"location":0, "id":5, "y":0.5017199,"x":0.40144593}
 */

public class Location extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		LocationProcess location = new LocationProcess(request);

		response.setContentType("application/json");
		response.setHeader("Cache-Control", "nocache");
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();

		out.print(location.getResponse());
	}
}