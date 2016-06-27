package io.konig.schemagen.gcp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;

import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class MemoryGcpManager implements GcpManager, BigQueryTableHandler {
	
	private Map<String, List<BigQueryTable>> tablesForClass = new HashMap<>();
	private ShapeManager shapeManager;
	
	

	public MemoryGcpManager(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
	}

	@Override
	public Collection<BigQueryTable> tablesForClass(URI owlClass) throws GoogleCloudException {
		return tablesForClass.get(owlClass.stringValue());
	}

	@Override
	public void add(BigQueryTable table) {
		
		URI owlClass = table.getTableClass();
		if (owlClass == null) {
			URI shapeId = table.getTableShape();
			if (shapeId != null) {
				Shape shape = shapeManager.getShapeById(shapeId);
				if (shape != null) {
					owlClass = shape.getScopeClass();
				}
			}
		}
		if (owlClass != null) {
			List<BigQueryTable> list = tablesForClass.get(owlClass.stringValue());
			if (list == null) {
				list = new ArrayList<>();
				tablesForClass.put(owlClass.stringValue(), list);
			}
			list.add(table);
		}
		
	}

}
