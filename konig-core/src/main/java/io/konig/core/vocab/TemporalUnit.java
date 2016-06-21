package io.konig.core.vocab;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public enum TemporalUnit {

	YEAR("http://www.w3.org/2006/time#unitYear"),
	MONTH("http://www.w3.org/2006/time#unitMonth"),
	WEEK("http://www.w3.org/2006/time#unitWeek"),
	DAY("http://www.w3.org/2006/time#unitDay"),
	HOUR("http://www.w3.org/2006/time#unitHour"),
	MINUTE("http://www.w3.org/2006/time#unitMinute"),
	SECOND("http://www.w3.org/2006/time#unitSecond") ;
	
	private URI uri;
	
	
	private TemporalUnit(String value) {
		uri = new URIImpl(value);
	}
	
	public URI getURI() {
		return uri;
	}
	
	public String toString() {
		return uri.getLocalName();
	}
	
	public static TemporalUnit fromURI(URI value) {
		
		for (TemporalUnit t : values()) {
			if (t.getURI().equals(value)) {
				return t;
			}
		}
		
		return null;
	}
}
