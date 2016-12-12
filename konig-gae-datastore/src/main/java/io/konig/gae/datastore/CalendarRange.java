package io.konig.gae.datastore;

import java.util.Calendar;

public class CalendarRange {
	private Calendar minInclusive;
	private Calendar minExclusive;
	private Calendar maxInclusive;
	private Calendar maxExclusive;
	
	public Calendar getMinInclusive() {
		return minInclusive;
	}
	public void setMinInclusive(Calendar minInclusive) {
		this.minInclusive = minInclusive;
	}
	public Calendar getMinExclusive() {
		return minExclusive;
	}
	public void setMinExclusive(Calendar minExclusive) {
		this.minExclusive = minExclusive;
	}
	public Calendar getMaxInclusive() {
		return maxInclusive;
	}
	public void setMaxInclusive(Calendar maxInclusive) {
		this.maxInclusive = maxInclusive;
	}
	public Calendar getMaxExclusive() {
		return maxExclusive;
	}
	public void setMaxExclusive(Calendar maxExclusive) {
		this.maxExclusive = maxExclusive;
	}

	

}
