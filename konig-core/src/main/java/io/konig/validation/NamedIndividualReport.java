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

public class NamedIndividualReport implements Comparable<NamedIndividualReport>, ReportElement {
	private URI individualId;
	private boolean nameHasWrongCase;
	private boolean requiresDescription;

	public NamedIndividualReport(URI individualId) {
		this.individualId = individualId;
	}

	public boolean getNameHasWrongCase() {
		return nameHasWrongCase;
	}

	public void setNameHasWrongCase(boolean nameHasWrongCase) {
		this.nameHasWrongCase = nameHasWrongCase;
	}

	public URI getIndividualId() {
		return individualId;
	}

	@Override
	public int compareTo(NamedIndividualReport o) {
		return individualId.stringValue().compareTo(o.getIndividualId().stringValue());
	}

	public boolean getRequiresDescription() {
		return requiresDescription;
	}

	public void setRequiresDescription(boolean requiresDescription) {
		this.requiresDescription = requiresDescription;
	}

	@Override
	public int errorCount() {
		return Sum.whereTrue(nameHasWrongCase);
	}
	
	
	
	

}
