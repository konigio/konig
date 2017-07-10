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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

public class YamlReaderTest {
	
	@Test
	public void testQuotedFieldInlineList() throws Exception {
		String text =
			"likes:\n" + 
			"  - 'givenName': Alice\n" + 
			"    'familyName': Jones\n" + 
			"  - 'givenName': Bob\n" + 
			"    'familyName': Smith\n" + 
			"  - 'givenName': Cathy\n" + 
			"    'familyName': Johnson";
		
		Person person = Yaml.read(Person.class, text);
		
		List<Person> likes = person.getLikes();
		assertTrue(likes != null);
		assertEquals(3, likes.size());
		
		Person friend = likes.get(0);
		assertEquals("Alice", friend.getGivenName());
		assertEquals("Jones", friend.getFamilyName());
		
		friend = likes.get(2);
		assertEquals("Cathy", friend.getGivenName());
		assertEquals("Johnson", friend.getFamilyName());
		
		
	}
	
	@Test
	public void testBareInlineList() throws Exception {
		String text =
			"likes:\n" + 
			"  - givenName: Alice\n" + 
			"    familyName: Jones\n" + 
			"  - givenName: Bob\n" + 
			"    familyName: Smith\n" + 
			"  - givenName: Cathy\n" + 
			"    familyName: Johnson";
		
		Person person = Yaml.read(Person.class, text);
		
		List<Person> likes = person.getLikes();
		assertTrue(likes != null);
		assertEquals(3, likes.size());
		
		Person friend = likes.get(0);
		assertEquals("Alice", friend.getGivenName());
		assertEquals("Jones", friend.getFamilyName());
		
		friend = likes.get(2);
		assertEquals("Cathy", friend.getGivenName());
		assertEquals("Johnson", friend.getFamilyName());
		
		
	}
	
	
	
	@Test
	public void testMap() throws Exception {
		String text =
			"contactPoint:\n" + 
			"  sales:\n" + 
			"    email: sales@acme.com\n" + 
			"  support:\n" + 
			"    email: support@acme.com";
		
		Organization org = Yaml.read(Organization.class, text);
		
		Map<String, ContactPoint> map = org.getContactPoint();
		assertTrue(map != null);
		ContactPoint sales = map.get("sales");
		assertTrue(sales != null);
		assertEquals("sales@acme.com", sales.getEmail());
		
		ContactPoint support = map.get("support");
		assertTrue(support != null);
		assertEquals("support@acme.com", support.getEmail());
		
	}
	
	@Test
	public void testMapAdapter() throws Exception {
		String text =
			"givenName: Alice\n" + 
			"familyName: Jones\n" + 
			"contactPoint:\n" + 
			"  work:\n" + 
			"    email: alice@work.com\n" + 
			"  home:\n" + 
			"    email: alice@home.com";
		
		Person alice = Yaml.read(Person.class, text);
		
		ContactPoint work = alice.getContactPoint("work");
		assertTrue(work!=null);
		assertEquals("alice@work.com", work.getEmail());
		
		ContactPoint home = alice.getContactPoint("home");
		assertTrue(home!=null);
		assertEquals("alice@home.com", home.getEmail());
	}
	
	@Test
	public void testStringArgConstructor() throws Exception {
		String text = 
				"\n" + 
				"   givenName: Alice\n" + 
				"   likes: \n" + 
				"      - Bob\n" + 
				"      - \n" +
				"         givenName: Cathy\n" +
				"         likes:\n" +
				"            - Dan";
			
			Person alice = Yaml.read(Person.class, text);
			assertTrue(alice != null);
			assertEquals("Alice", alice.getGivenName());
			
			List<Person> aliceLikes = alice.getLikes();
			assertTrue(aliceLikes != null);
			assertEquals(2, aliceLikes.size());
			Person bob = aliceLikes.get(0);
			assertEquals(0, bob.getAge());
			assertEquals("Bob", bob.getGivenName());
			Person cathy = aliceLikes.get(1);
			assertEquals("Cathy", cathy.getGivenName());
			
			List<Person> cathyLikes = cathy.getLikes();
			assertTrue(cathyLikes != null);
			assertEquals(1, cathyLikes.size());
			
			Person dan = cathyLikes.get(0);
			assertTrue(dan != null);
			assertEquals("Dan", dan.getGivenName());
			
	}
	
	@Test
	public void testUntyped() throws Exception {

		String text = 
			"\n" + 
			"   age: 9\n" + 
			"   familyName: Jones\n" + 
			"   givenName: Alice\n" + 
			"   address: \n" + 
			"      postalCode: 90210\n" + 
			"   likes: \n" + 
			"      - \n" + 
			"         age: 0\n" + 
			"         givenName: Bob\n" + 
			"         likes: \n" + 
			"            - &x4\n" + 
			"               age: 0\n" + 
			"               givenName: Cathy\n" + 
			"      - *x4";
		
		Person alice = Yaml.read(Person.class, text);
		assertTrue(alice != null);
		assertEquals(9, alice.getAge());
		assertEquals("Jones", alice.getFamilyName());
		assertEquals("Alice", alice.getGivenName());
		
		PostalAddress address = alice.getAddress();
		assertTrue(address != null);
		assertEquals("90210", address.getPostalCode());
		List<Person> aliceLikes = alice.getLikes();
		assertTrue(aliceLikes != null);
		assertEquals(2, aliceLikes.size());
		Person bob = aliceLikes.get(0);
		assertEquals(0, bob.getAge());
		assertEquals("Bob", bob.getGivenName());
		List<Person> bobLikes = bob.getLikes();
		assertTrue(bobLikes != null);
		assertEquals(1, bobLikes.size());
		Person cathy = bobLikes.get(0);
		assertEquals(0, cathy.getAge());
		assertEquals("Cathy", cathy.getGivenName());
		
		Person aliceCathy = aliceLikes.get(1);
		assertTrue(aliceCathy == cathy);
	}
	
	@Test
	public void testDeserializer() throws Exception {
		String text =
			  "!io.konig.yaml.WebResource &x1\n"
			+ "  url: http://example.com/foo";
		
		StringReader input = new StringReader(text);
		YamlReader reader = new YamlReader(input);
		reader.addDeserializer(URL.class, new UrlDeserializer());
		
		WebResource resource = reader.readObject(WebResource.class);
		URL url = resource.getUrl();
		assertEquals("http://example.com/foo", url.toString());
		reader.close();
		
	}
	
	@Test
	public void testQuotedString() throws Exception {
		Message msg = new Message();
		msg.setText(", my friend");
		
		String yamlText = Yaml.toString(msg);
		Message result = Yaml.read(Message.class, yamlText);
		assertEquals(msg.getText(), result.getText());
	}

	@Test
	public void test() throws Exception {
		
		String text = 
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
			"      - *x4";
		
		Person alice = Yaml.read(Person.class, text);
		assertTrue(alice != null);
		assertEquals(9, alice.getAge());
		assertEquals("Jones", alice.getFamilyName());
		assertEquals("Alice", alice.getGivenName());
		
		PostalAddress address = alice.getAddress();
		assertTrue(address != null);
		assertEquals("90210", address.getPostalCode());
		List<Person> aliceLikes = alice.getLikes();
		assertTrue(aliceLikes != null);
		assertEquals(2, aliceLikes.size());
		Person bob = aliceLikes.get(0);
		assertEquals(0, bob.getAge());
		assertEquals("Bob", bob.getGivenName());
		List<Person> bobLikes = bob.getLikes();
		assertTrue(bobLikes != null);
		assertEquals(1, bobLikes.size());
		Person cathy = bobLikes.get(0);
		assertEquals(0, cathy.getAge());
		assertEquals("Cathy", cathy.getGivenName());
		
		Person aliceCathy = aliceLikes.get(1);
		assertTrue(aliceCathy == cathy);
	}

}
