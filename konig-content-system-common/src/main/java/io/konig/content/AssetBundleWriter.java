package io.konig.content;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class AssetBundleWriter {

	public void writeBundle(AssetBundle bundle, PrintWriter out) throws IOException {
		
		out.print("format=");
		out.print(FormatConstants.BUNDLE_1p0);
		out.print(',');
		out.print("name=");
		out.print(bundle.getKey().getName());
		out.print(',');
		out.print("version=");
		out.print(bundle.getKey().getVersion());
		out.print('\n');
		
		List<AssetMetadata> list = bundle.getMetadataList();
		
		for (AssetMetadata a : list) {
			writeMetadata(a, out);
		}
		
		
		
	}

	private void writeMetadata(AssetMetadata a, PrintWriter out) {
		
		out.print(a.getPath());
		out.print(',');
		out.print(a.getEtag());
		out.print('\n');
		
	}
}
