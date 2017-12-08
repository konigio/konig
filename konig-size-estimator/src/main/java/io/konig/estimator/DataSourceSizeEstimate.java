package io.konig.estimator;

import io.konig.datasource.DataSource;
import io.konig.shacl.Shape;

public class DataSourceSizeEstimate {

	private Shape shape;
	private DataSource dataSource;
	private int recordCount;
	private int sizeSum;
	
	public DataSourceSizeEstimate(Shape shape, DataSource dataSource) {
		this.shape = shape;
		this.dataSource = dataSource;
	}
	
	public void incrementSize(int increment) {
		sizeSum += increment;
		recordCount++;
	}

	public Shape getShape() {
		return shape;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public int getRecordCount() {
		return recordCount;
	}

	public int getSizeSum() {
		return sizeSum;
	}
	
	public int averageSize() {
		return (int) Math.ceil(((double)sizeSum)/recordCount);
	}
	
}
