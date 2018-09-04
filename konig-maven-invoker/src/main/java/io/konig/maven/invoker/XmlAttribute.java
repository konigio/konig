package io.konig.maven.invoker;

public class XmlAttribute implements XmlNode {
	private String name;
	private String value;
	public XmlAttribute(String name, String value) {
		this.name = name;
		this.value = value;
	}
	public String getName() {
		return name;
	}
	public String getValue() {
		return value;
	}
	@Override
	public void print(PrettyPrintWriter out) {
		out.print(name);
		out.print("=\"");
		out.print(value);
		out.print('"');
		
	}

	
}
