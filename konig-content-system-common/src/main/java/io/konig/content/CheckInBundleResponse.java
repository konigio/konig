package io.konig.content;

import java.util.List;

public class CheckInBundleResponse {

	private List<String> missingAssets;
	private String editServiceAddress;

	public List<String> getMissingAssets() {
		return missingAssets;
	}

	public void setMissingAssets(List<String> missingAssets) {
		this.missingAssets = missingAssets;
	}

	public String getEditServiceAddress() {
		return editServiceAddress;
	}

	public void setEditServiceAddress(String editServiceAddress) {
		this.editServiceAddress = editServiceAddress;
	}
	
	
}
