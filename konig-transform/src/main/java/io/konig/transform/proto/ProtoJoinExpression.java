package io.konig.transform.proto;

/*
 * #%L
 * Konig Transform
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


/**
 * An entity that expresses a constraint for joining two shapes.  It consists of left and right FromItem instances,
 * and a boolean expression for the join condition.
 * 
 * <h3>Example</h3>
 * Suppose you want to lookup the birth place of the founder of the 
 * company that manufactures a given product.  You might formulate a SQL query
 * like this...
 * <pre>
 *   SELECT Place.name
 *   FROM
 *   	Product
 *   JOIN
 *   	Organization ON Organization.id = Product.manufacturer
 *   JOIN
 *   	Person ON Person.id = Organization.founder
 *   JOIN
 *   	Place ON Place.id = Person.birthPlace
 *   WHERE
 *   	Product.id={productIdValue}
 * </pre>
 * Schematically, the FromItem within this query would like something like this...
 * <pre>
 *   ProtoJoinExpression(
 *   	Product, 
 *   	ProtoJoinExpression(
 *   		Organization,
 *   		ProtoJoinExpression(
 *   			Person,
 *   			Place,
 *   			ProtoJoinExpression(EQUALS, birthPlace, id)
 *   		)
 *   		ProtoBooleanExpression(EQUALS, founder, id)
 *   	),
 *   	ProtoBooleanExpression(EQUALS, manufacturer, id)
 *   )
 * </pre>
 * 
 *
 * @author Greg McFall
 *
 */
public class ProtoJoinExpression implements ProtoFromItem {

	private ShapeModel left;
	private ProtoFromItem right;
	private ProtoBooleanExpression condition;
	
	public ProtoJoinExpression(ShapeModel left, ProtoFromItem right, ProtoBooleanExpression condition) {
		this.left = left;
		this.right = right;
		this.condition = condition;
	}
	
	public ShapeModel getLeft() {
		return left;
	}
	
	public ProtoFromItem getRight() {
		return right;
	}
	
	public ProtoBooleanExpression getCondition() {
		return condition;
	}

	public void setLeft(ShapeModel left) {
		this.left = left;
	}

	public void setRight(ProtoFromItem right) {
		this.right = right;
	}
	
	public ShapeModel getRightShapeModel() {
		return right instanceof ShapeModel ? (ShapeModel) right : null;
	}

	public void setCondition(ProtoBooleanExpression condition) {
		this.condition = condition;
	}

	@Override
	public ShapeModel first() {
		return left;
	}

	@Override
	public ProtoFromItem rest() {
		return right;
	}
	
	
}
