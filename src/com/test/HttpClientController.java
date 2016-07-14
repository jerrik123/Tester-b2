package com.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 */
public class HttpClientController extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.println("parameterMap: " + req.getParameterMap());
		String result = extractFromRequest(req);
		System.out.println("JSONStr: " + result);
		//req.getRequestDispatcher("/yj.html").forward(req, resp);
		PrintWriter pw = resp.getWriter();
		pw.write("parameterMap: " + req.getParameterMap());
		pw.write("\n");
		pw.write("JSONStr: " + result);
		pw.flush();
		pw.close();
	}

	private String extractFromRequest(HttpServletRequest req) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));
		String newLine = null;
		StringBuilder sb = new StringBuilder();
		while ((newLine = br.readLine()) != null) {
			sb.append(newLine);
			sb.append("\n");
		}
		return sb.toString();
	}

}
