package io.konig.dao.sql.generator;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;
import io.konig.shacl.Shape;

public class BasicSqlDataSourceProcessor implements SqlDataSourceProcessor {
	private  String packageName;
	
	

	public BasicSqlDataSourceProcessor(String packageName) {
		this.packageName = packageName;
	}
	@Override
	public List<DataSource> findDataSources(Shape shape) {
		List<DataSource> list = new ArrayList<>();
		List<DataSource> sourceList = shape.getShapeDataSource();
		if (sourceList != null) {
				
			for (DataSource ds : sourceList) {
				if (ds instanceof TableDataSource) {
					list.add(ds);
				}
			}
		}
		
		return list;
	}

	@Override
	public String shapeReaderClassName(Shape shape, DataSource dataSource) throws SqlDaoGeneratorException {
		
		String shapeSlug = shapeSlug(shape);
		
		StringBuilder builder = new StringBuilder();
		builder.append(packageName);
		builder.append('.');
		builder.append(shapeSlug);
		builder.append("SqlReadService");
		
		return builder.toString();
	}

	private String shapeSlug(Shape shape) throws SqlDaoGeneratorException {
		Resource shapeId = shape.getId();
		if (shapeId instanceof URI) {
			URI uri = (URI) shapeId;
			String localName = uri.getLocalName();
			if (localName.endsWith("Shape")) {
				localName = localName.substring(0, localName.length() - 5);
			}
			return localName;
		} 
		throw new SqlDaoGeneratorException("Shape id must be a URI but was: " + shape.getId().stringValue());
	}

}
