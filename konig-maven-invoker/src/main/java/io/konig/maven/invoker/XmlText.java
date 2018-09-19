package io.konig.maven.invoker;

public class XmlText extends BaseXmlNode {

	private String stringValue;

	public XmlText(String stringValue) {
		this.stringValue = stringValue;
	}

	public String getStringValue() {
		return stringValue;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.print(stringValue);
	}
	
	
}
