package io.konig.transform.beam;

public class BeamFunctions {


	/**
	 * Get the local name from a fully-qualified IRI
	 * @param iriString
	 * @return
	 */
	public static String localName(String iriString) {
		if (iriString != null) {
			int start = iriString.lastIndexOf('/');
			if (start<0) {
				start = iriString.lastIndexOf('#');
				if (start < 0) {
					start = iriString.lastIndexOf(':');
				}
			}
			
			if (start >= 0) {
				return iriString.substring(start+1);
			}
			
		}
		return null;
	}
	
	/**
	 * Strip the spaces from some text.
	 * @param text
	 * @return
	 */
	public static String stripSpaces(String text) {
		StringBuilder builder = new StringBuilder();
		for (int i=0; i<text.length();) {
			int c = text.codePointAt(i);
			
			if (!Character.isSpaceChar(c)) {
				builder.appendCodePoint(c);
			}
			
			i += Character.charCount(c);
			
		}
		return builder.toString();
	}
}
