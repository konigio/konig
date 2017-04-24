package io.konig.content;

import java.util.List;

public class CheckBundleResponse {

	private List<String> missingAssets;

	public List<String> getMissingAssets() {
		return missingAssets;
	}

	public void setMissingAssets(List<String> missingAssets) {
		this.missingAssets = missingAssets;
	}
	
	
}
