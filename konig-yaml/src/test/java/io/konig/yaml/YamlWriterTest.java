package io.konig.yaml;

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
