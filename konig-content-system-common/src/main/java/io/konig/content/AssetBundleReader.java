package io.konig.content;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class AssetBundleReader {

	public AssetBundle readBundle(Reader reader) throws ContentAccessException, IOException {
		List<AssetMetadata> list = new ArrayList<>();
		
		BufferedReader buffer = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
		
		
		AssetBundleKey key = readKey(buffer);
		
		String line = null;
		while ( (line=buffer.readLine()) != null) {
			int comma = line.indexOf(',');
			if (comma > 0) {
				String path = line.substring(0, comma).trim();
				String etag = line.substring(comma+1).trim();
				AssetMetadata asset = new AssetMetadata();
				asset.setBundleKey(key);
				asset.setEtag(etag);
				asset.setPath(path);
				
				list.add(asset);
			}
		}
		
		AssetBundle bundle = new AssetBundle(key);
		bundle.setMetadataList(list);
		
		return bundle;
	}

	private AssetBundleKey readKey(BufferedReader reader) throws IOException, ContentAccessException {

		String name = null;
		String version = null;
		
		String line = reader.readLine();
		String[] parts = line.split(",");
		
		if (parts.length != 3) {
			throw new ContentAccessException("Invalid format");
		}
		
		for (String data : parts) {
			NameValuePair pair = new NameValuePair(data);
			switch (pair.getName()) {
			case "format" :
				if (!FormatConstants.BUNDLE_1p0.equals(pair.getValue())) {
					throw new ContentAccessException("Invalid format: " + pair.getValue());
				}
				break;
				
			case "name" :
				name = pair.getValue();
				break;
				
			case "version" :
				version = pair.getValue();
			}
		}
		
		if (name==null) {
			throw new ContentAccessException("Bundle name must be defined");
		}
		
		if (version == null) {
			throw new ContentAccessException("Bundle version must be defined");
		}
		
		return new AssetBundleKey(name, version);
	}
	
	static class NameValuePair {
		String name;
		String value;
		NameValuePair(String data) throws ContentAccessException {
			int equal = data.indexOf('=');
			if (equal <=0 ) {
				throw new ContentAccessException("Invalid format");
			}
			name = data.substring(0, equal);
			value = data.substring(equal+1);
		}
		public String getName() {
			return name;
		}
		public String getValue() {
			return value;
		}
		
		
	}
}
