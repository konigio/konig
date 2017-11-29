package io.konig.core.vocab;

import org.openrdf.model.URI;

public enum KonigTime {

	DAY(Konig.Day, 86400000L),
	WEEK(Konig.Week, 604800000L),
	MONTH(Konig.Month, 2628000000L),
	YEAR(Konig.Year, 31540000000L);
	
	private URI iri;
	private long milliseconds;
	
	private KonigTime(URI iri, long milliseconds) {
		this.iri = iri;
		this.milliseconds = milliseconds;
	}

	public URI getIri() {
		return iri;
	}

	/**
	 * The number of milliseconds in this unit of time
	 * @return
	 */
	public long getMilliseconds() {
		return milliseconds;
	}
	
	public static KonigTime fromIri(URI uri) {
		for (KonigTime value : KonigTime.values()) {
			if (value.getIri().equals(uri)) {
				return value;
			}
		}
		return null;
	}
	
	

}
