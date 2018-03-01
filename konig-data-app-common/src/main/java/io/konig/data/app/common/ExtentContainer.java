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
import java.text.MessageFormat;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.dao.core.ConstraintOperator;
import io.konig.dao.core.DaoException;
import io.konig.dao.core.FieldPath;
import io.konig.dao.core.Format;
import io.konig.dao.core.FusionCharts;
import io.konig.dao.core.ShapeQuery;
import io.konig.dao.core.ShapeQuery.Builder;
import io.konig.sql.runtime.ClasspathEntityStructureService;
import io.konig.sql.runtime.EntityStructure;
import io.konig.dao.core.ShapeReadService;

/**
 * A container that holds all instances of a given type.
 * @author Greg McFall
 *
 */
public class ExtentContainer extends AbstractContainer {

	private static final String VIEW = ".view";
	
	private static final String AGGREGATE = ".aggregate";
	
	private static final String XSORT = ".xSort";
	
	private static final String YSORT = ".ySort";
	
	private static final String OFFSET = ".offset";
	
	private static final String LIMIT = ".limit";
	
	public enum AggregateAs { avg, sum, min, max, count };
	
	public enum SortAs { desc, asc };
	
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
		ClasspathEntityStructureService structureService = ClasspathEntityStructureService.defaultInstance();
		if (individualId != null) {
			builder.beginPredicateConstraint().setPropertyName(DataAppConstants.ID)
					.setOperator(ConstraintOperator.EQUAL).setValue(individualId.stringValue())
					.endPredicateConstraint();
		} else if (queryParams != null) {
			for (String keyParam : queryParams.keySet()) {
				String key = escapeUtils(keyParam);
				String value = escapeUtils(queryParams.get(keyParam));
				if(key.endsWith(".minInclusive")){	
					validateQueryParam(key, queryParams.get(key), XMLSchema.DATE);
					builder.beginPredicateConstraint().setPropertyName(key.replace(".minInclusive", ""))
						.setOperator(ConstraintOperator.GREATER_THAN_OR_EQUAL).setValue(value)
						.endPredicateConstraint();
				} else if(key.endsWith(".minExclusive")){
					validateQueryParam(key, queryParams.get(key), XMLSchema.DATE);
					builder.beginPredicateConstraint().setPropertyName(key.replace(".minExclusive", ""))
						.setOperator(ConstraintOperator.GREATER_THAN).setValue(value)
						.endPredicateConstraint();
				} else if(key.endsWith(".maxInclusive")){
					validateQueryParam(key, queryParams.get(key), XMLSchema.DATE);
					builder.beginPredicateConstraint().setPropertyName(key.replace(".maxInclusive", ""))
						.setOperator(ConstraintOperator.LESS_THAN_OR_EQUAL).setValue(value)
						.endPredicateConstraint();					
				} else if(key.endsWith(".maxExclusive")){
					validateQueryParam(key, queryParams.get(key), XMLSchema.DATE);
					builder.beginPredicateConstraint().setPropertyName(key.replace(".maxExclusive", ""))
						.setOperator(ConstraintOperator.LESS_THAN).setValue(value)
						.endPredicateConstraint();					
				} else if(key.endsWith(VIEW)) {
					validateQueryParam(key, queryParams.get(key), XMLSchema.STRING);
				} else if (key.equals("xAxis")  || key.equals("yAxis")){
					if(!(validateValue(queryParams.get(keyParam)))){
						throw new DataAppException("IllegalArgumentException : Invalid  value : "+queryParams.get(keyParam));
					}
				} else if(key.equals(AGGREGATE)){
					validateQueryParam(key, queryParams.get(key), XMLSchema.STRING);
					builder.setAggregate(value);
				} else if (key.equals(LIMIT)) {
					validateQueryParam(key, queryParams.get(key), XMLSchema.LONG);
					builder.setLimit(Long.parseLong(value));
				} else if (key.equals(YSORT)) {
					validateQueryParam(key, queryParams.get(key), XMLSchema.STRING);
					builder.setYSort(value);
				} else if (key.equals(XSORT)) {
					validateQueryParam(key, queryParams.get(key), XMLSchema.STRING);
					builder.setXSort(value);
				} else if (key.equals(OFFSET)) {
					validateQueryParam(key, queryParams.get(key), XMLSchema.LONG);
					builder.setOffset(Long.parseLong(value));
				}  else if (key.equals(".cursor")) {
					builder.setCursor(value==null?"":value);
				} else {
					try {
						EntityStructure struct = structureService.structureOfShape(shapeId.toString());
						FieldPath measure = FieldPath.createFieldPath(key, struct);
						if (measure == null) {
							throw new DaoException("Field not found in Shape: " + shapeId.toString());
						}
					} catch (DaoException ex) {
						throw new DataAppException(ex.getMessage());
					}
					if(validateValue(queryParams.get(keyParam))){
					builder.beginPredicateConstraint().setPropertyName(key)
						.setOperator(ConstraintOperator.EQUAL).setValue(queryParams.get(keyParam))
						.endPredicateConstraint();
					}else{
						throw new DataAppException("IllegalArgumentException : Invalid  value : "+queryParams.get(keyParam));
					}
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
	
	public boolean validateQueryParam(String key, String value, URI datatype ) throws DataAppException {
		String errorMsg = MessageFormat.format("IllegalArgumentException : Invalid format key : {0}  /  value : {1}",
				key, value);
		try {
			if (XMLSchema.DATE.equals(datatype)) {			
				DateTime localTime = new DateTime(value).toDateTime(DateTimeZone.UTC);
				localTime.toString(ISODateTimeFormat.date());			
				return true;
			} else if (XMLSchema.STRING.equals(datatype)) {
				if (VIEW.equals(key) && FusionCharts.MAP_MEDIA_TYPE.equals(value)) {
					return true;
				} else if (VIEW.equals(key) && FusionCharts.MSLINE_MEDIA_TYPE.equals(value)) {
					return true;
				} else if (VIEW.equals(key) && FusionCharts.BAR_MEDIA_TYPE.equals(value)) {
					return true;
				} else if (VIEW.equals(key) && FusionCharts.PIE_MEDIA_TYPE.equals(value)) {
					return true;
				} else if (AGGREGATE.equals(key)) {
					AggregateAs.valueOf(value);
					return true;
				} else if (XSORT.equals(key) || YSORT.equals(key)) {
					SortAs.valueOf(value);
					return true;
				}
	
			} else if (XMLSchema.LONG.equals(datatype)) {
				Long.parseLong(value);
				return true;
			}
			throw new DataAppException(errorMsg);
		} catch (Exception ex) {
			throw new DataAppException(errorMsg);
		}
	}
	
	public String escapeUtils(String value) {
		return StringEscapeUtils.escapeHtml(StringEscapeUtils.escapeSql(StringEscapeUtils.escapeJavaScript(value)));
	}
	
	 boolean validateValue (String value )throws DataAppException{
		 String  errorMsg = MessageFormat.format("IllegalArgumentException : Invalid  value : {0}",
				 value);
		  Pattern special = Pattern.compile ("[!@#$%&*()+=|<>?{}\\[\\]~]");
		  Matcher hasSpecial = special.matcher(value);		
				try{
					if(hasSpecial.find()){
						return false;
					}else if ((value.contains("select")||value.contains("SELECT")) && (value.contains("from")||value.contains("FROM"))){
						return false;
					}else if ((value.contains("insert")||value.contains("INSERT")) && (value.contains("into")||value.contains("INTO"))){
						return false;
					}else if (value.contains("update")||value.contains("UPDATE")){
						return false;
					}else if ((value.contains("delete")||value.contains("DELETE")) && (value.contains("from")||value.contains("FROM"))){
						return false;
					}else if ((value.contains("script")||value.contains("SCRIPT")) && (value.contains("html")||value.contains("HTML"))){
						return false;
					}else if ((value.contains("script")||value.contains("SCRIPT")) && (value.contains("alert")||value.contains("ALERT"))){
						return false;
					}else if ((value.contains("html")||value.contains("HTML")) && (value.contains("alert")||value.contains("ALERT"))){
						return false;
					}else if (value.contains("html")||value.contains("HTML")){
						return false;
					}else if (value.contains("alert")||value.contains("ALERT")){
						return false;
					}else if (value.contains("script")||value.contains("SCRIPT")){
						return false;
					}else{
						return true;
					}
				}catch(Exception ex){
					throw new DataAppException(errorMsg);
				}
				
	 }

}
