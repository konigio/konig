package io.konig.maven.transform;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.path.PathFactory;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.TransformGenerator;

@Mojo( name = "generate")
public class KonigTransformMojo extends AbstractMojo{
	
	@Parameter
	private File shapesDir;
	
	@Parameter
	private File outDir;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		GcpShapeConfig.init();
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		ShapeManager shapeManager = new MemoryShapeManager();
		
		ShapeLoader shapeLoader = new ShapeLoader(null, shapeManager, nsManager);
		shapeLoader.loadAll(shapesDir);
		
		PathFactory pathFactory = new PathFactory(nsManager);
		
		TransformGenerator generator = new TransformGenerator(nsManager, shapeManager, pathFactory);
		
		try {
			generator.generateAll(outDir);
		} catch (ShapeTransformException | IOException e) {
			throw new MojoExecutionException("Failed to generate shape transformations", e);
		}
	}

}
