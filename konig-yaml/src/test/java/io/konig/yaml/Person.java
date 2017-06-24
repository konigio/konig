package io.konig.yaml;

import java.util.ArrayList;
import java.util.List;

public class Person {

	private String givenName;
	private String familyName;
	private int age;
	private List<Person> likes;
	private PostalAddress address;
	
	public Person() {
		
	}
	
	
	public Person(String givenName) {
		this.givenName = givenName;
	}
	
	public PostalAddress getAddress() {
		return address;
	}

	public void setAddress(PostalAddress address) {
		this.address = address;
	}

	public void addLikes(Person person) {
		if (likes == null) {
			likes = new ArrayList<>();
		}
		likes.add(person);
	}
	
	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getGivenName() {
		return givenName;
	}
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}
	public String getFamilyName() {
		return familyName;
	}
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}
	public List<Person> getLikes() {
		return likes;
	}
	public void setLikes(List<Person> likes) {
		this.likes = likes;
	}
	
	
	
	

}
