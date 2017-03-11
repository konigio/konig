package io.konig.transform.sql.query;

import io.konig.shacl.Shape;
import io.konig.transform.MappedProperty;
import io.konig.transform.ShapePath;

public class JoinInfo {

	private ShapePath leftShape;
	private TableName leftTable;
	private MappedProperty leftProperty;
	
	private TableName rightTable;
	private ShapePath rightShapePath;
	private MappedProperty rightProperty;
	
	public JoinInfo(
		ShapePath leftShape,
		TableName leftTable, 
		MappedProperty leftProperty,
		TableName rightTable, 
		ShapePath rightShapePath,
		MappedProperty rightProperty
	) {
		this.leftShape = leftShape;
		this.leftTable = leftTable;
		this.leftProperty = leftProperty;
		this.rightTable = rightTable;
		this.rightShapePath = rightShapePath;
		this.rightProperty = rightProperty;
	}
	public TableName getLeftTable() {
		return leftTable;
	}
	
	public MappedProperty getLeftProperty() {
		return leftProperty;
	}
	public TableName getRightTable() {
		return rightTable;
	}
	public ShapePath getRightShapePath() {
		return rightShapePath;
	}
	public MappedProperty getRightProperty() {
		return rightProperty;
	}
	public ShapePath getLeftShapePath() {
		return leftShape;
	}
	
}
