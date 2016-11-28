package io.konig.schemagen.gcp; 

import java.util.ArrayList;
import java.util.List;

public class CompositeBigQueryTableHandler implements BigQueryTableHandler {
	
	private List<BigQueryTableHandler> list = new ArrayList<>();
	
	public void addHandler(BigQueryTableHandler handler) {
		list.add(handler);
	}

	@Override
	public void add(BigQueryTable table) {
		for (BigQueryTableHandler handler : list) {
			handler.add(table);
		}
		
	}

	

}
