package io.konig.transform;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.core.Path;
import io.konig.core.util.IriTemplate;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

/**
 * A utility for writing a TransformFrame to a stream.  This is used primarily for
 * debugging purposes.
 * @author Greg McFall
 *
 */
public class TransformFrameWriter {
	private static final int indentSpaces = 3;
	
	private PrintWriter out;
	private int indent;
	
	

	public TransformFrameWriter(Writer writer) {
		this.out = (writer instanceof PrintWriter) ? (PrintWriter)writer : new PrintWriter(writer);
	}
	
	public TransformFrameWriter(OutputStream stream) {
		this(new OutputStreamWriter(stream));
	}
	

	public void write(TransformFrame frame) {
		
		Shape shape = frame.getTargetShape();
		Collection<MappedId> idMappings = frame.getIdMappings();
		Collection<TransformAttribute> attrList = frame.getAttributes();
		
		println("[");
		indent++;
		indent();
		print("targetShape <");
		print(shape.getId().toString());
		print(">");
		
		if (idMappings != null && !idMappings.isEmpty()) {
			println(';');
			indent();
			print("idMapping ");
			String comma = "";
			for (MappedId m : idMappings) {
				print(comma);
				comma = " , ";
				write(m);
			}
		}
		
		if (attrList != null && !attrList.isEmpty()) {
			println(';');
			indent();
			print("attribute ");
			String comma = "";
			for (TransformAttribute attr : attrList) {
				print(comma);
				comma = " , ";
				print(attr);
			}
			
		}
		
		
		indent--;
		out.println();
		indent();
		out.println("]");
		
		out.flush();
		
	}
	
	private void println() {
		out.println();
	}
	
	private void print(TransformAttribute attr) {
		URI predicate = attr.getPredicate();
		MappedProperty m = attr.getMappedProperty();
		TransformFrame embedded = attr.getEmbeddedFrame();
		
		println('[');
		indent++;
		indent();
		print("predicate ");
		printResource(predicate);
		println(';');
		
		if (m!=null) {
			indent();
			print("mappedProperty ");
			print(m);
			println();
		}
		
		if (embedded != null) {
			indent();
			print("embeddedFrame ");
			write(embedded);
			println();
		}
		
		indent--;
		indent();
		print(']');
		
		
		
		
	}

	private void print(MappedProperty m) {
		
		PropertyConstraint p = m.getProperty();
		Shape sourceShape = m.getSourceShape();
		int stepIndex = m.getStepIndex();
		IriTemplateInfo info = m.getTemplateInfo();
		
		println('[');
		indent++;
		
		indent();
		print("sourceShape ");
		printResource(sourceShape.getId());
		println(';');
		
		indent();
		print("property ");
		print(p);
		println(';');
		
		if (stepIndex >=0) {
			indent();
			print("stepIndex ");
			out.print(stepIndex);
			println(';');
		}
		
		if (info != null) {
			indent();
			print("templateInfo ");
			print(info);
			println(';');
		}
		
		indent--;
		out.println();
		indent();
		print(']');
		
	}

	private void print(PropertyConstraint p) {
		
		URI predicate = p.getPredicate();
		Path path = p.getEquivalentPath();
		
		println('[');
		indent++;
		
		indent();
		print("predicate ");
		printResource(predicate);
		
		if (path != null) {
			println(';');
			
			indent();
			print("equivalentPath \"");
			print(path.toString());
			println('"');
		} else {
			println();
		}
		
		
		indent--;
		indent();
		print(']');
		
	}

	private void write(MappedId m) {
		

		Shape sourceShape= m.getSourceShape();
		IriTemplateInfo info = m.getTemplateInfo();
		
		out.println("[");
		indent++;
		indent();
		print("sourceShape ");
		printResource(sourceShape.getId());
		
		
		if (m.getTemplateInfo() != null) {
			println(';');
			indent();
			print("templateInfo ");
			print(m.getTemplateInfo());
		}
		println();
		
		
		indent--;
		indent();
		
		
		
		print(']');
		
		
	}

	private void print(IriTemplateInfo info) {


		IriTemplate template = info.getTemplate();
		
		
		println("[");
		indent++;
		indent();
		print("template \"");
		print(template.toString());
		println("\";");
		
		indent();
		print("element ");
		print('(');
		indent++;
		
		for (IriTemplateElement e : info) {
			print(' ');
			print(e);
		}
		
		indent--;
		println();
		indent();
		println(')');
		
		
		indent--;
		indent();
		print(']');
		
	}

	private void print(IriTemplateElement e) {
		Namespace ns = e.getNamespace();
		
		println('[');
		indent++;

		indent();
		print("text '");
		print(e.getText());
		println("';");
		
		if (ns != null) {
			indent();
			print("namespace '");
			print(ns.getName());
			println("';");
		}
		
		if (e.getProperty() != null) {
			indent();
			print("property ");
			print(e.getProperty());
			println();
		} 
		
		indent--;
		indent();
		print(']');
		
	}

	private void printResource(Resource id) {
		
		if (id instanceof URI) {
			print('<');
			print(id.stringValue());
			print('>');
			
			
		} else {
			print("_:");
			print(id.stringValue());
		}
		
	}

	private void println(String text) {
		out.println(text);
	}
	
	private void println(char c) {
		out.println(c);
	}
	
	private void print(char c) {
		out.print(c);
	}
	
	private void print(String text) {
		out.print(text);
	}

	private void indent() {
		
		for (int i=0; i<indent*indentSpaces; i++) {
			out.print(' ');
		}
		
	}
}
