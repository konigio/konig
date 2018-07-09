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


import static org.junit.Assert.*;

import org.junit.Test;

public class AbbrevTrieTest {
	

	@Test
	public void test() {
		
		Abbreviation ou = abbreviation("organizational_unit", "ou");
		Abbreviation org = abbreviation("organizational", "org");
		
		AbbreviationConfig config = new AbbreviationConfig();
		config.setDelimiters("_ \r\t\n");
		config.setPreferredDelimiter("_");
		
		AbbrevTrie trie = new AbbrevTrie();
		trie.add(ou, config.getDelimiters());
		trie.add(org, config.getDelimiters());
		
		String replacement = trie.replaceAll("Fred's Organizational Unit organizational parade", config);
		
		assertEquals("fred's_ou_org_parade", replacement);
	}

	private Abbreviation abbreviation(String prefLabel, String abbreviationLabel) {
		Abbreviation abbrev = new Abbreviation();
		abbrev.setPrefLabel(prefLabel);
		abbrev.setAbbreviationLabel(abbreviationLabel);
		return abbrev;
	}

}
