package io.konig.transform.rule;

public class AlphabeticVariableNamer implements VariableNamer {

	private int count=0;
	
	
	@Override
	public String next() {
		count++;
		StringBuilder builder = new StringBuilder();
		int num = count;
		while (num > 0) {
			num--;
			int remainder = num % 26;
			char digit = (char) (remainder + 97);
			builder.append(digit);
			num = (num-remainder) / 26;
		}
		
		return builder.toString();
	}

}
