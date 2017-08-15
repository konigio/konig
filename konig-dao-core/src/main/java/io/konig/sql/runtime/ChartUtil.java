package io.konig.sql.runtime;

public class ChartUtil {
	private static final int UPPER = 1;
	private static final int LOWER = 2;

	public static String label(FieldInfo field) {
		String name = field.getName();
		StringBuilder builder = new StringBuilder();
		
		char c = Character.toUpperCase(name.charAt(0));
		builder.append(c);
		int priorCase = UPPER;
		for (int i=1; i<name.length(); i++) {
			c = name.charAt(i);
			int currentCase = Character.isUpperCase(c) ? UPPER : LOWER;
			if (currentCase != priorCase && currentCase==UPPER) {
				builder.append(' ');
			}
			builder.append(c);
			priorCase = currentCase;
		}
		
		return builder.toString();
	}
}
