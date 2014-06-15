package com.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bdd.MeasurementInterface;

/**
 * Servlet to return a list of the calibration points in the database. It
 * displays a JSON Object containing the points for the map_id given in
 * parameter.
 * 
 * response: [{"id":31,"map_id":5,"y":0.7863001,"x":0.16044463},
 * {"id":32,"map_id":5,"y":0.43806538,"x":0.14177734}, ...
 * {"id":33,"map_id":5,"y":0.15964928,"x":0.35781866}]
 */

public class ListOfCalibration extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		System.out.println("ListOfCalibrationPoints - request received");

		// open a new interface with the database and get the map id.
		MeasurementInterface mInterface = new MeasurementInterface();
		int map_id = Integer.valueOf(request.getParameter("map_id"));

		// prepare a response.
		response.setContentType("application/json");
		response.setHeader("Cache-Control", "nocache");
		response.setCharacterEncoding("utf-8");

		PrintWriter out = response.getWriter();

		// send a JSON with the list of calibration points.
		out.print(mInterface.getJSONCalibration(map_id));

	}
}
