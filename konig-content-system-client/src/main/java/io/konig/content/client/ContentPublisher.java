package io.konig.content.client;

/*
 * #%L
 * Konig Content System, Client Library
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.Distribution.BucketOptions;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Bucket.BlobTargetOption;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import io.konig.content.Asset;
import io.konig.content.AssetBundle;
import io.konig.content.AssetBundleKey;
import io.konig.content.AssetMetadata;
import io.konig.content.CheckInBundleResponse;
import io.konig.content.ContentAccessException;
import io.konig.content.ContentSystemUtil;
import io.konig.content.EtagFactory;

public class ContentPublisher {
	private static final Logger logger = LoggerFactory.getLogger(ContentPublisher.class);
	
	private boolean compress = true;
	private File credentialsFile;

	public void publish(File baseDir, String baseURL, String bundleName, String bundleVersion) throws IOException, ContentAccessException {
		
		List<AssetMetadata> metaList = new ArrayList<>();
		AssetBundle bundle = new AssetBundle(bundleName, bundleVersion);
		AssetBundleKey bundleKey = bundle.getKey();
		bundle.setMetadataList(metaList);
		
		addAssets(bundle, baseDir, "/");

		logger.info("Publishing to: {}", bundleKey.url(baseURL));
		ContentSystemClient client = new ContentSystemClient(baseURL);
		CheckInBundleResponse response = client.checkInBundle(bundle);
		
		Collections.sort(response.getMissingAssets());
		
		List<String> requiredAssets = response.getMissingAssets();
		if (!requiredAssets.isEmpty()) {
			
			String editAddress = response.getEditServiceAddress();
			if (editAddress == null) {
				throw new ContentAccessException("Link header with 'rel=edit' not found in response from Content System client");
			}
			
			if (compress) {
				File zipFile = zipFile(baseDir, bundleKey);
				
				try (
						FileOutputStream fos = new FileOutputStream(zipFile);
						ZipOutputStream zos = new ZipOutputStream(fos)
				) {
					for (String path : requiredAssets) {
						
						String filePath = ContentSystemUtil.trimSlashes(path);
						addZipEntry(zos, baseDir, filePath);
					}
				}
				
				Bucket bucket = getBucket(editAddress);
				String objectId = bundleKey.getName() + "/" + bundleKey.getVersion();
				
				try (FileInputStream contentStream = new FileInputStream(zipFile)) {
					bucket.create(objectId, contentStream, "application/zip");
				}

//				String bundleURL = bundle.getKey().url(baseURL);
//				HttpPost post = new HttpPost(bundleURL);
//				FileEntity entity = new FileEntity(zipFile);
//				entity.setContentType("application/zip");
//				post.setEntity(entity);
//				CloseableHttpClient httpClient = HttpClients.createDefault();
//				CloseableHttpResponse httpResponse = httpClient.execute(post);
//				logger.info(httpResponse.getStatusLine().toString());
				
				
			} else {
		
				for (String path : requiredAssets) {
					
					String filePath = path.substring(1);
					File assetFile = new File(baseDir, filePath);
					Asset asset = loadAsset(bundleKey, path, assetFile);
					client.saveAsset(asset);
					logger.info(path);
				}
			}

		
		} else {
			logger.info("All assets are up-to-date");
		}
	}

	private Bucket getBucket(String editAddress) throws ContentAccessException {
		File credentialsFile = getCredentialsFile();
		if (credentialsFile == null) {
			throw new ContentAccessException("Google service account credentials not found.  "
					+ "Please set the credentialsFile property or the GOOGLE_APPLICATION_CREDENTIALS environment variable");
		}
		if (!credentialsFile.exists()) {
			throw new ContentAccessException("Credentials file does not exist: " + credentialsFile.getAbsolutePath());
		}
		
		try (FileInputStream credentialsStream = new FileInputStream(credentialsFile)) {

			Storage storage = StorageOptions.newBuilder()
				.setCredentials(ServiceAccountCredentials.fromStream(credentialsStream))
				.build().getService();
			
			int slash = editAddress.lastIndexOf('/');
			String bucketName = editAddress.substring(slash+1);
			return storage.get(bucketName);
			
		} catch (IOException e) {
			throw new ContentAccessException(e);
		}
	}

	private File getCredentialsFile() {
		
		File result = credentialsFile;
		if (result == null) {
			String value = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
			if (value != null) {
				result = new File(value);
			}
		}
		return result;
	}

	private void addZipEntry(ZipOutputStream zos, File baseDir, String filePath) throws IOException {

		logger.info(filePath);
		File file = new File(baseDir, filePath);
		FileInputStream fis = new FileInputStream(file);
		try {
			ZipEntry entry = new ZipEntry(filePath);
			zos.putNextEntry(entry);
			byte[] bytes = new byte[1024];
			int length;
			while ((length = fis.read(bytes)) >= 0) {
				zos.write(bytes, 0, length);
			}

			zos.closeEntry();
		} finally {
			close(fis, file.getAbsolutePath());
		}
		
	}

	private void close(Closeable stream, String name) {
		try {
			stream.close();
		} catch (IOException e) {
			logger.warn("Failed to close stream: {}", name);
		}
		
	}

	private File zipFile(File baseDir, AssetBundleKey bundleKey) {
		StringBuilder builder = new StringBuilder();
		builder.append(bundleKey.getName());
		builder.append('-');
		builder.append(bundleKey.getVersion());
		builder.append(".zip");
		File parent = baseDir.getParentFile();
		return new File(parent, builder.toString());
	}

	private Asset loadAsset(AssetBundleKey bundleKey, String assetPath, File assetFile) throws IOException {
		Path path = assetFile.toPath();
		String contentType = Files.probeContentType(path);
		byte[] body = Files.readAllBytes(path);
		String etag = EtagFactory.createEtag(body);
		
		AssetMetadata meta = new AssetMetadata();
		meta.setBundleKey(bundleKey);
		meta.setPath(assetPath);
		meta.setContentType(contentType);
		meta.setEtag(etag);
		meta.setPath(assetPath);
		
		return new Asset(meta, body);
	}

	private void addAssets(AssetBundle bundle, File dir, String parentPath) throws IOException {
		
		if (dir.isDirectory()) {
			List<AssetMetadata> metaList = bundle.getMetadataList();
			File[] array = dir.listFiles();
			for (File file : array) {

				StringBuilder pathBuilder = new StringBuilder();
				pathBuilder.append(parentPath);
				pathBuilder.append(file.getName());
				if (file.isDirectory()) {
					pathBuilder.append('/');
					addAssets(bundle, file, pathBuilder.toString());
				} else {
					Path path = file.toPath();
					byte[] data = Files.readAllBytes(path);
					String etag = EtagFactory.createEtag(data);

					AssetMetadata meta = new AssetMetadata();
					meta.setBundleKey(bundle.getKey());
					meta.setEtag(etag);
					meta.setPath(pathBuilder.toString());
					metaList.add(meta);					
				}
			}
		}
	}

}
