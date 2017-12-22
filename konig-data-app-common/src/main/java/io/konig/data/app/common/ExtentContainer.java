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
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.URI;

import io.konig.dao.core.ConstraintOperator;
import io.konig.dao.core.DaoException;
import io.konig.dao.core.Format;
import io.konig.dao.core.ShapeQuery;
import io.konig.dao.core.ShapeReadService;
import io.konig.dao.core.ShapeQuery.Builder;

/**
 * A container that holds all instances of a given type.
 * @author Greg McFall
 *
 */
public class ExtentContainer extends AbstractContainer {

	private static final String VIEW = ".view";

	private ShapeReadService shapeReadService;
	private URI extentClass;
	
	public ShapeReadService getShapeReadService() {
		return shapeReadService;
	}

	public void setShapeReadService(ShapeReadService shapeReadService) {
		this.shapeReadService = shapeReadService;
	}

	public URI getExtentClass() {
		return extentClass;
	}

	public void setExtentClass(URI extentClass) {
		this.extentClass = extentClass;
	}

	@Override
	public void get(GetRequest request, DataResponse response) throws DataAppException {
		URI shapeId = request.getShapeId();
		URI individualId = request.getIndividualId();
		Writer out = response.getWriter();
		Format format = request.getFormat();
		Map<String, String> queryParams = request.getQueryParams();
		Builder builder = ShapeQuery.newBuilder()
				.setView(request.getQueryParams().get(VIEW))
				.setParameters(request.getQueryParams())
				.setShapeId(shapeId.toString());

		if (individualId != null) {
			builder.beginPredicateConstraint().setPropertyName(DataAppConstants.ID)
					.setOperator(ConstraintOperator.EQUAL).setValue(individualId.stringValue())
					.endPredicateConstraint();
		} else if (queryParams != null) {
			for (String key : queryParams.keySet()) {
				if(key.endsWith(".minInclusive")){					
					builder.beginPredicateConstraint().setPropertyName(key.replace(".minInclusive", ""))
						.setOperator(ConstraintOperator.GREATER_THAN_OR_EQUAL).setValue(queryParams.get(key))
						.endPredicateConstraint();
				} else if(key.endsWith(".minExclusive")){
					builder.beginPredicateConstraint().setPropertyName(key.replace(".minExclusive", ""))
						.setOperator(ConstraintOperator.GREATER_THAN).setValue(queryParams.get(key))
						.endPredicateConstraint();
				} else if(key.endsWith(".maxInclusive")){
					builder.beginPredicateConstraint().setPropertyName(key.replace(".maxInclusive", ""))
						.setOperator(ConstraintOperator.LESS_THAN_OR_EQUAL).setValue(queryParams.get(key))
						.endPredicateConstraint();					
				} else if(key.endsWith(".maxExclusive")){
					builder.beginPredicateConstraint().setPropertyName(key.replace(".maxExclusive", ""))
						.setOperator(ConstraintOperator.LESS_THAN).setValue(queryParams.get(key))
						.endPredicateConstraint();					
				} else if(key.endsWith(".view") || key.equals("xAxis")  || key.equals("yAxis")){
					//TODO: just to skipped the view attribute 
				} else if(key.equals(".aggregate")){
					builder.setAggregate(queryParams.get(".aggregate"));
				} else if (key.equals(".limit")) {
					builder.setLimit(Long.parseLong(queryParams.get(".limit")));
				} else if (key.equals(".ySort")) {
					builder.setYSort(queryParams.get(".ySort"));
				} else if (key.equals(".xSort")) {
					builder.setXSort(queryParams.get(".xSort"));
				} else if (key.equals(".offset")) {
					builder.setOffset(Long.parseLong(queryParams.get(".offset")));
				}  else if (key.equals(".cursor")) {
					builder.setCursor(queryParams.get(".cursor")==null?"":queryParams.get(".cursor"));
				} else {
					builder.beginPredicateConstraint().setPropertyName(key)
						.setOperator(ConstraintOperator.EQUAL).setValue(queryParams.get(key))
						.endPredicateConstraint();
				}				
			}
		}
		ShapeQuery query = builder.build();
			
			try {
				shapeReadService.execute(query, out, format);
			} catch (DaoException e) {
				throw new DataAppException(e);
			}
		
	}

}
