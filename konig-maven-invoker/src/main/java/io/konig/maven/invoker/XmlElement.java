package io.konig.maven.invoker;

import java.util.ArrayList;
import java.util.List;

public class XmlElement extends BaseXmlNode {
	private String tagName;
	private List<XmlAttribute> attributeList = null;
	private List<XmlNode> nodeList=new ArrayList<>();

	public XmlElement(String tagName) {
		this.tagName = tagName;
	}

	public String getTagName() {
		return tagName;
	}
	
	public void addAttribute(XmlAttribute attribute) {
		if (attributeList ==null) {
			attributeList = new ArrayList<>();
		}
		attributeList.add(attribute);
	}
	
	public void addNode(XmlNode node) {
		nodeList.add(node);
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.print('<');
		out.print(tagName);
		out.println('>');
		out.pushIndent();
		for (XmlNode node : nodeList) {
			out.indent();
			node.print(out);
		}
		
		out.popIndent();
		out.print('>');
		out.println();
		
	}

	public List<XmlAttribute> getAttributeList() {
		return attributeList;
	}

	public List<XmlNode> getNodeList() {
		return nodeList;
	}
	
	

}
