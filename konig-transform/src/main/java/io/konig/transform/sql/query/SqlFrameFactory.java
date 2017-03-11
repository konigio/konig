package io.konig.transform.sql.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.sql.query.FunctionExpression;
import io.konig.sql.query.ValueExpression;
import io.konig.transform.MappedProperty;
import io.konig.transform.ShapePath;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.TransformAttribute;
import io.konig.transform.TransformFrame;

public class SqlFrameFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(SqlFrameFactory.class);
	
	private ValueExpression currentDate = null;
	
	private int tableCount = 0;
	private Map<ShapePath,TableName> tableMap = new HashMap<>();
	private List<JoinInfo> joinList = new ArrayList<>();

	public SqlFrame create(TransformFrame frame) throws ShapeTransformException {
		frame.countShapes();
		ShapePath best = frame.bestShape();
		TableName tableName = tableName(null, null, null, best, null);
		
		
		SqlFrame s = produce(frame, best, tableName, null);
		s.setTableList(joinList);
		return s;
	}


	private SqlFrame produce(TransformFrame frame, ShapePath preferredShape, TableName preferredTable, MappedProperty joinProperty) throws ShapeTransformException {
		
		SqlFrame result = new SqlFrame(frame);
		Collection<TransformAttribute> list = frame.getAttributes();
		for (TransformAttribute attr : list) {
			
			TransformFrame childFrame = attr.getEmbeddedFrame();
			MappedProperty m = attr.getProperty(preferredShape);
			
			if (childFrame == null) {
				// Datatype property or Object reference.
				
				if (m == null) {
					
					MappedProperty best = attr.bestProperty();
					if (best != null) {
						
						
						TableName nextTable = preferredTable;
						if (!compatible(joinProperty, best)) {
							nextTable = tableName(preferredShape, joinProperty, preferredTable, best.getShapePath(), best);
						}
						SqlAttribute a = new SqlAttribute(nextTable, attr, best);
						result.add(a);
						
					} else if (Konig.modified.equals(attr.getPredicate())) {
						SqlAttribute a = new SqlAttribute(null, attr, current_date());
						result.add(a);
						
					} else {
						// Theoretically, should never get here.
						throw new ShapeTransformException("Unsupported join condition: " + attr.getPredicate().stringValue());
					}
					
					
				} else {

					SqlAttribute a = new SqlAttribute(preferredTable, attr, m);
					result.add(a);
				}
			} else {
				// Nested Structure
				
				if (m == null) {
					// The preferred table does not contain a reference to the nested structure
					// TODO: Implement a join to get the nested structure
					logger.warn("Unsupported join condition for frame " + frame.getTargetShape().getId());
					continue;
				} else {
					
					MappedProperty best = attr.bestProperty();
					if (best == null || best.getShapePath().getCount()<=preferredShape.getCount()) {
						SqlAttribute a = new SqlAttribute(preferredTable, attr, m);
						SqlFrame s = produce(childFrame, preferredShape, preferredTable, m);
						a.setEmbedded(s);
						result.add(a);
						
					} else {
						
						TableName nextTable = tableName(preferredShape, m, preferredTable, best.getShapePath(), best);
						SqlAttribute a = new SqlAttribute(nextTable, attr, best);
						SqlFrame s = produce(childFrame, best.getShapePath(), nextTable, m);
						a.setEmbedded(s);
						result.add(a);
						
					}
				}
				
			}
			
			
			
		}
		
		return result;
	}



	private ValueExpression current_date() {
		if (currentDate == null) {
			currentDate = new FunctionExpression("CURRENT_DATE");
		}
		return currentDate;
	}







	private boolean compatible(MappedProperty joinProperty, MappedProperty m) {

		if (joinProperty != null) {
			PropertyConstraint p = m.getProperty();
			if (p != null) {
				URI predicate = p.getPredicate();
				if (predicate != null) {
					PropertyConstraint q = joinProperty.getProperty();
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
		ShapePath leftShape,
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
		
		
		
		TableName tableName = tableMap.get(rightShapePath);
		if (tableName == null) {
			char c = (char)('a' + tableCount++);
			String alias = new String(new char[]{c});
			tableName = new TableName(fullName, alias);
			
			JoinInfo join = new JoinInfo(leftShape, leftTable, leftProperty, tableName, rightShapePath, rightProperty);
			joinList.add(join);
			tableMap.put(rightShapePath, tableName);
		}
		
		return tableName;
	}
}
