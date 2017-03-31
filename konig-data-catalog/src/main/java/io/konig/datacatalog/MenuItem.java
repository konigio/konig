package io.konig.datacatalog;

import org.openrdf.model.URI;

public class MenuItem {
	private URI itemId;
	private String name;
	
	public MenuItem(URI itemId, String name) {
		this.itemId = itemId;
		this.name = name;
	}

	public URI getItemId() {
		return itemId;
	}

	public String getName() {
		return name;
	}
	
}
