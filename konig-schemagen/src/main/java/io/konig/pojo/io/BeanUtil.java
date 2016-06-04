package io.konig.pojo.io;

import org.openrdf.model.URI;

import io.konig.core.util.StringUtil;

public class BeanUtil {
	
	public static String setterName(URI predicate) {
		StringBuilder builder = new StringBuilder();
		builder.append("set");
		builder.append(StringUtil.capitalize(predicate.getLocalName()));
		
		return builder.toString();
	}
	
	public static String adderName(URI predicate) {
		StringBuilder builder = new StringBuilder();
		builder.append("add");
		builder.append(StringUtil.capitalize(predicate.getLocalName()));
		
		return builder.toString();
	}

}
