package io.konig.maven.diff;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.delta.BNodeKeyFactoryList;
import io.konig.core.delta.ChangeSetFactory;
import io.konig.core.delta.CompositeBNodeKeyFactory;
import io.konig.core.delta.GenericBNodeKeyFactory;
import io.konig.core.delta.OwlRestrictionKeyFactory;
import io.konig.core.delta.PlainTextChangeSetReportWriter;
import io.konig.core.delta.SimpleKeyFactory;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.SH;

@Mojo( name = "diff" )
public class KonigSchemaDiffMojo extends AbstractMojo {

	@Parameter(property="konig.originalModelDir")
	private File originalModelDir;
	
	
	@Parameter(property="konig.revisedModelDir")
	private File revisedModelDir;
	
	@Parameter(property="konig.diffReportFile")
	private File diffReportFile;
	
	@Parameter
	private Set<String> ignoreNamespace;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		if (originalModelDir == null || revisedModelDir == null) {
			return;
		}
		
		
		try {
			NamespaceManager nsManager = new MemoryNamespaceManager();
			nsManager.add("rdf", RDF.NAMESPACE);
			nsManager.add("rdfs", RDFS.NAMESPACE);
			nsManager.add("sh", SH.NAMESPACE);
			nsManager.add("owl", OWL.NAMESPACE);
			Graph original = new MemoryGraph();
			Graph revised = new MemoryGraph();
			CompositeBNodeKeyFactory keyFactory = new CompositeBNodeKeyFactory();
			keyFactory.register(SH.property, new SimpleKeyFactory(SH.property, SH.predicate));
			keyFactory.register(RDFS.SUBCLASSOF, new OwlRestrictionKeyFactory());
			
			BNodeKeyFactoryList keyFactoryList = new BNodeKeyFactoryList();
			keyFactoryList.add(keyFactory);
			keyFactoryList.add(new GenericBNodeKeyFactory());
			
			RdfUtil.loadTurtle(originalModelDir, original, nsManager);
			RdfUtil.loadTurtle(revisedModelDir, revised, nsManager);
			
			ChangeSetFactory factory = new ChangeSetFactory();
			if (ignoreNamespace != null && !ignoreNamespace.isEmpty()) {
				factory.setIgnoreNamespace(ignoreNamespace);
			}
			Graph changeSet = factory.createChangeSet(original, revised, keyFactoryList);
			
			PlainTextChangeSetReportWriter reporter = new PlainTextChangeSetReportWriter(nsManager);
			
			if (diffReportFile != null) {
				diffReportFile.getParentFile().mkdirs();
				FileWriter writer = new FileWriter(diffReportFile);
				try {
					reporter.write(changeSet, writer);
				} finally {
					writer.close();
				}
			} else {
				reporter.write(changeSet, System.out);
			}
			
			
			
		} catch (RDFParseException | RDFHandlerException | IOException e) {
			throw new MojoExecutionException("Failed to compute model difference", e);
		}
	}

}
