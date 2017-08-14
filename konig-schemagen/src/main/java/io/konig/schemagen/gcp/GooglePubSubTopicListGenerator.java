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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.openrdf.model.URI;

import io.konig.core.KonigException;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeHandler;

public class GooglePubSubTopicListGenerator implements ShapeHandler {
	
	private File file;
	private PrintWriter writer;

	public GooglePubSubTopicListGenerator(File file) {
		this.file = file;
	}

	@Override
	public void visit(Shape shape) {
		
		List<DataSource> list = shape.getShapeDataSource();
		if (list != null) {
			for (DataSource ds : list) {
				if (ds.isA(Konig.GooglePubSubTopic)) {
					URI uri = (URI) ds.getId();
					String topicName = uri.getLocalName();
					writer.println(topicName);
					
				}
			}
		}
	}

	@Override
	public void beginShapeTraversal() {
		
		try {
			file.getParentFile().mkdirs();
			writer = new PrintWriter(new FileWriter(file));
		} catch (IOException e) {
			throw new KonigException(e);
		}
		
	}

	@Override
	public void endShapeTraversal() {
		
		if (writer != null) {
			writer.close();
		}
		
	}

}
