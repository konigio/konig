package io.konig.maven.diff;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import info.aduna.io.FileUtil;
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
	private static final String ARCHIVE_DIR = "target/archive/";

	@Parameter(property="konig.originalModelDir")
	private File originalModelDir;
	
	
	@Parameter(property="konig.revisedModelDir")
	private File revisedModelDir;
	
	@Parameter(property="konig.diffReportFile")
	private File diffReportFile;
	
	@Parameter(property="konig.originalVersion")
	private String originalVersion;

	@Parameter(property="konig.originalModelPath")
	private String originalModelPath;
	
	@Parameter(property="konig.revisedVersion")
	private String revisedVersion;

	@Parameter(property="konig.revisedModelPath")
	private String revisedModelPath;
	
	@Parameter
	private Set<String> ignoreNamespace;
	
	@Parameter(property="konig.excludedTerms")
	private String excludedTerms;
	
	@Component
	private MavenProject mavenProject;

	@Component
	private MavenSession mavenSession;

	@Component
	private BuildPluginManager pluginManager;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		unpack();
		
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
			setExclusions(reporter);
			
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

	private void setExclusions(PlainTextChangeSetReportWriter reporter) {
		if (excludedTerms != null) {
			StringTokenizer tokenizer = new StringTokenizer(excludedTerms, " \t\r\n");
			while (tokenizer.hasMoreTokens()) {
				String term = tokenizer.nextToken();
				reporter.exclude(new URIImpl(term));
			}
		}
		
	}

	private void unpack() throws MojoExecutionException {
		
		if (originalVersion != null) {
			originalModelDir = unpack(originalVersion, originalModelPath, "originalModelPath");
		}
		
		if (revisedVersion != null) {
			revisedModelDir = unpack(revisedVersion, revisedModelPath, "revisedModelPath");
		}
		
	}

	private File unpack(String version, String path, String paramName) throws MojoExecutionException {
		
		if (path == null) {
			throw new MojoExecutionException("The '" + paramName + "' configuration parameter must be defined.");
		}
		
		String groupId = mavenProject.getGroupId();
		String artifactId = mavenProject.getArtifactId();
		
		File basedir = mavenProject.getBasedir();
		File archiveDir = new File(basedir, ARCHIVE_DIR + version);
		
		deleteDir(archiveDir);
		archiveDir.mkdirs();
		
		executeMojo(
			plugin(
				groupId("org.apache.maven.plugins"),
				artifactId("maven-dependency-plugin"),
				version("2.10")
			),
			goal("unpack"),
			configuration(
				element(name("outputDirectory"), archiveDir.getAbsolutePath()),
				element(name("artifactItems"),
					element(name("artifactItem"), 
						element(name("groupId"), groupId),
						element(name("artifactId"), artifactId),
						element(name("version"), version),
						element(name("classifier"), "bin"),
						element(name("overWrite"), "true"),
						element(name("type"), "zip")
					)
				)
			),
			executionEnvironment(
				mavenProject,
				mavenSession,
				pluginManager
			)
		);
		return new File(archiveDir, path);
	}

	private void deleteDir(File doomed) throws MojoExecutionException {
		
		if (doomed.exists()) {
			try {
				FileUtil.deleteDir(doomed);
			} catch (IOException e) {
				throw new MojoExecutionException("Failed to delete directory: " + doomed.getAbsolutePath(), e);
			}
		}
		
	}

}
