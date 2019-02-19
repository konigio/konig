package io.konig.spreadsheet;

import io.konig.core.showl.ShowlManager;
import io.konig.core.showl.ShowlPropertyManager;

public class ShowlManagerFactory implements ServiceFactory<ShowlManager>{

	private WorkbookProcessor processor;
	private ShowlPropertyManager showlManager;
	
	

	public ShowlManagerFactory(WorkbookProcessor processor) {
		this.processor = processor;
	}



	@Override
	public ShowlManager createInstance() {
		if (showlManager == null) {
			showlManager = new ShowlPropertyManager(
					processor.getShapeManager(), 
					processor.getOwlReasoner());
			showlManager.build();
			
			
		}
		return showlManager;
	}

}
