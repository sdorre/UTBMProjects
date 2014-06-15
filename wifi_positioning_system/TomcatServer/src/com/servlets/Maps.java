package com.servlets;

import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bdd.MapsInterface;

/**
 * Servlet to show maps available in the database. It displays a JSON Object
 * containing all maps available. if the user send an ID in parameter, it shows
 * the image file of the maps. example response:
 * {"1":["my house3",1306,999,20.5,15.0], "2":["UTBM",1227,811,151.20,43],
 * "3":["my house4",935,454,15.3,7.4]}
 */

public class Maps extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		MapsInterface maps = new MapsInterface();

		String id = request.getParameter("id");
		if (id != null) {
			response.setContentType("image/jpeg");

			// show the map image with the ID="id"
			BufferedImage bi = maps.getMap(id);
			OutputStream out = response.getOutputStream();
			ImageIO.write(bi, "jpg", out);
			out.close();
		} else {
			response.setContentType("application/json");
			response.setHeader("Cache-Control", "nocache");
			response.setCharacterEncoding("utf-8");
			PrintWriter out = response.getWriter();

			// finally output the json string containing a list with all the
			// available maps
			out.print(maps.getMaps());
		}
	}

}