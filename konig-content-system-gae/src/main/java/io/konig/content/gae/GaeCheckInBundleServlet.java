package io.konig.content.gae;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.konig.content.AssetBundle;
import io.konig.content.AssetBundleReader;
import io.konig.content.CheckInBundleResponse;
import io.konig.content.ContentAccessException;

public class GaeCheckInBundleServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String STORAGE_BUCKET_NAME = "konig.GaeCheckInBundleServlet.storageBucketName";
	
	private String editLink;

	
	private String editLink() throws ServletException {
		if (editLink == null) {
			String bucketName = System.getProperty(STORAGE_BUCKET_NAME);
			if (bucketName == null) {
				throw new ServletException("System property not defined: " + STORAGE_BUCKET_NAME);
			}
			StringBuilder builder = new StringBuilder();
			builder.append("<gs://");
			builder.append(bucketName);
			builder.append(">; rel=edit");
			editLink = builder.toString();
		}
		return editLink;
	}
	
	protected void doPost(HttpServletRequest req,  HttpServletResponse resp)
	throws ServletException, IOException {
		
		AssetBundleReader bundleReader = new AssetBundleReader();
		resp.setHeader("Link", editLink());
		Reader reader = req.getReader();
		try {
			AssetBundle bundle = bundleReader.readBundle(reader);
			GaeContentSystem contentSystem = new GaeContentSystem();
			CheckInBundleResponse response = contentSystem.checkInBundle(bundle);
			resp.setContentType("text/plain");
			PrintWriter writer = resp.getWriter();
			for (String path : response.getMissingAssets()) {
				writer.println(path);
			}
			resp.flushBuffer();
		} catch (ContentAccessException e) {
			throw new ServletException(e);
		}
	}

}
