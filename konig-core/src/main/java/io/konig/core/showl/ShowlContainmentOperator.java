package io.konig.core.showl;

public enum ShowlContainmentOperator {
	IN("IN"),
	NOT_IN("NOT IN");
	
	private String displayName;
	
	private ShowlContainmentOperator(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
	
	

}
