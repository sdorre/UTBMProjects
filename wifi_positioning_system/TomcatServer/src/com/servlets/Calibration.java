package com.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.process.CalibrationProcess;

/**
 * Servlet that handles calibration Requests. It gives the request to a
 * procedure and takes the result of this procedure. It return a JSON Object
 * containing the status of the calibration: (0=OK, -1=FAILED)
 * 
 * response: {"calibration":0}
 */

public class Calibration extends HttpServlet {

	public static final String ATT_MESSAGES = "messages";
	public static final String VUE = "/WEB-INF/test.jsp";

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		CalibrationProcess calibration = new CalibrationProcess(request);

		response.setContentType("application/json");
		response.setHeader("Cache-Control", "nocache");
		response.setCharacterEncoding("utf-8");

		PrintWriter out = response.getWriter();

		out.print(calibration.getResponse().toString());

		// this.getServletContext().getRequestDispatcher(VUE).forward(request,
		// response);

		/*****************************************************************************
		 * 
		 * http://<host>:<port>/<calibrationServlet>?x=X&y=Y&map id=id
		 * 
		 * Double x = request.getParameter("x"); Double y =
		 * request.getParameter("y"); int map_ip =
		 * request.getParameter("map_id");
		 * 
		 * 
		 * query Access Points to RSSI measurments with : http://<AP
		 * IP>:<port>/get?mac=XX:XX:XX:XX:XX:XX
		 * 
		 * wait at least 500ms
		 * 
		 * Acces Points answer with JSON objects : {”ap”:
		 * ”ap:ap:ap:ap:ap:ap”,”rssi
		 * ”:[{”xx:xx:xx:xx:xx:xx”:”val”,”samples”:”nb”}]} or (for AP which are
		 * too far) {”ap”: ”ap:ap:ap:ap:ap:ap”,”rssi”:[ ] }
		 * 
		 * 
		 * I need to answer to the mobile device with : {”calibration”: ”try
		 * again”} or {”calibration”: ”ok”}
		 * 
		 * if the PS does not get enough RSSI values per AP, it asks the MD for
		 * more with "try again" We need at least 5 RSSI values for each access
		 * Point
		 * 
		 * 
		 * 
		 */
	}
}
