package io.konig.content.client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.content.Asset;
import io.konig.content.AssetBundle;
import io.konig.content.AssetBundleKey;
import io.konig.content.AssetMetadata;
import io.konig.content.CheckInBundleResponse;
import io.konig.content.ContentAccessException;
import io.konig.content.EtagFactory;

public class ContentPublisher {
	private static final Logger logger = LoggerFactory.getLogger(ContentPublisher.class);

	public void publish(File baseDir, String baseURL, String bundleName, String bundleVersion) throws IOException, ContentAccessException {
		
		List<AssetMetadata> metaList = new ArrayList<>();
		AssetBundle bundle = new AssetBundle(bundleName, bundleVersion);
		AssetBundleKey bundleKey = bundle.getKey();
		bundle.setMetadataList(metaList);
		
		addAssets(bundle, baseDir, "/");

		logger.info("Publishing to: {}", bundleKey.url(baseURL));
		ContentSystemClient client = new ContentSystemClient(baseURL);
		CheckInBundleResponse response = client.checkInBundle(bundle);
		
		for (String path : response.getMissingAssets()) {
			
			String filePath = path.substring(1);
			File assetFile = new File(baseDir, filePath);
			Asset asset = loadAsset(bundleKey, path, assetFile);
			client.saveAsset(asset);
			logger.info(path);
		}
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
