package io.konig.transform.model;

/*
 * #%L
 * Konig Transform
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


import org.openrdf.model.URI;

/**
 * A representation of a SHACL PropertyShape decorated with information useful for generating transforms.
 * @author Greg McFall
 *
 */
public interface TPropertyShape {

	TNodeShape getOwner();
	URI getPredicate();
	
	TProperty getPropertyGroup();
	void setPropertyGroup(TProperty group);
	
	TExpression getValueExpression() throws ShapeTransformException;
	TProperty getValueExpressionGroup();
	
	TNodeShape getValueShape();
	int countValues();
	TProperty assignValue() throws ShapeTransformException;
	String getPath();

}
