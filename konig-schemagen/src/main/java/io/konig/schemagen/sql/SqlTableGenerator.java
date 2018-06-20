package io.konig.schemagen.sql;

import java.io.File;
import java.io.IOException;

/*
 * #%L
 * Konig Schema Generator
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


import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Konig;
import io.konig.schemagen.SchemaGeneratorException;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class SqlTableGenerator {
	
	private static final Logger LOG = LoggerFactory.getLogger(SqlTableGenerator.class);
	private SqlTableNameFactory nameFactory;
	private SqlDatatypeMapper datatypeMapper;
	private Map<String,String> abbreviations;
	
	public SqlTableGenerator() {
		nameFactory = new SqlTableNameFactory();
		datatypeMapper = new SqlDatatypeMapper();
	}	
	public SqlTableGenerator(SqlDatatypeMapper datatypeMapper) {
		nameFactory = new SqlTableNameFactory();
		this.datatypeMapper = datatypeMapper;
	}
	
	public SqlTable generateTable(Shape shape,File abbrevDir) throws SchemaGeneratorException {
		
		String tableName = nameFactory.getTableName(shape);
		SqlTable table = new SqlTable(tableName);
		if(shape.getOr()!=null){
			
			ShapeMerger merger = new ShapeMerger();
			try {
				shape = merger.merge(shape);
			} catch (ShapeMergeException e) {
				throw new SchemaGeneratorException(e);
			}
			
		}addIdColumn(shape,table);
		try {
			setAbbreviations(abbrevDir);
		} catch (RDFParseException | RDFHandlerException | IOException e) {
			throw new SchemaGeneratorException(e);
		}
		for (PropertyConstraint p : shape.getProperty()) {
			SqlColumn column = column(shape, p);
			if (column != null) {
				table.addColumn(column);
			}
		}
		return table;
	}


	private void setAbbreviations(File abbrevDir) throws RDFParseException, RDFHandlerException, IOException {
		Graph graph=new MemoryGraph();
		if(abbrevDir!=null && abbrevDir.exists()){
			RdfUtil.loadTurtle(abbrevDir, graph);
		}
			abbreviations=new HashMap<String,String>();
			List<Vertex> vertexList=graph.v(SKOS.CONCEPT).in(RDF.TYPE).toVertexList();
			if(vertexList!=null){
				for(Vertex v:vertexList){
					String abbreviation=v.getValue(Konig.abbreviationLabel).stringValue();
					String description = v.getValue(SKOS.PREF_LABEL).stringValue();
					abbreviations.put(description, abbreviation);
				}
			}
			
		
	}
	private void addIdColumn(Shape shape, SqlTable table) {
		SqlKeyType keyType = SqlKeyType.PRIMARY_KEY;
		if(shape.getNodeKind() == NodeKind.IRI){
			for (PropertyConstraint p : shape.getProperty()) {
				if (p.getStereotype() != null && Konig.primaryKey.equals(p.getStereotype()) ) {
						keyType = null;
				}
			}

			SqlColumn column = new SqlColumn("id", FacetedSqlDatatype.IRI, keyType, false);
			table.addColumn(column);
		}
		
	}

	private SqlColumn column(Shape shape, PropertyConstraint p) {
		
		URI predicate = p.getPredicate();	
		SqlKeyType keyType=SqlTableGeneratorUtil.getKeyType(p);
		if(!SqlTableGeneratorUtil.isValidRdbmsShape(shape) && 
				SqlKeyType.SYNTHETIC_KEY.equals(keyType)){
			LOG.error("konig:synthicKey is applicable only for shapes with datasource GoogleCloudSqlTable or AwsAurora");
			p.setStereotype(null);
			keyType=null;
		}
		if (predicate != null) {
			if (p.getShape() != null) {
				String message = MessageFormat.format("Nested shape is not allowed in property ''{0}'' in {1}", 
						predicate.getLocalName(), shape.getId().stringValue());
				throw new SchemaGeneratorException(message);
			}
			FacetedSqlDatatype datatype = datatypeMapper.type(p);
			boolean nullable = !p.isRequiredSingleValue();	
			String columnName=predicate.getLocalName();
			if(shape.getUsesAbbreviationScheme()!=null){
				if(abbreviations!=null){
					for(String key:abbreviations.keySet()){
						columnName=columnName.replace(key,abbreviations.get(key));
					}
				}
			}
			return new SqlColumn(columnName, datatype, keyType, nullable);
		}
		return null;
	}
}
