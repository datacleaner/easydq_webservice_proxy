package com.hi.easydq.proxy;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MockHttpServerServlet extends HttpServlet {

	private static final long serialVersionUID = -6874155816317896046L;
	
	private Logger logger = LoggerFactory.getLogger(MockHttpServerServlet.class);
	
    protected void doGet(HttpServletRequest request, 
                HttpServletResponse response)
        throws ServletException, IOException {
    	logger.info("HttpServletRequest received.");
    	
    	response.setContentType("text/html; charset=utf-8");
    	
        PrintWriter out = response.getWriter();
        out.write("<html><body><h1>My Servlet GET response");
        out.write("</h1></body></html>");
       
        out.flush();
        out.close();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, 
                HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, 
                HttpServletResponse response)
        throws ServletException, IOException {
    	logger.warn("doPost method should not be used!");
    }

}
