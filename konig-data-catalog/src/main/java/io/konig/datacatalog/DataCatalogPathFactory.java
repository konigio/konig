package io.konig.datacatalog;

import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;

public class DataCatalogPathFactory extends PathFactory {

	private String rootPrefix;
	
	public DataCatalogPathFactory(OwlReasoner reasoner, NamespaceManager nsManager, String rootPrefix) {
		super(reasoner, nsManager);
		this.rootPrefix = rootPrefix;
	}

	@Override
	protected boolean excludePrefix(String prefix) {
		return rootPrefix.equals(prefix);
	}

}
