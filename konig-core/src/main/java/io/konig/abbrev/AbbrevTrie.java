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


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class AbbrevTrie implements  Comparable<AbbrevTrie> {
	
	private String term;
	private Abbreviation abbrev;
	private Map<String, AbbrevTrie> children;

	public AbbrevTrie() {
	}
	
	public AbbrevTrie(String term) {
		this.term = term;
	}
	
	public void add(Abbreviation abbrev, String delimiters) {
		String text = abbrev.getPrefLabel();
		AbbrevTrie tail = append(tokenize(text, delimiters), 0);
		tail.abbrev = abbrev;
	}
	
	
	private List<String> tokenize(String text, String delimiters) {

		List<String> list = new ArrayList<>();
		StringTokenizer tokenizer = new StringTokenizer(text, delimiters);
		while (tokenizer.hasMoreTokens()) {
			String word = tokenizer.nextToken().toLowerCase();
			list.add(word);
		}
		return list;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		append(builder, 0);
		return builder.toString();
	}
	
	
	
	



	private void append(StringBuilder builder, int indent) {
		
		indent(builder, indent);
		if (term != null) {
			builder.append(term);
		}
		if (abbrev != null) {
			builder.append(" --> ");
			builder.append(abbrev.getAbbreviationLabel());
		}
		if (term != null) {
			builder.append('\n');
		}
		if (children != null) {
			indent++;
			List<AbbrevTrie> list = new ArrayList<>(children.values());
			Collections.sort(list);
			for (AbbrevTrie child : list) {
				child.append(builder, indent);
			}
		}
		
		
	}







	private void indent(StringBuilder builder, int indent) {
		for (int i=0; i<indent*3; i++) {
			builder.append(' ');
		}
		
	}







	private static class Term {
		private String word;
		private int wordCount=0;
		private Abbreviation abbrev;
		public Term(String word) {
			this.word = word;
		}
		
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Term[word: ");
			builder.append(word);
			if (abbrev !=null) {

				builder.append(", abbrev: ");
				builder.append(abbrev.getAbbreviationLabel());
			}
			builder.append(", wordCount: ");
			builder.append(wordCount);
			builder.append(']');
			return builder.toString();
		}
	
	}
	
	
	public String replaceAll(String text, AbbreviationConfig config) {
		List<Term> termList = toTermList(text, config.getDelimiters());
		
		match(termList);
		
		StringBuilder builder = new StringBuilder();
		for (int i=0; i<termList.size();) {
			if (i>0) {
				builder.append(config.getPreferredDelimiter());
			}
			Term term = termList.get(i);
			if (term.abbrev == null) {
				builder.append(term.word);
				i++;
			} else {
				builder.append(term.abbrev.getAbbreviationLabel());
				i+=term.wordCount;
			}
		}
		
		return builder.toString();
	}
	
	private AbbrevTrie getChild(String term) {
		
		return children==null ? null : children.get(term);
	}
	
	private void match(List<Term> termList) {
		for (int i=0; i<termList.size();) {
			Term term = termList.get(i);
			AbbrevTrie trie = getChild(term.word);
			if (trie != null) {
				int j = i;
				
				while (trie != null) {
					j++;
					if (trie.abbrev != null) {
						term.abbrev = trie.abbrev;
						term.wordCount = j-i;
					} 
					Term next = termList.get(j);
					trie = trie.getChild(next.word);
				}
			}
			if (term.abbrev==null) {
				term.wordCount=1;
			}
			i += term.wordCount;
		}
		
	}

	private List<Term> toTermList(String text, String delimiters) {
		List<Term> termList = new ArrayList<>();
		StringTokenizer tokens = new StringTokenizer(text, delimiters);
		while (tokens.hasMoreTokens()) {
			String token = tokens.nextToken().toLowerCase();
			termList.add(new Term(token));
		}
		return termList;
	}
	
	private AbbrevTrie append(List<String> wordList, int startIndex) {
		
		AbbrevTrie trie = this;
		for (String word : wordList) {
			AbbrevTrie child = trie.getChild(word);
			if (child == null) {
				child = new AbbrevTrie(word);
				if (trie.children==null) {
					trie.children = new HashMap<>();
				}
				trie.children.put(word, child);
			}
			trie = child;
		}
		
		return trie;
	}

	@Override
	public int compareTo(AbbrevTrie o) {
		return term.compareTo(o.term);
	}

}
