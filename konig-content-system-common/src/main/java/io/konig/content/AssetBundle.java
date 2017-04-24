package io.konig.content;

import java.util.List;

public class AssetBundle {
	
	private AssetBundleKey key;
	private List<AssetMetadata> metadataList;
	
	public AssetBundle(String name, String version) {
		this(new AssetBundleKey(name, version));
	}

	public AssetBundle(AssetBundleKey key) {
		this.key = key;
	}

	public AssetBundleKey getKey() {
		return key;
	}

	public List<AssetMetadata> getMetadataList() {
		return metadataList;
	}

	public void setMetadataList(List<AssetMetadata> metadataList) {
		this.metadataList = metadataList;
	}

}
