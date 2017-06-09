package io.konig.content;

public class AssetMetadata {

	private AssetBundleKey bundleKey;
	private String path;
	private String contentType;
	private String etag;
	
	
	public AssetBundleKey getBundleKey() {
		return bundleKey;
	}
	public void setBundleKey(AssetBundleKey bundleKey) {
		this.bundleKey = bundleKey;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = ContentSystemUtil.trimSlashes(path);
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getEtag() {
		return etag;
	}
	public void setEtag(String etag) {
		this.etag = etag;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{bundleKey: ");
		builder.append(bundleKey);
		builder.append(", contentType: ");
		builder.append(contentType);
		builder.append(", etag: ");
		builder.append(etag);
		builder.append(", path: ");
		builder.append(path);
		builder.append('}');
		return builder.toString();
	}
	
	
}
