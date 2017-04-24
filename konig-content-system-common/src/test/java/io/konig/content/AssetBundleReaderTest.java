package io.konig.content;

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
