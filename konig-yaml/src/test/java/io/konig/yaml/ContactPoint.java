package io.konig.yaml;

public class ContactPoint {
	private String contactType;
	private String email;

	public ContactPoint(String contactType) {
		this.contactType = contactType;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getContactType() {
		return contactType;
	}
	
	

}
