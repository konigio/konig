package io.konig.shacl;

public class Link implements Comparable<Link> {

	private String name;
	private String href;
	private String className;
	
	public Link(String name, String href) {
		this.name = name;
		this.href = href;
	}

	public Link(String name, String href, String className) {
		this.name = name;
		this.href = href;
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

	public String getName() {
		return name;
	}

	public String getHref() {
		return href;
	}

	@Override
	public int compareTo(Link o) {
		return name.compareToIgnoreCase(o.getName());
	}
	
}
