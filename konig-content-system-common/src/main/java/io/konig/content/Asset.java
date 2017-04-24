package io.konig.content;

public class Asset {
	
	private AssetMetadata metadata;
	private byte[] body;
	
	public Asset(AssetMetadata metadata, byte[] body) {
		this.metadata = metadata;
		this.body = body;
	}

	public AssetMetadata getMetadata() {
		return metadata;
	}

	public byte[] getBody() {
		return body;
	}
}
