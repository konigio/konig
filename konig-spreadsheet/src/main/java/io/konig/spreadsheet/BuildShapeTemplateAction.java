package io.konig.spreadsheet;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;

import io.konig.core.NamespaceManager;
import io.konig.core.Term;
import io.konig.core.Term.Kind;
import io.konig.core.impl.BasicContext;
import io.konig.core.impl.SimpleLocalNameService;
import io.konig.core.util.IriTemplate;
import io.konig.core.util.SimpleValueFormat;
import io.konig.core.util.ValueFormat.Element;
import io.konig.shacl.Shape;
import io.konig.spreadsheet.WorkbookLoader.LocalNameLookup;

// TODO: Create a base class for BuildShapeTemplateAction and BuildClassTemplateAction

public class BuildShapeTemplateAction implements Action {

	private WorkbookLocation location;
	private WorkbookProcessor processor;
	private NamespaceManager nsManager;
	private SimpleLocalNameService localNameService;
	private Shape shape;
	private String templateText;
	

	private LocalNameLookup lookup;
	
	public BuildShapeTemplateAction(WorkbookLocation location, WorkbookProcessor processor, 
			NamespaceManager nsManager, SimpleLocalNameService localNameService, Shape shape,
			String templateText) {
		this.location = location;
		this.processor = processor;
		this.nsManager = nsManager;
		this.localNameService = localNameService;
		this.shape = shape;
		this.templateText = templateText;
	}



	@Override
	public void execute() throws SpreadsheetException {
		if (lookup == null) {
			List<Shape> shapeList = new ArrayList<>();
			shapeList.add(shape);
			lookup = new LocalNameLookup(localNameService, shapeList);
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
							processor.fail(location, "Namespace prefix not defined: {0}", prefix);
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
						processor.fail(
							location, "Template property not found: {0}", name);
						
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
		
		shape.setIriTemplate(iriTemplate);


	}

}
