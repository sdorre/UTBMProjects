package com.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bdd.APInterface;
import com.bdd.MapsInterface;
import com.beans.Router;

/**
 * Servlet to manage access points in the database. It displays a HTML form and
 * give the request to a Procedure Class that will do the work. When the work is
 * done, it refreshes the page and show ne informations to the user.
 */

public class AccessPoint extends HttpServlet {

	private APInterface ap = new APInterface();
	public static final String VUE = "/WEB-INF/accesspoint.jsp";

	/**
	 * handles GET request: if there is a parameter, it means that the user ask
	 * for a deletion.
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String id = request.getParameter("id");
		if (id != null) {
			System.out
					.println("SERVLET - clicked for deletion ! "
							+ "result : "
							+ ap.deleteAP(Integer.parseInt(request
									.getParameter("id"))));
			request.removeAttribute("id");
		}
		showRouter(request, response);
	}

	/**
	 * handles POST request: the user wants to create a new access point in the
	 * database.
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		/* find values given in the form and create new DB entry with. */
		String name = request.getParameter("name");
		String mac_addr = request.getParameter("mac_addr");
		String ip_addr = request.getParameter("ip_addr");
		String map_id = request.getParameter("map_id");

		ap.insertAP(name, mac_addr, ip_addr, Integer.valueOf(map_id));

		showRouter(request, response);
	}

	/**
	 * Show the page containing a list of the existing access points and a HTML
	 * form that creates new Router.
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void showRouter(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		ArrayList<Router> router = ap.getAP();
		HashMap<Integer, String> map_list = new MapsInterface().getMapIds();

		request.setAttribute("router", router);
		request.setAttribute("map_list", map_list);
		request.setAttribute("url", request.getRequestURL());

		this.getServletContext().getRequestDispatcher(VUE)
				.forward(request, response);
	}
}