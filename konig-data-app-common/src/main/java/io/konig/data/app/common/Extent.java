package io.konig.data.app.common;

/*
 * #%L
 * Konig DAO Core
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


import java.io.Writer;

import org.openrdf.model.URI;

import io.konig.dao.core.ConstraintOperator;
import io.konig.dao.core.DaoException;
import io.konig.dao.core.Format;
import io.konig.dao.core.ShapeQuery;
import io.konig.dao.core.ShapeReadService;
import io.konig.shacl.Shape;

public class Extent {
	private static final String ID = "id";

	private String path;
	private URI defaultShapeId;
	private ShapeReadService shapeReadService;
	
	
	public Extent(String path, URI defaultShapeId, ShapeReadService shapeReadService) {
		this.path = path;
		this.defaultShapeId = defaultShapeId;
		this.shapeReadService = shapeReadService;
	}

	public void writeNamedIndividual(URI individualId, Writer out, Format format) throws DataAppException {
		writeNamedIndividual(defaultShapeId, individualId, out, format);
	}

	public void writeNamedIndividual(URI shapeId, URI individualId, Writer out, Format format) throws DataAppException {
		ShapeQuery query = ShapeQuery.newBuilder()
			.setShapeId(shapeId.toString())
			.beginPredicateConstraint()
				.setPropertyName(ID)
				.setOperator(ConstraintOperator.EQUAL)
				.setValue(individualId.stringValue())
			.endPredicateConstraint()
			.build();
		
		try {
			shapeReadService.execute(query, out, format);
		} catch (DaoException e) {
			throw new DataAppException(e);
		}
	}


	public static String getId() {
		return ID;
	}


	public String getPath() {
		return path;
	}


	public URI getDefaultShapeId() {
		return defaultShapeId;
	}


	public ShapeReadService getShapeReadService() {
		return shapeReadService;
	}
}
