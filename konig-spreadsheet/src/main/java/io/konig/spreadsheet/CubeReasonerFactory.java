package io.konig.spreadsheet;

import io.konig.cadl.CubeReasoner;
import io.konig.core.showl.ShowlManager;

public class CubeReasonerFactory {
	
	private WorkbookProcessor processor;
	private CubeReasoner instance;
	
	public CubeReasonerFactory(WorkbookProcessor processor) {
		this.processor = processor;
	}
	
	public CubeReasoner getInstance() {
		if (instance == null) {
			ShowlManagerFactory factory = processor.service(ShowlManagerFactory.class);
			ShowlManager manager = factory.createInstance();
			instance = new CubeReasoner(manager);
		}
		return instance;
	}

	

}
