package io.konig.datacatalog;

public class OntologyDescription extends ResourceDescription implements Comparable<OntologyDescription> {

	private boolean isEnumNamespace;
	
	public OntologyDescription(String href, String name, String description, boolean isEnumNamespace) {
		super(href, name, description);
		this.isEnumNamespace = isEnumNamespace;
	}
	
	public boolean getIsEnumNamespace() {
		return isEnumNamespace;
	}

	@Override
	public int compareTo(OntologyDescription o) {
		
		return this.getName().compareTo(o.getName());
	}
	
	

}
