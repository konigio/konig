package io.konig.maven.invoker;

import java.io.StringWriter;

public abstract class BaseXmlNode implements XmlNode {
	
	public String toString() {
		StringWriter buffer = new StringWriter();
		PrettyPrintWriter out = new PrettyPrintWriter(buffer);
		this.print(out);
		out.close();
		return buffer.toString();
	}

}
