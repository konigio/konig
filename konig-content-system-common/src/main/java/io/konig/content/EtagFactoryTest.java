package io.konig.content;

public class EtagFactoryTest {

	
	public static void main(String[] args) {
		String value = EtagFactory.createEtag("Organization".getBytes());
		System.out.println(value);
	}

}
