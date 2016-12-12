package io.konig.gae.datastore;

public class IntegerRange {

	private Long minInclusive;
	private Long minExclusive;
	private Long maxInclusive;
	private Long maxExclusive;
	
	public Long getMinInclusive() {
		return minInclusive;
	}
	public void setMinInclusive(Long minInclusive) {
		this.minInclusive = minInclusive;
	}
	public Long getMinExclusive() {
		return minExclusive;
	}
	public void setMinExclusive(Long minExclusive) {
		this.minExclusive = minExclusive;
	}
	public Long getMaxInclusive() {
		return maxInclusive;
	}
	public void setMaxInclusive(Long maxInclusive) {
		this.maxInclusive = maxInclusive;
	}
	public Long getMaxExclusive() {
		return maxExclusive;
	}
	public void setMaxExclusive(Long maxExclusive) {
		this.maxExclusive = maxExclusive;
	}
}
