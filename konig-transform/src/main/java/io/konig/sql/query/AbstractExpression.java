package io.konig.sql.query;

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


import java.io.StringWriter;

import io.konig.core.io.PrettyPrintWriter;
import io.konig.core.io.PrettyPrintable;

public abstract class AbstractExpression implements QueryExpression {

	public String toString() {
		StringWriter buffer = new StringWriter();
		PrettyPrintWriter out = new PrettyPrintWriter(buffer);
		print(out);
		out.close();
		return buffer.toString();
	}
	
	@Override
	public void dispatch(QueryExpressionVisitor visitor) {
		visitor.enter(this);
		dispatchProperties(visitor);
		visitor.leave(this);
	}

	abstract protected void dispatchProperties(QueryExpressionVisitor visitor);

	protected void visit(QueryExpressionVisitor visitor, String predicate, QueryExpression object) {
		if (object != null) {
			visitor.visit(this, predicate, object);
			object.dispatch(visitor);
		}
	
	}
}
