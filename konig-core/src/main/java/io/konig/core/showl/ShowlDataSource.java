package io.konig.core.showl;

import io.konig.datasource.DataSource;

public class ShowlDataSource {
	
	private ShowlNodeShape dataSourceShape;
	private DataSource dataSource;
	
	public ShowlDataSource(ShowlNodeShape dataSourceShape, DataSource dataSource) {
		this.dataSourceShape = dataSourceShape;
		this.dataSource = dataSource;
		
		dataSourceShape.setShapeDataSource(this);
		
	}

	public ShowlNodeShape getDataSourceShape() {
		return dataSourceShape;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public String getPath() {
		StringBuilder builder = new StringBuilder();
		builder.append(dataSourceShape.getPath());
		builder.append(".shapeDataSource{");
		builder.append(dataSource.getIdentifier());
		builder.append("}");
		return builder.toString();
	}
	
	public String toString() {
		return getPath();
	}

}
