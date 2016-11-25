package io.konig.schemagen.plantuml;

import java.io.PrintWriter;
import java.io.Writer;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.core.OwlReasoner;
import io.konig.shacl.ClassManager;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class PlantumlClassDiagramGenerator {
	
	private static final String TAB = "   ";
	

	private OwlReasoner reasoner;
	private ShapeManager shapeManager;
	

	public PlantumlClassDiagramGenerator(OwlReasoner reasoner, ShapeManager shapeManager) {
		this.reasoner = reasoner;
		this.shapeManager = shapeManager;
	}

	public void generateDomainModel(ClassManager classManager, Writer out) throws PlantumlGeneratorException {
		Worker worker = new Worker(classManager, out);
		worker.run();
	}
	
	private class Worker {
		private ClassManager classManager;
		private PrintWriter out;

		public Worker(ClassManager classManager, Writer out) {
			this.classManager = classManager;
			this.out = (out instanceof PrintWriter) ? (PrintWriter) out : new PrintWriter(out);
		}
		
		private void run() {
			out.println("@startuml");
			for (Shape shape : classManager.list()) {
				handleShape(shape);
			}
			out.println("@enduml");
			out.flush();
		}

		private void handleShape(Shape shape) {
			
			URI domainClass = shape.getTargetClass();
			if (domainClass != null) {
				for (PropertyConstraint p : shape.getProperty()) {

					if (isObjectProperty(p)) {

						URI rangeClass = rangeClass(p);
						if (rangeClass != null  && !reasoner.isEnumerationClass(rangeClass)) {
							out.print(domainClass.getLocalName());
							out.print(" -- ");
							out.print(rangeClass.getLocalName());
							out.print(" : ");
							out.print(p.getPredicate().getLocalName());
							out.println(" >");
						}
						
					}
				}
			}
			
		}

		
		private URI rangeClass(PropertyConstraint p) {
			Resource valueClass = p.getValueClass();
			if (valueClass instanceof URI) {
				return (URI) valueClass;
			}
			Shape shape = p.getValueShape(shapeManager);
			if (shape != null) {
				Resource shapeId = shape.getId();
				if (shapeId instanceof URI) {
					return (URI) shapeId;
				}
			}
			
			return null;
		}

		private boolean isObjectProperty(PropertyConstraint p) {
			NodeKind nodeKind = p.getNodeKind();
			Resource valueClass = p.getValueClass();
			URI datatype = p.getDatatype();
			Resource valueShapeId = p.getValueShapeId();
			
			return 
				(datatype==null) ||
				(valueShapeId != null) ||
				nodeKind==NodeKind.IRI || 
				nodeKind==NodeKind.BlankNode ||
				(valueClass!=null && !reasoner.isDatatype(valueClass));
		}

		
		
		
	}

}
