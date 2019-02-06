package io.konig.transform.showl.sql;

import io.konig.core.showl.MappingStrategy;

/*
 * #%L
 * Konig Transform
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


import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlNodeShapeConsumer;
import io.konig.core.showl.ShowlProcessingException;
import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;
import io.konig.sql.query.InsertStatement;

public class ShowlSqlNodeConsumer implements ShowlNodeShapeConsumer {
	
	private ShowlSqlTransformConsumer consumer;
	private ShowlSqlTransform transform;
	private MappingStrategy mappingStrategy;

	public ShowlSqlNodeConsumer(ShowlSqlTransformConsumer consumer, MappingStrategy mappingStrategy, ShowlSqlTransform transform) {
		this.consumer = consumer;
		this.mappingStrategy = mappingStrategy;
		this.transform = transform;
	}

	@Override
	public void consume(ShowlNodeShape nodeShape) throws ShowlProcessingException {
		
		if (!nodeShape.isUnmapped()) {
			
			for (DataSource ds : nodeShape.getShape().getShapeDataSource()) {
				if (TableDataSource.class.isAssignableFrom(ds.getClass())) {
					@SuppressWarnings("unchecked")
					Class<? extends TableDataSource> dsType = (Class<? extends TableDataSource>) ds.getClass();
					try {
						mappingStrategy.selectMappings(nodeShape);
						InsertStatement insert = transform.createInsert(nodeShape, dsType);
						consumer.consume(insert, nodeShape, ds);
						
					} catch (ShowlSqlTransformException e) {
						throw new ShowlProcessingException(e);
					}
				}
			}
		}
		

	}

}
