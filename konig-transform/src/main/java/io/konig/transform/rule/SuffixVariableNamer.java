package io.konig.transform.rule;

public class SuffixVariableNamer implements VariableNamer {

	private String word;
	private int count;
	
	public SuffixVariableNamer(String word) {
		this.word = word;
	}
	
	@Override
	public String next() {
		count++;
		StringBuilder builder = new StringBuilder();
		builder.append(word);
		builder.append(count);
		return builder.toString();
	}

}
