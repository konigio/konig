package io.konig.transform.sql.query;

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


import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.Shape;
import io.konig.transform.MappedProperty;
import io.konig.transform.ShapePath;


public class JoinInfo extends AbstractPrettyPrintable {

	private JoinElement left;
	private JoinElement right;
	
	public JoinInfo(JoinElement left, JoinElement right) {
		this.left = left;
		this.right = right;
	}

	public JoinElement getLeft() {
		return left;
	}

	public JoinElement getRight() {
		return right;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		out.beginObject(this);
		out.field("left", left);
		out.field("right", right);
		out.endObject();
		
	}
	
	
	
}
