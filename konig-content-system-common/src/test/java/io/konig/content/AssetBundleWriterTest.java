package io.konig.content;

/*
 * #%L
 * Konig Content System, Shared Library
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


import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class AssetBundleWriterTest {

	@Test
	public void test() throws Exception {
		
		
		AssetBundleKey bundleKey = new AssetBundleKey("foo", "1.0");
		AssetBundle bundle = new AssetBundle(bundleKey);
		List<AssetMetadata> list = new ArrayList<>();
		bundle.setMetadataList(list);
		
		list.add(asset("alpha", "1"));
		list.add(asset("beta", "2"));
		
		StringWriter buffer = new StringWriter();
		PrintWriter out = new PrintWriter(buffer);
		
		AssetBundleWriter bundleWriter = new AssetBundleWriter();
		bundleWriter.writeBundle(bundle, out);
		
		out.flush();
		String actual = buffer.toString();
		String expected = 
			"format=Bundle-1.0,name=foo,version=1.0\n" + 
			"alpha,1\n" + 
			"beta,2\n";
		
		assertEquals(expected, actual);
	}

	private AssetMetadata asset(String path, String etag) {
		AssetMetadata metadata = new AssetMetadata();
		metadata.setPath(path);
		metadata.setEtag(etag);
		return metadata;
	}

}
