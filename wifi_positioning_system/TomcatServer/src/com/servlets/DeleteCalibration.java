package com.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bdd.MeasurementInterface;

/**
 * Servlet to delete calibration points in the database. It displays the number
 * of points deleted for the map_id given in parameter.
 */

public class DeleteCalibration extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		MeasurementInterface mInterface = new MeasurementInterface();
		int map_id = Integer.valueOf(request.getParameter("map_id"));

		response.setContentType("application/json");
		response.setHeader("Cache-Control", "nocache");
		response.setCharacterEncoding("utf-8");

		PrintWriter out = response.getWriter();

		out.print(mInterface.deleteCalibrationValues(map_id));
	}
}
