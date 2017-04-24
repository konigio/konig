package io.konig.content;

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
