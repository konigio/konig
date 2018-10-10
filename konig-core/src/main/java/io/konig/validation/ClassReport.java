package io.konig.validation;

/*
 * #%L
 * Konig Core
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

public class ClassReport implements Comparable<ClassReport> {
	private URI classId;
	private boolean nameHasWrongCase;
	private boolean requiresDescription;
	

	public ClassReport(URI classId) {
		this.classId = classId;
	}
	
	public URI getClassId() {
		return classId;
	}

	public boolean getNameHasWrongCase() {
		return nameHasWrongCase;
	}

	public void setNameHasWrongCase(boolean nameHasWrongCase) {
		this.nameHasWrongCase = nameHasWrongCase;
	}
	

	public boolean getRequiresDescription() {
		return requiresDescription;
	}

	public void setRequiresDescription(boolean requiresDescription) {
		this.requiresDescription = requiresDescription;
	}

	@Override
	public int compareTo(ClassReport o) {
		return classId.stringValue().compareTo(o.getClassId().stringValue());
	}
	
	

}
