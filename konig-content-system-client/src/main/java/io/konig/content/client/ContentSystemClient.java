package io.konig.content.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import io.konig.content.Asset;
import io.konig.content.AssetBundle;
import io.konig.content.AssetBundleWriter;
import io.konig.content.AssetMetadata;
import io.konig.content.CheckInBundleResponse;
import io.konig.content.ContentAccessException;
import io.konig.content.ContentSystem;

public class ContentSystemClient implements ContentSystem {
	private String baseURL;
	

	public ContentSystemClient(String baseURL) {
		this.baseURL = baseURL;
	}

	@Override
	public CheckInBundleResponse checkInBundle(AssetBundle bundle) throws ContentAccessException {
		
		CheckInBundleResponse result = new CheckInBundleResponse();
		
		AssetBundleWriter bundleWriter = new AssetBundleWriter();
		StringWriter buffer = new StringWriter();
		PrintWriter out = new PrintWriter(buffer);
		try {
			bundleWriter.writeBundle(bundle, out);
			String text = buffer.toString();
			StringEntity entity = new StringEntity(text);

			String bundleURL = bundle.getKey().url(baseURL);
			
			HttpPost post = new HttpPost(bundleURL);
			post.setEntity(entity);
			CloseableHttpClient client = HttpClients.createDefault();
			CloseableHttpResponse response = client.execute(post);
			
			try {
				int status = response.getStatusLine().getStatusCode();
				if (status != 200 && status!=201) {
					StatusLine statusLine = response.getStatusLine();
					String msg = MessageFormat.format("Check-in failed at <{0}> with status {1} {2}", 
							bundleURL, statusLine.getStatusCode(), statusLine.getReasonPhrase());
					throw new ContentAccessException(msg);
				}
				InputStream input = response.getEntity().getContent();
				InputStreamReader reader = new InputStreamReader(input);
				BufferedReader lineReader = new BufferedReader(reader);
				List<String> pathList = new ArrayList<>();
				String line = null;
				while ((line=lineReader.readLine()) != null) {
					line = line.trim();
					if (line.length()>0) {
						pathList.add(line);
					}
				}
				
				result.setMissingAssets(pathList);
				
			} finally {
				response.close();
			}
			
			
		} catch (IOException e) {
			throw new ContentAccessException(e);
		} finally {
			out.close();
		}
		
		return result;
	}

	@Override
	public int saveMetadata(AssetMetadata metadata) throws ContentAccessException {
		throw new ContentAccessException("saveMetadata method not supported by this client");
	}

	@Override
	public AssetMetadata getMetadata(String path) throws ContentAccessException {
		throw new ContentAccessException("getMetadata method not supported by this client");
	}

	@Override
	public int saveAsset(Asset asset) throws ContentAccessException {
		int status = 0;
		AssetMetadata meta = asset.getMetadata();
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost(meta.getBundleKey().assetURL(baseURL, meta));
		if (meta.getContentType() != null) {
			post.setHeader("Content-Type", meta.getContentType());
		}
		post.setEntity(new ByteArrayEntity(asset.getBody()));
		try {
			CloseableHttpResponse response = client.execute(post);
			try {
				status = response.getStatusLine().getStatusCode();
			} finally {
				response.close();
			}
		} catch (IOException e) {
			throw new ContentAccessException(e);
		}
		
		return status;
	}

	@Override
	public Asset getAsset(String path) throws ContentAccessException {
		throw new ContentAccessException("getAsset method is not supported by this client");
	}

}
