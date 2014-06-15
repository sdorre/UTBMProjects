package com.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.process.UploadProcess;

/**
 * Servlet to Upload a Map in the database. It displays a HTML form and give the
 * request to a Procedure Class that will do the work. When the work is done, it
 * takes the result code and includes it in the page shown to the user.
 */

public class Upload extends HttpServlet {

	public static final String VUE = "/WEB-INF/upload.jsp";
	public int result = 0;

	/**
	 * Handle GET requests: just show the page.
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		showPage(request, response);
	}

	/**
	 * handle POST requests: pass the request to a Procedure and take the result
	 * code: 0 - no new Map inserted 1 - new Map created
	 * 
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		UploadProcess upload = new UploadProcess(request);
		result = upload.getResult();
		showPage(request, response);
	}

	/**
	 * Show the page defined with the String "VUE". It includes also "result"
	 * parameter.
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void showPage(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		request.setAttribute("result", result);
		getServletContext().getRequestDispatcher(VUE)
				.forward(request, response);
	}
}