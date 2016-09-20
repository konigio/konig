package io.konig.schemagen.maven;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import io.konig.showl.WorkbookToTurtleTransformer;

@Mojo( name = "generate-rdf", defaultPhase = LifecyclePhase.GENERATE_SOURCES )
public class KonigSheetMojo extends AbstractMojo {
	
	 @Parameter (defaultValue="${basedir}/src/dataModel.xlsx", property="workbookFile", required=true)
	 private File workbookFile;
	 
	 @Parameter (defaultValue="${basedir}/target/rdf", property="outDir", required=true)
	 private File outDir;
	 
	 public void execute() throws MojoExecutionException   {
		 try {

			 WorkbookToTurtleTransformer transformer = new WorkbookToTurtleTransformer();
			 transformer.transform(workbookFile, outDir);
		 } catch (Throwable oops) {
			 throw new MojoExecutionException("Failed to transform workbook to RDF", oops);
		 }
	 }
}
