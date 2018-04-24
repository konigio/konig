package io.konig.transform.proto;

public class ShapeJoinExpression implements ProtoFromItem {

	private ShapeModel shapeModel;
	private ProtoBooleanExpression condition;
	
	public ShapeJoinExpression(ShapeModel shapeModel, ProtoBooleanExpression condition) {
		this.shapeModel = shapeModel;
		this.condition = condition;
	}

	public ShapeModel getShapeModel() {
		return shapeModel;
	}

	public ProtoBooleanExpression getCondition() {
		return condition;
	}

	
	
}
