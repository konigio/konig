package io.konig.content.gae;

import javax.servlet.http.HttpServletRequest;

import io.konig.content.AssetBundleKey;
import io.konig.content.AssetMetadata;

public class ContentSystemUtil {

	public static AssetBundleKey bundleKey(HttpServletRequest req) {
		String path = req.getPathInfo();
		
		int bundleNameEnd = path.indexOf('/', 1);
		int bundleVersionEnd = path.indexOf('/', bundleNameEnd+1);

		String bundleName = path.substring(1, bundleNameEnd);
		String bundleVersion = path.substring(bundleNameEnd+1, bundleVersionEnd);
		
		return new AssetBundleKey(bundleName, bundleVersion);
	}
	
	public static AssetMetadata parsePathInfo(String pathInfo) {
		
		int bundleNameEnd = pathInfo.indexOf('/', 1);
		int bundleVersionEnd = pathInfo.indexOf('/', bundleNameEnd+1);

		String bundleName = pathInfo.substring(1, bundleNameEnd);
		String bundleVersion = pathInfo.substring(bundleNameEnd+1, bundleVersionEnd);
		String path = pathInfo.substring(bundleVersionEnd+1);
		
		AssetBundleKey bundleKey = new AssetBundleKey(bundleName, bundleVersion);
		AssetMetadata metadata = new AssetMetadata();
		metadata.setBundleKey(bundleKey);
		metadata.setPath(path);
		
		return metadata;
	}
	
}
