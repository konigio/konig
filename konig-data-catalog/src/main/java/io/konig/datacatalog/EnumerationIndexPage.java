package io.konig.datacatalog;

/*
 * #%L
 * Konig Data Catalog
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


import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;

public class EnumerationIndexPage extends ClassIndexPage {

	
	protected void filter(OwlReasoner reasoner, List<Vertex> source) {
		filter(reasoner, source, false);
		
	}
	
	protected Template getTemplate(VelocityEngine engine) {
		return engine.getTemplate(ENTITY_LIST_TEMPLATE);
	}

	
	protected void buildContext(VelocityContext context) {
		context.put("entityIndexClass", "");
		context.put("enumIndexClass", "activeLink");
		
	}
}
