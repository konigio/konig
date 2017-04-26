package io.konig.content;

public class AssetBundleKey {

	private String name;
	private String version;
	
	public AssetBundleKey(String name, String version) {
		this.name = name;
		this.version = version;
	}
	public String getName() {
		return name;
	}
	public String getVersion() {
		return version;
	}
	
	public String url(String baseURL) {
		StringBuilder builder = new StringBuilder();
		builder.append(baseURL);
		if (!baseURL.endsWith("/")) {
			builder.append('/');
		}
		builder.append(name);
		builder.append('/');
		builder.append(version);
		builder.append('/');
		return builder.toString();
	}
	
	public String assetURL(String baseURL, AssetMetadata metadata) {

		StringBuilder builder = new StringBuilder();
		builder.append(baseURL);
		if (!baseURL.endsWith("/")) {
			builder.append('/');
		}
		String path = metadata.getPath();
		builder.append(name);
		builder.append('/');
		builder.append(version);
		if (!path.startsWith("/")) {
			builder.append('/');
		}
		builder.append(metadata.getPath());
		return builder.toString();
	}
	
	public String trimBundlePath(String path) {
		int bundleNameEnd = path.indexOf('/', 1);
		int versionEnd = path.indexOf('/', bundleNameEnd+1);
		return path.substring(versionEnd+1);
	}
	
	
}
