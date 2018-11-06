package io.konig.schemagen.gcp;

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


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.konig.core.KonigException;
import io.konig.core.OwlReasoner;
import io.konig.core.project.ProjectFolder;
import io.konig.gcp.common.BigQueryTableListener;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeHandler;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapeNamer;
import io.konig.shacl.ShapeVisitor;
import io.konig.transform.proto.BigQueryChannelFactory;
import io.konig.transform.proto.ShapeModelFactory;

public class GoogleCloudResourceGenerator {
	
	private ShapeManager shapeManager;
	private OwlReasoner owlReasoner;
	private List<ShapeVisitor> visitors = new ArrayList<>();
	private ShapeModelFactory shapeModelFactory;
	private BigQueryTableListener bigqueryTableListener;
	
	public GoogleCloudResourceGenerator(ShapeManager shapeManager,OwlReasoner owlReasoner ) {
		this.shapeManager = shapeManager;
		this.owlReasoner = owlReasoner;
	}
	
	
	
	public BigQueryTableListener getBigqueryTableListener() {
		return bigqueryTableListener;
	}



	public void setBigqueryTableListener(BigQueryTableListener bigqueryTableListener) {
		this.bigqueryTableListener = bigqueryTableListener;
	}



	public void add(ShapeVisitor visitor) {
		if (visitor != null) {
			visitors.add(visitor);
		}
	}
	
	public void addBigQueryGenerator(ProjectFolder folder) {

		BigQueryTableWriter tableWriter = new BigQueryTableWriter(folder);
		BigQueryTableGenerator tableGenerator = new BigQueryTableGenerator(shapeManager, null, owlReasoner);
		
		ShapeToBigQueryTransformer transformer = new ShapeToBigQueryTransformer(tableGenerator, tableWriter, shapeModelFactory());
		transformer.setBigQueryTableListener(bigqueryTableListener);
		add(transformer);
		
	}
	
	private ShapeModelFactory shapeModelFactory() {
		if (shapeModelFactory == null) {
			shapeModelFactory = new ShapeModelFactory(shapeManager, new BigQueryChannelFactory(), owlReasoner);
		}
		return shapeModelFactory;
	}
	
	public void addBigQueryViewGenerator(ProjectFolder bigQueryViewDir) {
		
		BigQueryTableWriter viewWriter = new BigQueryTableWriter(bigQueryViewDir);
		BigQueryTableGenerator tableGenerator = new BigQueryTableGenerator(shapeManager, null, owlReasoner);
		
		ShapeToBigQueryTransformer transformer = new ShapeToBigQueryTransformer(tableGenerator, viewWriter ,shapeModelFactory());
		transformer.setBigQueryTableListener(bigqueryTableListener);
		add(transformer);
		
	}
	public void addCloudStorageBucketWriter(File bucketDir) {
		add(new GoogleCloudStorageBucketWriter(bucketDir));
	}

	public void dispatch(List<Shape> shapeList) throws KonigException {
		beginTraversal();
		
		for (Shape shape : shapeList) {
			for (ShapeVisitor visitor : visitors) {
				visitor.visit(shape);
			}
		}
		
		endTraversal();
	}

	private void endTraversal() {

		for (ShapeVisitor visitor : visitors) {
			if (visitor instanceof ShapeHandler) {
				ShapeHandler handler = (ShapeHandler) visitor;
				handler.endShapeTraversal();
			}
		}
		
	}

	private void beginTraversal() {
		for (ShapeVisitor visitor : visitors) {
			if (visitor instanceof ShapeHandler) {
				ShapeHandler handler = (ShapeHandler) visitor;
				handler.beginShapeTraversal();
			}
		}
		
	}

}
