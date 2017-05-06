package io.konig.content.gae;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.konig.content.Asset;
import io.konig.content.AssetBundleKey;
import io.konig.content.AssetMetadata;
import io.konig.content.ContentAccessException;
import io.konig.content.EtagFactory;

public class GaeAssetServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doPost(HttpServletRequest req,   HttpServletResponse resp)
    throws ServletException, IOException {
		
		try {
			String pathInfo = req.getPathInfo();
			
			String contentType = req.getContentType();
			byte[] body = toByteArray(req.getInputStream());
			String etag = EtagFactory.createEtag(body);
			AssetMetadata metadata = ContentSystemUtil.parsePathInfo(pathInfo);
			metadata.setEtag(etag);
			metadata.setContentType(contentType);
			
			Asset asset = new Asset(metadata, body);
			
			GaeContentSystem contentSystem = new GaeContentSystem();
			int statusCode = contentSystem.saveAsset(asset);
			
			resp.setStatus(statusCode);
			
			
		} catch (ContentAccessException e) {
			throw new ServletException(e);
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest req,   HttpServletResponse resp)
    throws ServletException, IOException {
		
		String pathInfo = req.getPathInfo();
		
		GaeContentSystem contentSystem = new GaeContentSystem();
		try {
			Asset asset = contentSystem.getAsset(pathInfo);
			
			if (asset == null) {
				resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			
			byte[] body = asset.getBody();
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setContentLength(body.length);
			resp.setContentType(asset.getMetadata().getContentType());
			resp.setHeader("ETag", asset.getMetadata().getEtag());
			resp.getOutputStream().write(body);
			resp.flushBuffer();
			
		} catch (ContentAccessException e) {
			throw new ServletException(e);
		}
	}
	
	private byte[] toByteArray(InputStream input) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		byte[] data = new byte[16384];

		while ((nRead = input.read(data, 0, data.length)) != -1) {
		  buffer.write(data, 0, nRead);
		}

		buffer.flush();

		return buffer.toByteArray();
	}

}
