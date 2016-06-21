package io.konig.pojo.io;

import org.openrdf.model.URI;

import io.konig.core.util.StringUtil;

public class BeanUtil {
	
	public static String setterName(URI predicate) {
		StringBuilder builder = new StringBuilder();
		String localName = predicate.getLocalName();
		if (localName.startsWith("is") && localName.length()>2 && Character.isUpperCase(localName.charAt(2))) {
			localName = localName.substring(2);
		}
		builder.append("set");
		builder.append(StringUtil.capitalize(localName));
		
		return builder.toString();
	}
	
	
	
	public static String adderName(URI predicate) {
		StringBuilder builder = new StringBuilder();
		builder.append("add");
		builder.append(StringUtil.capitalize(predicate.getLocalName()));
		
		return builder.toString();
	}

}
