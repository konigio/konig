package io.konig.core.showl;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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


import java.util.Set;

public class ArrayFilterStatement implements ShowlStatement {
	
	private ShowlPropertyShape multiValuedProperty;
	private ShowlNodeShape enumTargetNode;

	public ArrayFilterStatement(ShowlPropertyShape multiValuedProperty, ShowlNodeShape enumTargetNode) {
		this.multiValuedProperty = multiValuedProperty;
		this.enumTargetNode = enumTargetNode;
	}

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNode, Set<ShowlPropertyShape> set) {
		multiValuedProperty.getSelectedExpression().addDeclaredProperties(sourceNode, set);

	}

	@Override
	public void addProperties(Set<ShowlPropertyShape> set) {
		
		multiValuedProperty.getSelectedExpression().addProperties(set);

	}

	public ShowlPropertyShape getMultiValuedProperty() {
		return multiValuedProperty;
	}

	public ShowlNodeShape getEnumTargetNode() {
		return enumTargetNode;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ArrayFilter(");
		builder.append(multiValuedProperty.getPath());
		builder.append(')');
		return builder.toString();
	}

}
