package io.konig.core.showl.expression;

import java.util.HashMap;
import java.util.Map;

public class Indentation {
	
	private static Map<StringBuilder, Indentation> map = new HashMap<>();
	
	private int depth=0;
	private StringBuilder builder;

	
	public Indentation(StringBuilder builder) {
		this.builder = builder;
	}

	public static Indentation get(StringBuilder builder) {
		Indentation result = map.get(builder);
		if (result == null) {
			result = new Indentation(builder);
		}
		return result;
	}
	
	public void push() {
		depth++;
		if (depth == 1) {
			map.put(builder, this);
		}
	}
	
	public void pop() {
		 depth--;
		 if (depth == 0) {
			 map.remove(builder);
		 }
	}
	
	public void indent(StringBuilder builder) {
		for (int i=0; i<depth; i++) {
			builder.append("  ");
		}
	}

}
