package io.konig.transform.rule;

import java.util.Comparator;

public class RankedVariable<T> extends Variable<T> implements Comparable<RankedVariable<T>> {

	private int ranking = 0;
	public RankedVariable(String name, T value) {
		super(name, value);
	}
	public int getRanking() {
		return ranking;
	}
	public void setRanking(int ranking) {
		this.ranking = ranking;
	}
	
	public void plusOne() {
		ranking++;
	}
	@Override
	public int compareTo(RankedVariable<T> other) {
		return ranking-other.getRanking();
	}
	
	
}
