package io.konig.content.gae;

import javax.servlet.http.HttpServletRequest;

import io.konig.content.AssetBundleKey;

public class ContentSystemUtil {

	public static AssetBundleKey bundleKey(HttpServletRequest req) {
		String path = req.getPathInfo();
		
		int bundleNameEnd = path.indexOf('/', 1);
		int bundleVersionEnd = path.indexOf('/', bundleNameEnd+1);

		String bundleName = path.substring(1, bundleNameEnd);
		String bundleVersion = path.substring(bundleNameEnd+1, bundleVersionEnd);
		
		return new AssetBundleKey(bundleName, bundleVersion);
	}
	
}
