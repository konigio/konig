package io.konig.content.gae;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GaeContentSystemServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private GaeCheckInBundleServlet checkInBundle = new GaeCheckInBundleServlet();
	private GaeAssetServlet asset = new GaeAssetServlet();

	protected void doPost(HttpServletRequest req,  HttpServletResponse resp)
	throws ServletException, IOException {
		
		if (isBundleRequest(req)) {
			checkInBundle.doPost(req, resp);
		} else {
			asset.doPost(req, resp);
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest req,   HttpServletResponse resp)
    throws ServletException, IOException {
		asset.doGet(req, resp);
	}
	
	private boolean isBundleRequest(HttpServletRequest req) {
		String pathInfo = req.getPathInfo();
		int bundleNameEnd = pathInfo.indexOf('/', 1);
		int versionEnd = pathInfo.indexOf('/', bundleNameEnd+1);
		
		return versionEnd<0 || pathInfo.length()==versionEnd+1;
	}
}
