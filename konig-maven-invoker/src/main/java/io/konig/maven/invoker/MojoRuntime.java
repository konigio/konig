package io.konig.maven.invoker;

import java.util.ArrayList;
import java.util.List;

public class MojoRuntime {
	
	
	private XmlElement xml;

	public MojoRuntime() {
		
		Builder builder = new Builder();
		
		
	}
	

	public static class Builder {
		private String modelVersion = "4.0.0";
		private String groupId;
		private String artifactId;
		private String version;
		private XmlElement configuration;
		private String phase;
		private String goal;
		
		
		
		private XmlElement xml;
		private List<XmlElement> stack = new ArrayList<>();
		public Builder() {
			xml = new XmlElement("project");
			push(xml);
			attribute("xmlNamespace","http://maven.apache.org/POM/4.0.0");
			attribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			attribute("xsi:schemaLocation", "http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd");
		}
		
		private void push(XmlElement e) {
			stack.add(e);
		}
		
		private XmlElement peek() {
			return stack.get(stack.size()-1);
		}
		
		public Builder attribute(String name, String value) {
			peek().addAttribute(new XmlAttribute(name, value));
			return this;
		}
		
		public Builder property(String tagName, String value) {
			XmlElement e = new XmlElement(tagName);
			e.addNode(new XmlText(value));
			stack.add(e);
			return this;
		}
		
		public Builder begin(String tagName) {
			XmlElement e = new XmlElement(tagName);
			push(e);
			return this;
		}
		
		public Builder end(String tagName) {
			while (!stack.isEmpty()) {
				XmlElement e = stack.remove(stack.size()-1);
				if (e.getTagName().equals(tagName)) {
					break;
				}
			}
			return this;
			
		}
		
		
	}
	
	
}
