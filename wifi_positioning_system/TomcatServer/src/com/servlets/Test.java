package com.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Servlet that simulate an Access Point response. It returns the same JSON
 * Object that a real Router sends. Values of measurement are randomly
 * generated. response:
 * {"ap":"12:34:56:78:90:AB","rssi":[{"11:11:11:11:11:11":"-39.12818060876927"
 * ,"samples":"6"}]}
 */

public class Test extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("application/json");
		response.setHeader("Cache-Control", "nocache");
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();

		// finally output the json string
		JSONObject json = new JSONObject();

		double start = -95;
		double end = 0;
		double random = new Random().nextDouble();
		double result = start + (random * (end - start));

		try {
			json.put("ap", "12:34:56:78:90:AB");

			// put an "array"
			JSONArray arr = new JSONArray();

			JSONObject rssi_values1 = new JSONObject();
			rssi_values1.put("11:11:11:11:11:11", String.valueOf(result));
			rssi_values1.put("samples", "6");

			arr.put(rssi_values1);

			/*
			 * If we want more measurements in the same response, uncomment this
			 * section.
			 */
			/*
			 * JSONObject rssi_values = new JSONObject();
			 * rssi_values.put("22:22:22:22:22:22", String.valueOf(result));
			 * rssi_values.put("samples", "6");
			 * 
			 * arr.put(rssi_values);
			 * 
			 * JSONObject rssi_values2 = new JSONObject();
			 * rssi_values2.put("33:33:33:33:33:33", String.valueOf(result));
			 * rssi_values2.put("samples", "4");
			 * 
			 * arr.put(rssi_values2);
			 */
			json.put("rssi", arr);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		out.print(json.toString());
	}
}
