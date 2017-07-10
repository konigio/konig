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


import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.List;

import org.junit.Test;

public class AssetBundleReaderTest {

	@Test
	public void test() throws Exception {

		String data = 
			"format=Bundle-1.0,name=foo,version=1.0\n" + 
			"alpha,1\n" + 
			"beta,2\n";
		
		StringReader input = new StringReader(data);
		
		AssetBundleReader reader = new AssetBundleReader();
		AssetBundle bundle = reader.readBundle(input);
		
		assertEquals("foo", bundle.getKey().getName());
		assertEquals("1.0", bundle.getKey().getVersion());
		
		List<AssetMetadata> list = bundle.getMetadataList();
		assertEquals(2, list.size());
		AssetMetadata alpha = list.get(0);
		assertEquals("alpha", alpha.getPath());
		assertEquals("1", alpha.getEtag());
		
		AssetMetadata beta = list.get(1);
		assertEquals("beta", beta.getPath());
		assertEquals("2", beta.getEtag());
	}

}
