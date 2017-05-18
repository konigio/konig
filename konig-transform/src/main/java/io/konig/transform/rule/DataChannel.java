package io.konig.transform.rule;

import io.konig.shacl.Shape;

public class DataChannel implements Comparable<DataChannel> {

	private int ranking = 0;
	private String name;
	private Shape shape;
	
	public DataChannel(String name, Shape value) {
		this.name = name;
		this.shape = value;
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
	public int compareTo(DataChannel other) {
		return ranking-other.getRanking();
	}
	

	public String getName() {
		return name;
	}

	public Shape getShape() {
		return shape;
	}
}
