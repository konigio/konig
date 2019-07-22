package io.konig.transform.beam;

import java.util.Collection;

import org.openrdf.model.URI;

import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TableSchema;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.pojo.SimplePojoFactory;
import io.konig.core.showl.ShowlChannel;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlUtil;
import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.gcp.deployment.BigqueryTableProperties;
import io.konig.gcp.deployment.BigqueryTableResource;
import io.konig.gcp.deployment.DeploymentConfig;
import io.konig.gcp.deployment.GcpConfigManager;
import io.konig.schemagen.gcp.BigQueryTableGenerator;
import io.konig.shacl.Shape;

/*
 * #%L
 * Konig Transform Beam
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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


public class BeamErrorTableGenerator {
	private BigQueryTableGenerator tableGenerator;
	private GcpConfigManager manager;
	private Graph graph;
	
	
	
	public BeamErrorTableGenerator(BigQueryTableGenerator tableGenerator, GcpConfigManager manager, Graph graph) {
		this.tableGenerator = tableGenerator;
		this.manager = manager;
		this.graph = graph;
	}

	public void generateAll(Collection<ShowlNodeShape> targetNodeList) throws BeamTransformGenerationException {
		for (ShowlNodeShape targetNode : targetNodeList) {
			visitTargetNode(targetNode);
		}
	}

	private void visitTargetNode(ShowlNodeShape targetShape) throws BeamTransformGenerationException {
		
		for (ShowlChannel channel : targetShape.getChannels()) {
			ShowlNodeShape sourceNode = channel.getSourceNode();
			if (!ShowlUtil.isEnumNode(sourceNode)) {
				generateErrorTable(sourceNode);
			}
		}
		
	}
	

	/**
	 * Generate the error log table for a given source Node Shape.
	 * @param sourceNode
	 * @throws BeamTransformGenerationException 
	 */
	private void generateErrorTable(ShowlNodeShape sourceNode) throws BeamTransformGenerationException {
		
		GoogleBigQueryTable bigquery = bigQueryTable(sourceNode);
		Shape shape = sourceNode.getShape();

		String datasetId = bigquery.getTableReference().getDatasetId();
		String tableId = BeamUtil.errorTableName(sourceNode.getShape().getIri());
		
		String resourceName = manager.bigqueryTableName(datasetId, tableId);

		DeploymentConfig config = manager.getConfig();
		BigqueryTableResource resource = config.findResource(resourceName, BigqueryTableResource.class);
		if (resource == null) {
			resource = new BigqueryTableResource();
			resource.setName(resourceName);
			config.addResource(resource);
			BigqueryTableProperties properties = null;
			
			URI resourceIri = manager.bigqueryTableIri(datasetId, tableId);
			
			
			Vertex v = graph.getVertex(resourceIri);
			if (v != null) {
				SimplePojoFactory factory = new SimplePojoFactory();
				properties = factory.create(v, BigqueryTableProperties.class);
			} else {
				properties = new BigqueryTableProperties();
			}

			resource.setProperties(properties);
			properties.setExternalDataConfiguration(bigquery.getExternalDataConfiguration());
			
			properties.setDatasetId(datasetId);
			TableReference tableReference = new TableReference();
			properties.setTableReference(tableReference);
			tableReference.setDatasetId(datasetId);
			tableReference.setTableId(tableId);
			
			TableSchema tableSchema = tableGenerator.toErrorSchema(shape);
			properties.setSchema(tableSchema);
			
			resource.produceMetadata().addDependency(manager.datasetName(datasetId));
			
		}
	}


	private GoogleBigQueryTable bigQueryTable(ShowlNodeShape sourceNode) throws BeamTransformGenerationException {
		ShowlNodeShape targetNode = sourceNode.getTargetNode().getRoot();
		DataSource ds = targetNode.getShapeDataSource().getDataSource();
		if (ds instanceof GoogleBigQueryTable) {
			return (GoogleBigQueryTable) ds;
		}
		throw new BeamTransformGenerationException("Expected BigQueryTable data source from " + targetNode.getPath());
	}

}
