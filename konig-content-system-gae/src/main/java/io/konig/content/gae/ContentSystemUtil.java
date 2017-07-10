package io.konig.content.gae;

/*
 * #%L
 * Konig Content System, Google App Engine implementation
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
