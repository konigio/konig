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


import java.util.Iterator;

import io.konig.core.KonigException;
import io.konig.transform.rule.DataChannel;

public class ProtoFromItemIterator implements Iterator<ShapeModel> {

	private ProtoFromItem current;
	
	public ProtoFromItemIterator(ProtoFromItem first) {
		current = first;
	}

	@Override
	public boolean hasNext() {
		return current!=null;
	}

	@Override
	public ShapeModel next() {
		ShapeModel shapeModel = null;
		if (current instanceof ShapeModel) {
			shapeModel = (ShapeModel) current;
			current = null;
		} else if (current instanceof ProtoJoinExpression) {
			ProtoJoinExpression join = (ProtoJoinExpression) current;
			shapeModel = join.getLeft();
			current = join.getRight();
		} else {
			throw new KonigException("Unsupported type of ProtoFromItem: " + current.getClass().getName());
		}
		return shapeModel;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
		
	}

}
