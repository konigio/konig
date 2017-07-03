package io.konig.yaml;

import java.util.HashMap;
import java.util.Map;

public class Organization {
	
	private ContactPointMap contactPoints;

	public Organization() {
	}

	public Map<String, ContactPoint> getContactPoint() {
		return contactPoints;
	}

	public void setContactPoint(ContactPointMap contactPoints) {
		this.contactPoints = contactPoints;
	}
	
	@SuppressWarnings("serial")
	static class ContactPointMap extends HashMap<String, ContactPoint> {
		
	}

}
