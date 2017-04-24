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
	
	
}
