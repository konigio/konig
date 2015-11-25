package io.konig.core;

import org.openrdf.model.URI;

public class Term {
	private String key;
	private String id;
	private String language;
	private String type;
	private URI expandedType;
	private URI expandedId;
	private int index=-1;
	
	public Term(String key, String id, String language, String type) {
		this.key = key;
		this.id = id;
		this.language = language;
		this.type = type;
	}

	public String getKey() {
		return key;
	}

	public String getId() {
		return id;
	}

	public String getLanguage() {
		return language;
	}

	public String getType() {
		return type;
	}

	public URI getExpandedType() {
		return expandedType;
	}

	void setExpandedType(URI expandedType) {
		this.expandedType = expandedType;
	}

	public URI getExpandedId() {
		return expandedId;
	}

	void setExpandedId(URI expandedId) {
		this.expandedId = expandedId;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(key);
		buffer.append('[');
		String comma = "";
		if (id != null) {
			buffer.append("@id: ");
			buffer.append(id);
			comma = ", ";
		}
		if (type != null) {
			buffer.append(comma);
			buffer.append("@type: ");
			buffer.append(type);
			comma = ", ";
		}
		if (language != null) {
			buffer.append("@language: ");
			buffer.append(language);
		}
		buffer.append(']');
		
		return buffer.toString();
	}
	

	public String getExpandedIdValue() {
		return expandedId!=null ? expandedId.stringValue() :
			id != null ? id :
			null;
	}

}
