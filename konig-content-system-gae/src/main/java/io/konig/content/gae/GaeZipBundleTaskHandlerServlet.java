package io.konig.content.gae;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.json.JsonParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.pubsub.model.PubsubMessage;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import io.konig.content.AssetBundleKey;

public class GaeZipBundleTaskHandlerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(GaeZipBundleTaskHandlerServlet.class.getName());

	public final void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

		
		
		String bundleName = "undefined";
		String bundleVersion = "undefined";
		
		try {
		
			ServletInputStream input = req.getInputStream();
			JsonParser parser = JacksonFactory.getDefaultInstance().createJsonParser(input);
			parser.skipToKey("message");
			PubsubMessage message = parser.parseAndClose(PubsubMessage.class);
			
			
			Map<String,String> attributes = message.getAttributes();
			String eventType = attributes.get("eventType");
			
			if (!"OBJECT_FINALIZE".equals(eventType)) {
				resp.setStatus(HttpServletResponse.SC_OK);
				return;
			}
			
			String objectId = attributes.get("objectId");
			String bucketId = attributes.get("bucketId");
			
			int slash = objectId.indexOf('/');
			if (slash <=0 ) {
				logger.severe("In bucket '" + bucketId + "', object has invalid id: " + objectId);
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
			bundleName = objectId.substring(0, slash);
			bundleVersion = objectId.substring(slash+1); 
			
			Storage storage = StorageOptions.getDefaultInstance().getService();
			Bucket bucket = storage.get(bucketId);
			
			Blob blob = bucket.get(objectId);
			if (blob == null) {
				logger.severe("In bucket '" + bucketId + "', object not found: "  + objectId);
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
			
			try (
				InputStream archiveInput = Channels.newInputStream(blob.reader());
				ZipInputStream zipInput = new ZipInputStream(archiveInput);
			) {
				MemoryZipArchive archive = new MemoryZipArchive(zipInput);
				
				AssetBundleKey bundleKey = new AssetBundleKey(bundleName, bundleVersion);
				GaeContentSystem contentSystem = new GaeContentSystem();
				contentSystem.saveBundle(bundleKey, archive);
				blob.delete();
			}

			resp.setStatus(HttpServletResponse.SC_OK);
			
		} catch (Throwable e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			logger.log(Level.SEVERE, "Failed to process bundle " + bundleName + ":" + bundleVersion, e);

			// TODO: We should consider sending an email notification about the error 
		}
	}
}
