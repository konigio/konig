package io.konig.transform.sql.query;

/*
 * #%L
 * Konig Transform
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


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.Path;
import io.konig.core.path.OutStep;
import io.konig.core.path.Step;
import io.konig.core.pojo.BeanUtil;
import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.sql.query.SqlFunctionExpression;
import io.konig.sql.query.StringLiteralExpression;
import io.konig.sql.query.ValueExpression;
import io.konig.transform.IriTemplateInfo;
import io.konig.transform.MappedProperty;
import io.konig.transform.ShapePath;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.TransformAttribute;
import io.konig.transform.TransformFrame;
import io.konig.transform.sql.factory.SqlUtil;

public class SqlFrameFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(SqlFrameFactory.class);
	
	private SqlFunctionExpression currentDate = null;
	
	private int tableCount = 0;
	private Map<ShapePath,TableName> tableMap = new HashMap<>();
	private List<JoinInfo> joinList = new ArrayList<>();
	
	public SqlFrameFactory() {
		
	}
	
	public SqlFrameFactory(int tableCount) {
		this.tableCount = tableCount;
	}

	public SqlFrame create(TransformFrame frame) throws ShapeTransformException {
		frame.countShapes();
		ShapePath best = frame.bestShape();
		TableName tableName = produceTable(best);
		
		JoinElement joinElement = new JoinElement(best, tableName);
		JoinInfo joinInfo = new JoinInfo(null, joinElement);
		joinList.add(joinInfo);
		
		
		SqlFrame s = produce(frame, best, tableName, null);
		s.setTableList(joinList);
		return s;
	}

	/**
	 * Produce a SqlFrame from a TransformFrame.
	 * @param frame The TransformFrame from which the SqlFrame is to be produced.
	 * @param preferredShape The preferred ShapePath to use when mapping attributes of the TransformFrame.
	 * @param preferredTable The TableName associated with the preferredShape
	 * @param joinProperty The mappedProperty through which the TransformFrame is accessed.
	 * @return The SqlFrame produced from the supplied TransformFrame
	 */	
	private SqlFrame produce(TransformFrame frame, ShapePath preferredShape, TableName preferredTable, MappedProperty joinProperty) throws ShapeTransformException {
		
		SqlFrame result = new SqlFrame(frame);
		Collection<TransformAttribute> list = frame.getAttributes();
		for (TransformAttribute attr : list) {
			
			TransformFrame childFrame = attr.getEmbeddedFrame();
			MappedProperty m = attr.getProperty(preferredShape);
			
			if (childFrame == null) {
				// Datatype property or Object reference.
				
				if (m == null) {
					
					// The preferredShape does not have a Property that matches attr
					
					
					MappedProperty best = attr.bestProperty();
					if (best != null) {
						
						
						TableName nextTable = preferredTable;
						if (!compatible(joinProperty, best)) {
							
							JoinElement left = null;
							JoinElement right = null;

							Shape rightShape = best.getShapePath().getShape();
							
							PropertyConstraint leftConstraint = joinProperty.getProperty();
							if (leftConstraint.getDatatype() != null) {
								Path leftPath = leftConstraint.getEquivalentPath();
								if (leftPath != null) {
									if (leftPath.length()==2) {
										// Special case where the relationship is a unique key reference
										Step end = leftPath.asList().get(1);
										if (end instanceof OutStep) {
											OutStep out = (OutStep) end;
											URI predicate = out.getPredicate();
											PropertyConstraint rightConstraint = rightShape.getPropertyConstraint(predicate);
											if (rightConstraint != null) {
												left = new JoinColumn(joinProperty.getShapePath(), preferredTable, leftConstraint.getPredicate().getLocalName());
												nextTable = produceTable(best.getShapePath());
												right = new JoinColumn(best.getShapePath(), nextTable, predicate.getLocalName());
											} else {
												// Check for a property on rightShape with an equivalentPath that maps to the predicate at the end of leftPath
												for (PropertyConstraint p : rightShape.getProperty()) {
													Path rightPath = p.getEquivalentPath();
													if (rightPath != null && rightPath.length()==1) {
														Step rightEnd = rightPath.asList().get(0);
														if (rightEnd instanceof OutStep) {
															OutStep rightOut = (OutStep) rightEnd;
															if (predicate.equals(rightOut.getPredicate())) {

																left = new JoinColumn(joinProperty.getShapePath(), preferredTable, leftConstraint.getPredicate().getLocalName());
																nextTable = produceTable(best.getShapePath());
																right = new JoinColumn(best.getShapePath(), nextTable, p.getPredicate().getLocalName());
																break;
															}
														}
													}
												}
												
											}
										}
									}
								}
								
							} else if (isIri(joinProperty)){
								if (rightShape.getNodeKind()==NodeKind.IRI) {
									nextTable = produceTable(best.getShapePath());
									left = new JoinColumn(joinProperty.getShapePath(), preferredTable, leftConstraint.getPredicate().getLocalName());
									right = new JoinColumn(best.getShapePath(), nextTable, "id");
								}
								
							}
							if (left==null || right==null) {
								throw new ShapeTransformException("Unsupported join condition: " + attr.getPredicate().stringValue());
							}
							JoinInfo joinInfo = new JoinInfo(left, right);
							joinList.add(joinInfo);
						}
						SqlAttribute a = new SqlAttribute(nextTable, attr, best);
						result.add(a);
						
					} else if (attr.getPredicate().getLocalName().equals("modified")) {
						SqlAttribute a = new SqlAttribute(null, attr, current_date());
						result.add(a);
						
					} else {
						// Theoretically, should never get here.
						throw new ShapeTransformException("Unsupported join condition: " + attr.getPredicate().stringValue());
					}
					
					
				} else {
					Set<Value> hasValue = m.getProperty().getHasValue();
					if (hasValue==null || hasValue.isEmpty()) {
						SqlAttribute a = new SqlAttribute(preferredTable, attr, m);
						result.add(a);
					} else {
						URI predicate = m.getProperty().getPredicate();
						if (hasValue.size()>1) {
							throw new ShapeTransformException("Cannot support multiple values on property: " + predicate);
						}
						Value value = hasValue.iterator().next();
						if (value instanceof Literal) {
							Literal literal = (Literal) value;
							Object javaValue = BeanUtil.toJavaObject(literal);
							ValueExpression valueExpr = SqlUtil.literal(javaValue);
							if (valueExpr == null) {
								throw new ShapeTransformException("On property <" + predicate + ">, literal value not supported: " + value);
							}
							SqlAttribute a = new SqlAttribute(preferredTable, attr, valueExpr);
							result.add(a);
						} else {
							throw new ShapeTransformException("Only literal values are supported on property: " + predicate);
						}
					}
				}
			} else {
				// Nested Structure
				
				if (m == null) {
					// The preferred table does not contain a reference to the nested structure
					// TODO: Implement a join to get the nested structure
					logger.warn("Unsupported join condition for frame " + frame.getTargetShape().getId());
					continue;
				} else {
					
					// The preferred table contains a reference to the nested structure
					
					SqlAttribute a = new SqlAttribute(preferredTable, attr, m);
					SqlFrame s = produce(childFrame, preferredShape, preferredTable, m);
					a.setEmbedded(s);
					result.add(a);
					
				}
				
			}
			
			
			
		}
		
		return result;
	}


	private boolean isIri(MappedProperty joinProperty) {

		PropertyConstraint p = joinProperty.getProperty();
	
		return p!=null && p.getNodeKind()==NodeKind.IRI;
	}

	private TableName produceTable(ShapePath shapePath) throws ShapeTransformException {
		TableName rightTable = tableMap.get(shapePath);
		if (rightTable == null) {
			
			Shape shape = shapePath.getShape();
			List<DataSource> list = shape.getShapeDataSource();
			String fullName = null;
			if (list != null) {
				for (DataSource source : list) {
					if (source instanceof TableDataSource) {
						TableDataSource table = (TableDataSource) source;
						if (fullName == null) {
							fullName = table.getTableIdentifier();
						} else {
							StringBuilder err = new StringBuilder();
							err.append("Table name is ambiguous for shape <");
							err.append(shape.getId().stringValue());
							err.append(">.  Found '");
							err.append(fullName);
							err.append("' and '");
							err.append(table.getTableIdentifier());
							err.append("'");
							throw new ShapeTransformException(err.toString());
						}
					}
				}
			}
			
			if (fullName == null) {
				throw new ShapeTransformException("Table datasource not found for Shape: " + shape.getId());
			}
			
			char c = (char)('a' + tableCount++);
			String alias = new String(new char[]{c});
			rightTable = new TableName(fullName, alias);
			tableMap.put(shapePath, rightTable);
		}
		return rightTable;
	}




	private ValueExpression current_date() {
		if (currentDate == null) {
			currentDate = new SqlFunctionExpression("TIMESTAMP");
			currentDate.addArg(new StringLiteralExpression("{modified}"));
		}
		return currentDate;
	}


	/**
	 * @return true if the PropertyConstraint from 'a' has a value Shape
	 * that contains the predicate from 'b'.
	 */
	private boolean compatible(MappedProperty a, MappedProperty b) {

		if (a != null) {
			PropertyConstraint p = b.getProperty();
			if (p != null) {
				URI predicate = p.getPredicate();
				if (predicate != null) {
					PropertyConstraint q = a.getProperty();
					if (q != null) {
						Shape shape = q.getShape();
						if (shape != null) {
							PropertyConstraint match = shape.getPropertyConstraint(predicate);
							if (match != null) {
								return true;
							}
							
						}
					}
				}
			}
			
		}
		return false;
	}







	private TableName tableName(
		ShapePath leftShapePath,
		MappedProperty leftProperty, 
		TableName leftTable, 
		ShapePath rightShapePath,
		MappedProperty rightProperty
	) throws ShapeTransformException {
		
		Shape shape = rightShapePath.getShape();
		List<DataSource> list = shape.getShapeDataSource();
		String fullName = null;
		if (list != null) {
			for (DataSource source : list) {
				if (source instanceof TableDataSource) {
					TableDataSource table = (TableDataSource) source;
					if (fullName == null) {
						fullName = table.getTableIdentifier();
					} else {
						StringBuilder err = new StringBuilder();
						err.append("Table name is ambiguous for shape <");
						err.append(shape.getId().stringValue());
						err.append(">.  Found '");
						err.append(fullName);
						err.append("' and '");
						err.append(table.getTableIdentifier());
						err.append("'");
						throw new ShapeTransformException(err.toString());
					}
				}
			}
		}
		
		if (fullName == null) {
			throw new ShapeTransformException("Table datasource not found for Shape: " + shape.getId());
		}
		
		
		
		TableName rightTable = tableMap.get(rightShapePath);
		if (rightTable == null) {
			char c = (char)('a' + tableCount++);
			String alias = new String(new char[]{c});
			rightTable = new TableName(fullName, alias);

			JoinElement left = null;
			JoinElement right = null;
			
			if (leftShapePath == null) {
				right = new JoinElement(rightShapePath, rightTable);
				
			} else {
				Shape rightShape = rightShapePath.getShape();
				
				URI mappedPredicate = mappedPredicate(leftProperty);
				PropertyConstraint rightConstraint = mappedPredicate==null ? null : rightShape.getPropertyConstraint(mappedPredicate);
				
				if (rightConstraint != null) {
					left = new JoinColumn(leftShapePath, leftTable, leftProperty.getProperty().getPredicate().getLocalName());
					right = new JoinColumn(rightShapePath, rightTable, mappedPredicate.getLocalName());
				} else {
					PropertyConstraint leftConstraint = leftProperty.getProperty();
					if (leftConstraint.getNodeKind()==NodeKind.IRI && rightShape.getNodeKind()==NodeKind.IRI) {
						left = new JoinColumn(leftShapePath, leftTable, leftConstraint.getPredicate().getLocalName());
						right = new JoinColumn(rightShapePath, rightTable, "id");
						
					} else if (leftConstraint.getNodeKind()==NodeKind.IRI && rightShape.getIriTemplate()!=null) {
						left = new JoinColumn(leftShapePath, leftTable, leftConstraint.getPredicate().getLocalName());
						right = new JoinIriTemplate(rightShapePath, rightTable, new IriTemplateInfo(rightShape.getIriTemplate()));
					} else {

						
						
						Shape leftShape = leftShapePath.getShape();
						Path leftPath = leftConstraint==null ? null : leftConstraint.getEquivalentPath();
						String leftPathString = leftPath==null ? null : leftPath.toString();
						
						Path rightPath = rightConstraint==null ? null : rightConstraint.getEquivalentPath();
						String rightPathString = rightPath==null ? null : rightPath.toString();
						
						StringBuilder msg = new StringBuilder();
						msg.append("Failed to define join condition. \n");
						msg.append("leftShape: ");
						msg.append(leftShape.getId());
						msg.append("\nleftProperty.property.predicate: ");
						msg.append(leftProperty.getProperty().getPredicate().getLocalName());
						msg.append("\nleftProperty.property.equivalentPath: ");
						msg.append(leftPathString==null? "null" : leftPathString);
						msg.append("\nleftProperty.stepIndex: ");
						msg.append(leftProperty.getStepIndex());
						msg.append("\nrightShape: ");
						msg.append(rightShape.getId().stringValue());
						msg.append("\nrightProperty.property.predicate: ");
						msg.append(rightProperty.getProperty().getPredicate().getLocalName());
						msg.append("\nrightProperty.property.equivalentPath: ");
						msg.append(rightPathString==null ? null : rightPathString);
						throw new ShapeTransformException(msg.toString());
					}
				} 
			}
			
			JoinInfo join = new JoinInfo(left, right);
			joinList.add(join);
			tableMap.put(rightShapePath, rightTable);
		}
		
		return rightTable;
	}

	private URI mappedPredicate(MappedProperty mappedProperty) {
		if (mappedProperty != null) {
			PropertyConstraint p = mappedProperty.getProperty();
			if (p != null) {
				Path path = p.getEquivalentPath();
				if (path != null) {
					int last = path.length()-1;
					Step step = path.asList().get(last);
					if (step instanceof OutStep) {
						OutStep out = (OutStep) step;
						return out.getPredicate();
					}
				}
			}
		}
		return null;
	}
}
