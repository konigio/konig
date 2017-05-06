package io.konig.content.gae;

import java.io.IOException;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.konig.content.AssetBundleKey;
import io.konig.content.ContentAccessException;

public class GaeZipBundleServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		ZipInputStream zipInput = new ZipInputStream(req.getInputStream());

		MemoryZipArchive zipArchive = new MemoryZipArchive(zipInput);
		
		AssetBundleKey bundleKey = ContentSystemUtil.bundleKey(req);

		GaeContentSystem contentSystem = new GaeContentSystem();
		try {
			contentSystem.saveBundle(bundleKey, zipArchive);
			resp.setStatus(HttpServletResponse.SC_OK);
			
		} catch (ContentAccessException e) {
			throw new ServletException(e);
		}
		
	}

}
