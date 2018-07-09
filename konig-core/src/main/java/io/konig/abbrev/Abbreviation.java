package io.konig.abbrev;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.util.StringTokenizer;

import org.openrdf.model.URI;

public class Abbreviation  {
	
	private URI id;
	private String prefLabel;
	private String abbreviationLabel;
	private AbbreviationScheme inScheme;


	public Abbreviation() {
	}
	
	

	public Abbreviation(URI id) {
		this.id = id;
	}


	public URI getId() {
		return id;
	}


	public void setId(URI id) {
		this.id = id;
	}


	public String getPrefLabel() {
		return prefLabel;
	}


	public void setPrefLabel(String prefLabel) {
		this.prefLabel = prefLabel;
	}



	public static String normalize(String text) {
		StringTokenizer tokens = new StringTokenizer(text, " \t\r\n");
		
		StringBuilder builder = new StringBuilder(text.length());
		String space = "";
		while (tokens.hasMoreTokens()) {
			builder.append(space);
			space = " ";
			String word = tokens.nextToken().toLowerCase();
			builder.append(word);
			
		}
		return builder.toString();
	}


	public String getAbbreviationLabel() {
		return abbreviationLabel;
	}


	public void setAbbreviationLabel(String abbreviationLabel) {
		this.abbreviationLabel = abbreviationLabel;
	}


	public AbbreviationScheme getInScheme() {
		return inScheme;
	}


	public void setInScheme(AbbreviationScheme inScheme) {
		this.inScheme = inScheme;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Abbreviation[prefLabel: \"");
		builder.append(prefLabel);
		builder.append("\", abbreviationLabel: \"");
		builder.append(abbreviationLabel);
		builder.append("\"]");
		return builder.toString();
	}

}
