package io.konig.schemagen.io;

import java.io.File;
import java.io.IOException;

import org.apache.velocity.VelocityContext;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.schemagen.gcp.TurtleGenerator;

public class GenericEmitter implements Emitter {
	
	private URI owlClass;
	private File outDir;
	private VelocityContext context;

	

	public GenericEmitter(URI owlClass, File outDir, VelocityContext context) {
		this.owlClass = owlClass;
		this.outDir = outDir;
		this.context = context;
	}



	@Override
	public void emit(Graph graph) throws IOException, KonigException {
		TurtleGenerator generator = new TurtleGenerator();
		try {
			generator.generateAll(owlClass, outDir, graph, context);
		} catch (RDFHandlerException e) {
			throw new KonigException("Failed to generate all instances of " + owlClass.getLocalName());
		}

	}

}
