package io.konig.yaml;

/*
 * #%L
 * Konig YAML
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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

public class YamlWriterTest {

	@Test
	public void test() {
		
		Person alice = new Person();
		alice.setGivenName("Alice");
		alice.setFamilyName("Jones");
		alice.setAge(9);
		
		PostalAddress address = new PostalAddress();
		address.setPostalCode("90210");
		alice.setAddress(address);
		
		Person bob = new Person();
		bob.setGivenName("Bob");
		
		Person cathy = new Person();
		cathy.setGivenName("Cathy");
		
		Person david = new Person();
		david.setGivenName("David");
		
		alice.addLikes(bob);
		alice.addLikes(cathy);
		alice.addLikes(david);
		
		bob.addLikes(cathy);
		bob.addLikes(david);
		
		String actual = Yaml.toString(alice);
		String expected =
			"!io.konig.yaml.Person &x1\n" + 
			"   age: 9\n" + 
			"   familyName: Jones\n" + 
			"   givenName: Alice\n" + 
			"   address: !io.konig.yaml.PostalAddress &x2\n" + 
			"      postalCode: 90210\n" + 
			"   likes: \n" + 
			"      - !io.konig.yaml.Person &x3\n" + 
			"         age: 0\n" + 
			"         givenName: Bob\n" + 
			"         likes: \n" + 
			"            - !io.konig.yaml.Person &x4\n" + 
			"               age: 0\n" + 
			"               givenName: Cathy\n" + 
			"            - !io.konig.yaml.Person &x5\n" + 
			"               age: 0\n" + 
			"               givenName: David\n" + 
			"      - *x4\n" +
			"      - *x5\n";
		
		assertEquals(expected, actual);
	}

}
