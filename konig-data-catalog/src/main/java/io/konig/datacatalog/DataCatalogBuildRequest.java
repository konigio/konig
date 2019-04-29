package io.konig.datacatalog;

/*
 * #%L
 * Konig Data Catalog
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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


import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.velocity.app.VelocityEngine;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SKOS;

import io.konig.core.Graph;
import io.konig.core.NamespaceInfoManager;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.showl.ShowlManager;
import io.konig.core.vocab.SH;
import io.konig.schema.EnumerationReasoner;
import io.konig.shacl.ClassStructure;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class DataCatalogBuildRequest {
	private static final String ALL_CLASSES = "AllClasses";
	private String siteName;
	private URI ontologyId;
	private File outDir;
	private File exampleDir;
	private Graph graph; 
	private ShapeManager shapeManager;
	private Set<URI> ontologyInclude;
	private Set<URI> ontologyExclude;
	private List<Vertex> ontologyList;
	private NamespaceInfoManager namespaceInfoManager = new NamespaceInfoManager();
	
	private PathFactory pathFactory;
	private ClassStructure classStructure;
	private DataCatalogBuilder catalogBuilder;
	private VelocityEngine engine;
	private CatalogFileFactory fileFactory;
	private OwlReasoner owlReasoner;
	
	private SubjectManager subjectManager = null;
	
	private boolean showUndefinedClass = false;
	
	public DataCatalogBuildRequest() {
		
	}

	public boolean isShowUndefinedClass() {
		return showUndefinedClass;
	}
	
	public SubjectManager getSubjectMananger() {
		if (subjectManager == null) {
			subjectManager = new SubjectManager();
			subjectManager.load(graph);
		}
		return subjectManager;
	}

	public void setShowUndefinedClass(boolean showUndefinedClass) {
		this.showUndefinedClass = showUndefinedClass;
	}

	public URI getOntologyId() {
		return ontologyId;
	}

	public void setOntologyId(URI ontologyId) {
		this.ontologyId = ontologyId;
	}

	public File getOutDir() {
		return outDir;
	}

	public void setOutDir(File outDir) {
		this.outDir = outDir;
	}

	public File getExampleDir() {
		return exampleDir;
	}

	public void setExampleDir(File exampleDir) {
		this.exampleDir = exampleDir;
	}

	public Graph getGraph() {
		return graph;
	}

	public void setGraph(Graph graph) {
		this.graph = graph;
		namespaceInfoManager.load(graph);
		EnumerationReasoner reasoner = new EnumerationReasoner();
		reasoner.annotateEnumerationNamespaces(graph, namespaceInfoManager);
	}

	public ShapeManager getShapeManager() {
		return shapeManager;
	}

	public void setShapeManager(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public Set<URI> getOntologyInclude() {
		return ontologyInclude;
	}

	public void setOntologyInclude(Set<URI> ontologySet) {
		this.ontologyInclude = ontologySet;
	}

	public List<Vertex> getOntologyList() {
		return ontologyList;
	}

	public void setOntologyList(List<Vertex> ontologyList) {
		this.ontologyList = ontologyList;
	}

	public Set<URI> getOntologyExclude() {
		return ontologyExclude;
	}

	public void setOntologyExclude(Set<URI> ontologyExclude) {
		this.ontologyExclude = ontologyExclude;
	}
	
	

	public void useDefaultOntologyList() throws DataCatalogException {
		if (graph == null) {
			throw new DataCatalogException("graph must be defined");
		}
		
		Set<URI> exclude = ontologyExclude();
		List<Vertex> list = graph.v(OWL.ONTOLOGY).in(RDF.TYPE).toVertexList();
		Iterator<Vertex> sequence = list.iterator();
		while (sequence.hasNext()) {
			Vertex v = sequence.next();
			if (exclude.contains(v.getId())) {
				sequence.remove();
			}
		}
		ontologyList = list;
	}

	private Set<URI> ontologyExclude() {
		if (ontologyExclude == null) {
			Set<URI> set = new HashSet<>();
			add(set, RDF.NAMESPACE);
			add(set, RDFS.NAMESPACE);
			add(set, OWL.NAMESPACE);
			add(set, SH.NAMESPACE);
			return set;
		}
		return ontologyExclude;
	}

	private void add(Set<URI> set, String value) {
		set.add(uri(value));
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}
	
	PathFactory getPathFactory() {
		if (pathFactory == null) {
			OwlReasoner reasoner = classStructure.getReasoner();
			reasoner = new DataCatalogReasoner(reasoner.getGraph());
			NamespaceManager nsManager = graph.getNamespaceManager();
			pathFactory = new DataCatalogPathFactory(reasoner, nsManager, DataCatalogBuilder.DCAT_PREFIX);
		}
		return pathFactory;
	}

	ClassStructure getClassStructure() {
		return classStructure;
	}

	void setClassStructure(ClassStructure classStructure) {
		this.classStructure = classStructure;
	}

	DataCatalogBuilder getCatalogBuilder() {
		return catalogBuilder;
	}

	void setCatalogBuilder(DataCatalogBuilder catalogBuilder) {
		this.catalogBuilder = catalogBuilder;
	}

	VelocityEngine getEngine() {
		return engine;
	}

	void setEngine(VelocityEngine engine) {
		this.engine = engine;
	}

	public NamespaceInfoManager getNamespaceInfoManager() {
		return namespaceInfoManager;
	}

	public CatalogFileFactory getFileFactory() {
		return fileFactory;
	}

	public void setFileFactory(CatalogFileFactory fileFactory) {
		this.fileFactory = fileFactory;
	}

	public OwlReasoner getOwlReasoner() {
		if (owlReasoner == null) {
			owlReasoner = new OwlReasoner(graph);
		}
		return owlReasoner;
	}
	
	public String classSubjects(Shape shape) {
		URI targetClass = shape.getTargetClass();
		if (targetClass != null) {
			Vertex owlClass = graph.getVertex(targetClass);
			if (owlClass != null) {
				return classSubjects(owlClass);
			}
		}
		return ALL_CLASSES;
	}

	public String classSubjects(Vertex owlClass) {
		SubjectManager subjectManager = getSubjectMananger();
		if (!subjectManager.isEmpty()) {
			Set<Vertex> subjectSet = owlClass.getVertexSet(SKOS.BROADER);
			if (!subjectSet.isEmpty()) {
				StringBuilder builder = new StringBuilder();
				builder.append(ALL_CLASSES);
				for (Vertex s : subjectSet) {
					ClassifiedName cname = subjectManager.getSubjectName(s.getId());
					if (cname != null) {
						builder.append(' ');
						builder.append(cname.getClassName());
					}
				}
				return builder.toString();
			}
		}
		return ALL_CLASSES;
	}
	
	
}
