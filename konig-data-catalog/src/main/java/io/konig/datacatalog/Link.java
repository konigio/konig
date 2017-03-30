package io.konig.datacatalog;

public class Link implements Comparable<Link> {

	private String name;
	private String href;
	
	public Link(String name, String href) {
		this.name = name;
		this.href = href;
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
