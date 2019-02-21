package io.konig.spreadsheet;

/*
 * #%L
 * Konig Spreadsheet
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


import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.Term;
import io.konig.core.Term.Kind;
import io.konig.core.impl.BasicContext;
import io.konig.core.impl.SimpleLocalNameService;
import io.konig.core.util.IriTemplate;
import io.konig.core.util.SimpleValueFormat;
import io.konig.core.util.ValueFormat.Element;
import io.konig.core.vocab.Konig;
import io.konig.shacl.ShapeManager;
import io.konig.spreadsheet.WorkbookLoader.LocalNameLookup;

public class ClassTemplateBuilder {

	private URI classId;
	private String templateText;
	private LocalNameLookup lookup;
	private SimpleLocalNameService localNameService;

	public ClassTemplateBuilder(SimpleLocalNameService localNameService, URI classId, String iriTemplate) {
		this.classId = classId;
		this.templateText = iriTemplate;
		this.localNameService = localNameService;
	}
	
	public String getTemplateText() {
		return templateText;
	}

	public void createTemplate(Graph graph, ShapeManager shapeManager, NamespaceManager nsManager) throws SpreadsheetException {
		if (lookup == null) {
			lookup = new LocalNameLookup(localNameService, shapeManager.getShapesByTargetClass(classId));
		}
		SimpleValueFormat format = new SimpleValueFormat(templateText);
		BasicContext context = new BasicContext(null);
		IriTemplate iriTemplate = new IriTemplate();
		iriTemplate.setContext(context);

		for (Element e : format.toList()) {

			switch (e.getType()) {

			case TEXT:
				iriTemplate.addText(e.getText());
				break;

			case VARIABLE:
				String name = e.getText();
				iriTemplate.addVariable(name);
				int colon = name.indexOf(':');
				if (colon > 0) {
					String prefix = name.substring(0, colon);

					Term nsTerm = context.getTerm(prefix);
					if (nsTerm == null) {
						Namespace ns = nsManager.findByPrefix(prefix);
						if (ns != null) {
							nsTerm = new Term(prefix, ns.getName(), Kind.NAMESPACE);
							context.add(nsTerm);
						} else {
							throw new SpreadsheetException("Namespace prefix not defined: " + prefix);
						}
					}
				} else {
					URI p = lookup.toQualifiedIri(name);
					if (p == null) {

						Namespace ns = nsManager.findByPrefix(name);
						if (ns != null) {
							context.add(new Term(name, ns.getName(), Kind.NAMESPACE));
							break;
						}

						throw new SpreadsheetException(
								"For Class <" + classId + "> template property not found: " + name);
					}
					String namespace = p.getNamespace();
					Namespace ns = nsManager.findByName(namespace);
					if (ns == null) {
						context.add(new Term(name, p.stringValue(), Kind.PROPERTY));
					} else {
						String prefix = ns.getPrefix();
						Term nsTerm = context.getTerm(prefix);
						if (nsTerm == null) {
							context.add(new Term(prefix, namespace, Kind.NAMESPACE));
						}
						context.add(new Term(name, prefix + ":" + name, Kind.PROPERTY));
					}
				}
				break;
			}
		}
		context.sort();

		Literal literal = new LiteralImpl(iriTemplate.toString());
		graph.edge(classId, Konig.iriTemplate, literal);
	}

	
}
