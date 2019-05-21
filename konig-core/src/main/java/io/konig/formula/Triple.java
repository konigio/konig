package io.konig.formula;

/*
 * #%L
 * Konig Core
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


import io.konig.core.io.PrettyPrintWriter;

public class Triple extends AbstractFormula {
	private PathTerm subject;
	private IriValue predicate;
	private PathTerm object;

	public Triple(PathTerm subject, IriValue predicate, PathTerm object) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}
	
	public PathTerm getSubject() {
		return subject;
	}


	public IriValue getPredicate() {
		return predicate;
	}


	public PathTerm getObject() {
		return object;
	}
	
	@Override
	public Triple clone() {
		return new Triple(subject.clone(), predicate.clone(), object.clone());
	}


	@Override
	public void print(PrettyPrintWriter out) {
		out.print(subject);
		out.print(' ');
		out.print(predicate);
		out.print(' ');
		out.print(object);
		out.print(" .");

	}

	@Override
	public void dispatch(FormulaVisitor visitor) {

		visitor.enter(this);
		subject.dispatch(visitor);
		predicate.dispatch(visitor);
		object.dispatch(visitor);
		visitor.exit(this);

	}

}
