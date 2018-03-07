package io.konig.transform.sql.factory;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.aws.datasource.AwsAurora;
import io.konig.aws.datasource.AwsAuroraTableReference;
import io.konig.core.Context;
import io.konig.core.OwlReasoner;
import io.konig.core.Path;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.util.IriTemplate;
import io.konig.core.util.TurtleElements;
import io.konig.core.util.ValueFormat.Element;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;
import io.konig.formula.BinaryOperator;
import io.konig.formula.BinaryRelationalExpression;
import io.konig.formula.Direction;
import io.konig.formula.DirectionStep;
import io.konig.formula.Expression;
import io.konig.formula.HasPathStep;
import io.konig.formula.IriValue;
import io.konig.formula.NumericExpression;
import io.konig.formula.ObjectList;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;
import io.konig.formula.PathTerm;
import io.konig.formula.PredicateObjectList;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.QuantifiedExpression;
import io.konig.formula.VariableTerm;
import io.konig.gcp.datasource.BigQueryTableReference;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.gcp.datasource.GoogleBigQueryView;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.sql.query.AliasExpression;
import io.konig.sql.query.CastSpecification;
import io.konig.sql.query.ColumnExpression;
import io.konig.sql.query.ComparisonOperator;
import io.konig.sql.query.ComparisonPredicate;
import io.konig.sql.query.FromExpression;
import io.konig.sql.query.FunctionExpression;
import io.konig.sql.query.GroupByClause;
import io.konig.sql.query.GroupingElement;
import io.konig.sql.query.InsertStatement;
import io.konig.sql.query.JoinExpression;
import io.konig.sql.query.NullValueExpression;
import io.konig.sql.query.OnExpression;
import io.konig.sql.query.QueryExpression;
import io.konig.sql.query.Result;
import io.konig.sql.query.SearchCondition;
import io.konig.sql.query.SelectExpression;
import io.konig.sql.query.SignedNumericLiteral;
import io.konig.sql.query.SimpleCase;
import io.konig.sql.query.SimpleWhenClause;
import io.konig.sql.query.StringLiteralExpression;
import io.konig.sql.query.StructExpression;
import io.konig.sql.query.TableAliasExpression;
import io.konig.sql.query.TableItemExpression;
import io.konig.sql.query.TableNameExpression;
import io.konig.sql.query.UpdateExpression;
import io.konig.sql.query.UpdateItem;
import io.konig.sql.query.ValueContainer;
import io.konig.sql.query.ValueExpression;
import io.konig.transform.factory.TransformBuildException;
import io.konig.transform.proto.PropertyModel;
import io.konig.transform.proto.ShapeModel;
import io.konig.transform.rule.AnyValuePropertyRule;
import io.konig.transform.rule.BinaryBooleanExpression;
import io.konig.transform.rule.BooleanExpression;
import io.konig.transform.rule.ChannelProperty;
import io.konig.transform.rule.ContainerPropertyRule;
import io.konig.transform.rule.CopyIdRule;
import io.konig.transform.rule.DataChannel;
import io.konig.transform.rule.ExactMatchPropertyRule;
import io.konig.transform.rule.FilteredDataChannel;
import io.konig.transform.rule.FixedValuePropertyRule;
import io.konig.transform.rule.FormulaIdRule;
import io.konig.transform.rule.FormulaPropertyRule;
import io.konig.transform.rule.FromItem;
import io.konig.transform.rule.FunctionGroupingElement;
import io.konig.transform.rule.IdPropertyRule;
import io.konig.transform.rule.IdRule;
import io.konig.transform.rule.IriTemplateIdRule;
import io.konig.transform.rule.JoinRule;
import io.konig.transform.rule.JoinStatement;
import io.konig.transform.rule.MapValueTransform;
import io.konig.transform.rule.NullPropertyRule;
import io.konig.transform.rule.PropertyComparison;
import io.konig.transform.rule.PropertyRule;
import io.konig.transform.rule.RenamePropertyRule;
import io.konig.transform.rule.ShapeRule;
import io.konig.transform.rule.TransformBinaryOperator;
import io.konig.transform.rule.ValueTransform;
import io.konig.transform.sql.query.TableName;

public class SqlFactory {
	private static final Logger logger = LoggerFactory.getLogger(SqlFactory.class);
	private String idColumnName = "id";

	public InsertStatement insertStatement(ShapeRule shapeRule) throws TransformBuildException {
		Worker worker = new Worker();
		return worker.insertStatement(shapeRule);
	}

	public UpdateExpression updateExpression(ShapeRule shapeRule) throws TransformBuildException {
		Worker worker = new Worker();
		return worker.updateExpression(shapeRule);
	}

	public SelectExpression selectExpression(ShapeRule shapeRule) throws TransformBuildException {
		Worker worker = new Worker();
		return worker.selectExpression(shapeRule);
	}

	public class Worker implements VariableTableMap {
		private SqlFormulaFactory formulaFactory = new SqlFormulaFactory();
		private OwlReasoner reasoner = new OwlReasoner(new MemoryGraph());
		private Map<String, TableItemExpression> tableItemMap = new HashMap<>();
		private boolean useAlias;

		private SelectExpression selectExpression(ShapeRule shapeRule) throws TransformBuildException {
			SelectExpression select = new SelectExpression();
			addDataChannels(select, select.getFrom(), shapeRule);
			addColumns(select, shapeRule);
			addWhereClause(select, shapeRule);
			addGroupBy(select, shapeRule);

			return select;
		}

		private void addWhereClause(SelectExpression select, ShapeRule shapeRule) throws TransformBuildException {
			
			for (BinaryRelationalExpression binary : shapeRule.getWhereExpressions()) {
				ComparisonPredicate comparison = comparisonPredicate(binary);
				select.addWhereCondition(comparison);
			}
			
		}

		private ComparisonPredicate comparisonPredicate(BinaryRelationalExpression binary) throws TransformBuildException {
			
			ComparisonOperator operator = operator(binary.getOperator());
			ValueExpression left = valueExpression(binary.getLeft());
			ValueExpression right = valueExpression(binary.getRight());
			return new ComparisonPredicate(operator, left, right);
		}

		private ValueExpression valueExpression(NumericExpression e) throws TransformBuildException {
			
			PropertyConstraint constraint = new PropertyConstraint();
			constraint.setMaxCount(1);
			constraint.setMinCount(1);
			QuantifiedExpression formula = QuantifiedExpression.wrap(e);
			constraint.setFormula(formula);
			
			SqlFormulaExchange request = SqlFormulaExchange.builder()
				.withProperty(constraint)
				.withTableMap(this)
				.build();
			
			return formulaFactory.formula(request);
		}

		private ComparisonOperator operator(BinaryOperator operator) {
			switch (operator) {
			case EQUALS: 	return ComparisonOperator.EQUALS;
			case GREATER_THAN :	return ComparisonOperator.GREATER_THAN;
			case GREATER_THAN_OR_EQUAL : return ComparisonOperator.GREATER_THAN_OR_EQUALS;
			case LESS_THAN : return ComparisonOperator.LESS_THAN;
			case LESS_THAN_OR_EQUAL : return ComparisonOperator.LESS_THAN_OR_EQUALS;
			case NOT_EQUAL : return ComparisonOperator.NOT_EQUALS;
				
			}
			return null;
		}

		public InsertStatement insertStatement(ShapeRule shapeRule) throws TransformBuildException {
			InsertStatement insert = null;
			Object tableRef = tableRef(shapeRule);					
			if (tableRef != null) {
						TableName tableName = tableName(tableRef, null);
						List<ColumnExpression> columnList = columnList(shapeRule);
						sort(columnList);
						SelectExpression select = selectExpression(shapeRule);
						insert = new InsertStatement(tableName.getExpression(), columnList, select);
			}
					
			return insert;
		}

		private void sort(List<ColumnExpression> columnList) {
			Collections.sort(columnList, new Comparator<ColumnExpression>() {

				@Override
				public int compare(ColumnExpression a, ColumnExpression b) {
					
					return a.toString().compareTo(b.toString());
				}
			});
			
		}

		private List<ColumnExpression> columnList(ShapeRule shapeRule) {
			List<ColumnExpression> list = new ArrayList<>();
			IdRule idRule = shapeRule.getIdRule();

			if (idRule != null) {
				list.add(new ColumnExpression(idColumnName));
			}

			for (PropertyRule p : shapeRule.getPropertyRules()) {
				String columnName = p.getPredicate().getLocalName();
				list.add(new ColumnExpression(columnName));
			}
			return list;
		}

		private void addGroupBy(SelectExpression select, ShapeRule shapeRule) throws TransformBuildException {

			GroupByClause clause = null;

			List<GroupingElement> ge = shapeRule.getGroupingElement();
			if (!ge.isEmpty()) {
				clause = new GroupByClause(null);
				for (GroupingElement element : ge) {
					add(clause, element);
				}
			} else {
				for (PropertyRule p : shapeRule.getPropertyRules()) {
					if (p instanceof FormulaPropertyRule) {
						FormulaPropertyRule fpr = (FormulaPropertyRule) p;
						PropertyConstraint pc = fpr.getTargetProperty();
						if (Konig.dimension.equals(pc.getStereotype())) {
							if (clause == null) {
								clause = new GroupByClause(null);
							}
							QueryExpression qe = column(p);
							if (qe instanceof AliasExpression) {
								AliasExpression ae = (AliasExpression) qe;
								qe = ae.getExpression();
							}
							if (qe instanceof GroupingElement) {
								clause.add((GroupingElement) qe);
							} else {
								throw new TransformBuildException("Expected column to be a GroupingElement");
							}
						}
					}
				}
			}
			select.setGroupBy(clause);

		}

		private void add(GroupByClause clause, GroupingElement element) throws TransformBuildException {

			if (element instanceof FunctionGroupingElement) {
				FunctionGroupingElement fge = (FunctionGroupingElement) element;
				element = sqlFunction(fge);
			}
			clause.add(element);
			
		}

		private FunctionExpression sqlFunction(FunctionGroupingElement fge) throws TransformBuildException {
			PropertyConstraint constraint = new PropertyConstraint();
			constraint.setMaxCount(1);
			constraint.setMinCount(1);
			QuantifiedExpression formula = QuantifiedExpression.wrap(fge.getFunction());
			constraint.setFormula(formula);
			
			SqlFormulaExchange request = SqlFormulaExchange.builder()
				.withProperty(constraint)
				.withTableMap(this)
				.build();
			return (FunctionExpression) formulaFactory.formula(request);
		}

		private UpdateExpression updateExpression(ShapeRule shapeRule) throws TransformBuildException {

			UpdateExpression update = null;
			Object tableRef = tableRef(shapeRule);
			if (tableRef != null) {
				useAlias = true;
				update = new UpdateExpression();
				String fullName = fullTableName((BigQueryTableReference)tableRef);
				String targetTableAlias = shapeRule.getVariableNamer().next();
				TableNameExpression tableNameExpression = new TableNameExpression(fullName);
				TableItemExpression tableItem = new TableAliasExpression(tableNameExpression, targetTableAlias);

				update.setTable(tableItem);

				for (PropertyRule p : shapeRule.getPropertyRules()) {
					ColumnExpression left = new ColumnExpression(p.getPredicate().getLocalName());
					ValueExpression right = column(p, true);
					UpdateItem item = new UpdateItem(left, right);
					update.add(item);
				}

				addDataChannelsForUpdate(update.getFrom(), shapeRule);

				ValueExpression idRule = createIdRule(shapeRule);
				ValueExpression idColumn = column(targetTableAlias, idColumnName);

				SearchCondition search = new ComparisonPredicate(ComparisonOperator.EQUALS, idColumn, idRule);
				update.setWhere(search);
			}

			return update;
		}

		private ValueExpression column(String tableName, String columnName) {
			StringBuilder builder = new StringBuilder();
			builder.append(tableName);
			builder.append('.');
			builder.append(columnName);

			return new ColumnExpression(builder.toString());
		}

		private String fullTableName(BigQueryTableReference tableRef) {
			StringBuilder builder = new StringBuilder();
			builder.append(tableRef.getDatasetId());
			builder.append('.');
			builder.append(tableRef.getTableId());
			return builder.toString();
		}

		private Object tableRef(ShapeRule shapeRule) {
			Shape shape = shapeRule.getTargetShape();
			for (DataSource ds : shape.getShapeDataSource()) {
				
				if (ds.isA(Konig.GoogleBigQueryTable)) {
					GoogleBigQueryTable bigQuery = (GoogleBigQueryTable) ds;
					return bigQuery.getTableReference();
				} else if(ds instanceof AwsAurora){
					AwsAurora awsAurora = (AwsAurora) ds;
					return awsAurora.getTableReference();
				}
			}
			return null;
		}

		private TableName tableName(Object tableRef, String alias) {
			if(tableRef instanceof BigQueryTableReference){
				BigQueryTableReference ref=(BigQueryTableReference) tableRef;
				StringBuilder builder = new StringBuilder();
				builder.append(ref.getDatasetId());
				builder.append('.');
				builder.append(ref.getTableId());
				String tableName = builder.toString();
				return new TableName(tableName, alias);
			}
			else if(tableRef instanceof AwsAuroraTableReference){
				return new TableName(((AwsAuroraTableReference)tableRef).getAwsTableName(), alias);
			}
			return null;
		}

		private void addColumns(ValueContainer select, ShapeRule shapeRule) throws TransformBuildException {

			addIdColumn(select, shapeRule);

			List<PropertyRule> list = new ArrayList<>(shapeRule.getPropertyRules());
			Collections.sort(list);

			for (PropertyRule p : list) {
				select.add(column(p));
			}
		}

		private void addIdColumn(ValueContainer select, ShapeRule shapeRule) throws TransformBuildException {
			ValueExpression e = createIdRule(shapeRule);
			if (e != null) {
				select.add(e);
			}
		}

		private ValueExpression createIdRule(ShapeRule shapeRule) throws TransformBuildException {
			ValueExpression result = null;
			if (shapeRule == null) {
				throw new TransformBuildException("shapeRule is null");
			}
			IdRule idRule = shapeRule.getIdRule();
			if (idRule != null) {
				if (idRule instanceof CopyIdRule) {
					CopyIdRule copyRule = (CopyIdRule) idRule;
					DataChannel channel = copyRule.getDataChannel();
					TableItemExpression tableItem = simpleTableItem(channel);
					
					PropertyModel p = shapeRule.getTargetShapeModel().getPropertyByPredicate(Konig.id);
					if (p != null) {
						return SqlUtil.columnExpression(p.getGroup().getSourceProperty(), tableItem);
					}
					
					String columnName = SqlUtil.columnName(tableItem, Konig.id);

					result = new ColumnExpression(columnName);

				} else if (idRule instanceof IriTemplateIdRule) {
					result = createIriTemplateValue(shapeRule, (IriTemplateIdRule) idRule);

				} else if (idRule instanceof FormulaIdRule) {
					result = createFormulaIdValue(shapeRule, (FormulaIdRule) idRule);

				} else {
					throw new TransformBuildException("Unsupported IdRule " + idRule.getClass().getName());
				}
			}
			return result;
		}

		private ValueExpression createFormulaIdValue(ShapeRule shapeRule, FormulaIdRule idRule)
				throws TransformBuildException {
			PropertyConstraint p = new PropertyConstraint(Konig.id);
			p.setFormula(shapeRule.getTargetShape().getIriFormula());
			p.setMaxCount(1);
			p.setValueClass(shapeRule.getTargetShape().getTargetClass());
			TableItemExpression sourceTable=tableItem(idRule);
			SqlFormulaExchange exchange = SqlFormulaExchange.builder().withShape(shapeRule.getTargetShape())
					.withSourcePropertyModel(idRule.getSourcePropertyModel())
					.withSourceTable(sourceTable)
					.withShapeRule(shapeRule).withProperty(p).withTableMap(this).withSqlFactoryWorker(this).build();
			ValueExpression ve = formulaFactory.formula(exchange);

			GroupingElement ge = exchange.getGroupingElement();
			if (ge != null) {
				shapeRule.addGroupingElement(ge);
			}

			return new AliasExpression(ve, idColumnName);
		}

		private TableItemExpression tableItem(FormulaIdRule idRule) {
			PropertyModel p = idRule.getSourcePropertyModel();
			if (p != null) {
				if (p.isTargetProperty()) {
					p = p.getGroup().getSourceProperty();
					if (p == null) {
						return null;
					}
				}
				ShapeModel s = p.getDeclaringShape();
				if (s != null) {
					DataChannel channel = s.getDataChannel();
					if (channel!=null) {
						return tableItemMap.get(channel.getName());
					}
				}
			}
			return null;
		}

		private ValueExpression createIriTemplateValue(ShapeRule shapeRule, IriTemplateIdRule idRule)
				throws TransformBuildException {
			FunctionExpression func = new FunctionExpression("CONCAT");
			Shape shape = idRule.getDeclaringShape();
			IriTemplate template = shape.getIriTemplate();
			Context context = template.getContext();
			TableItemExpression tableItem = simpleTableItem(idRule.getDataChannel());

			for (Element e : template.toList()) {
				String text = e.getText();
				switch (e.getType()) {
				case TEXT:
					func.addArg(new StringLiteralExpression(text));
					break;

				case VARIABLE:
					URI iri = new URIImpl(context.expandIRI(text));
					String localName = iri.getLocalName();
					if (localName.length() == 0) {
						func.addArg(new StringLiteralExpression(iri.stringValue()));
					} else {
						PropertyConstraint p = shape.getPropertyConstraint(iri);
						if (p == null) {
							StringBuilder msg = new StringBuilder();
							msg.append("Shape ");
							msg.append(TurtleElements.resource(shape.getId()));
							msg.append(" has invalid IRI template <");
							msg.append(template.getText());
							msg.append(">. Property not found: ");
							msg.append(text);
							throw new TransformBuildException(msg.toString());
						}
						URI datatype = p.getDatatype();
						if (XMLSchema.STRING.equals(datatype)) {
							func.addArg(SqlUtil.columnExpression(tableItem, iri));
						} else {
							CastSpecification cast = new CastSpecification(SqlUtil.columnExpression(tableItem, iri),
									"STRING");
							func.addArg(cast);
						}
					}
					break;
				}
			}
			
			AliasExpression alias = new AliasExpression(func, "id");

			return alias;
		}

		public ValueExpression column(PropertyRule p) throws TransformBuildException {
			return column(p, false);
		}

		private ValueExpression column(PropertyRule p, boolean suppressRename) throws TransformBuildException {

			DataChannel channel = p.getDataChannel();
			TableItemExpression tableItem = simpleTableItem(channel);
			URI predicate = p.getPredicate();
			if (logger.isDebugEnabled()) {
				logger.debug("column predicate: {}", predicate.getLocalName());
			}
			if (p instanceof ExactMatchPropertyRule) {
				
				return SqlUtil.columnExpression(p, tableItem);
			}

			if (p instanceof RenamePropertyRule) {
				RenamePropertyRule renameRule = (RenamePropertyRule) p;
				ValueTransform vt = renameRule.getValueTransform();
				if (vt instanceof MapValueTransform) {
					ValueExpression caseStatement = mapValues(renameRule, (MapValueTransform) vt);
					if (suppressRename) {
						return caseStatement;
					}
					return new AliasExpression(caseStatement, predicate.getLocalName());
				}
				URI sourcePredicate = renameRule.getSourceProperty().getPredicate();
				int pathIndex = renameRule.getPathIndex();
				Path path = renameRule.getSourceProperty().getEquivalentPath();
				if (pathIndex == path.asList().size() - 1) {
					ValueExpression column = SqlUtil.columnExpression(tableItem, sourcePredicate);
					if (suppressRename) {
						return column;
					}
					return new AliasExpression(column, predicate.getLocalName());
				}
				throw new TransformBuildException("Unable to map Path");
			}
			if (p instanceof ContainerPropertyRule) {
				ContainerPropertyRule containerRule = (ContainerPropertyRule) p;
				
				
				
				StructExpression struct = new StructExpression();
				ShapeRule nested = containerRule.getNestedRule();
				addColumns(struct, nested);

				AliasExpression alias = new AliasExpression(struct, predicate.getLocalName());
				
				if (containerRule.isRepeated()) {
					FunctionExpression func = new FunctionExpression("ARRAY_AGG");
					func.addArg(alias);
					return func;
				}
				
				return alias;
			}

			if (p instanceof FixedValuePropertyRule) {
				FixedValuePropertyRule lpr = (FixedValuePropertyRule) p;
				ValueExpression value = valueExpression(lpr.getValue());

				return new AliasExpression(value, predicate.getLocalName());
			}

			if (p instanceof NullPropertyRule) {
				return new AliasExpression(new NullValueExpression(), predicate.getLocalName());
			}

			if (p instanceof FormulaPropertyRule) {
				FormulaPropertyRule fr = (FormulaPropertyRule) p;

				SqlFormulaExchange exchange = SqlFormulaExchange.builder().withProperty(fr.getSourceProperty())
						.withShapeRule(fr.getContainer())
						.withSourceTable(tableItem).withTableMap(this).build();

				ValueExpression ve = formulaFactory.formula(exchange);

				AliasExpression ae = new AliasExpression(ve, predicate.getLocalName());

				return ae;
			}

			if (p instanceof IdPropertyRule) {

				ColumnExpression idExpression = SqlUtil.columnExpression(tableItem, Konig.id);
				return new AliasExpression(idExpression, predicate.getLocalName());
			}
			
			if (p instanceof AnyValuePropertyRule) {
				AnyValuePropertyRule anyValue = (AnyValuePropertyRule) p;
				
				ValueExpression ve = column(anyValue.getCollection(), suppressRename);
				if (ve instanceof AliasExpression) {
					AliasExpression alias = (AliasExpression) ve;
					return new AliasExpression(new FunctionExpression(FunctionExpression.ANY_VALUE, alias.getExpression()), alias.getAlias());
				}
				return new FunctionExpression(FunctionExpression.ANY_VALUE, ve);
			}

			throw new TransformBuildException("Unsupported PropertyRule: " + p.getClass().getName());
		}

		

		

		private ValueExpression mapValues(RenamePropertyRule p, MapValueTransform vt) throws TransformBuildException {

			DataChannel channel = p.getDataChannel();
			URI predicate = p.getSourceProperty().getPredicate();
			TableItemExpression tableItem = simpleTableItem(channel);

			ValueExpression caseOperand = SqlUtil.columnExpression(tableItem, predicate);
			List<SimpleWhenClause> whenClauseList = new ArrayList<>();

			// For now, we assume that URI values map to localName strings.

			for (Entry<Value, Value> e : vt.getValueMap().entrySet()) {
				Value key = e.getKey();
				Value value = e.getValue();

				ValueExpression whenOperand = valueExpression(key);
				Result result = mappedValue(value);

				SimpleWhenClause when = new SimpleWhenClause(whenOperand, result);
				whenClauseList.add(when);
			}

			Result elseClause = null;

			return new SimpleCase(caseOperand, whenClauseList, elseClause);
		}

		private Result mappedValue(Value value) throws TransformBuildException {
			if (value instanceof URI) {
				URI iri = (URI) value;
				return new StringLiteralExpression(iri.getLocalName());
			}
			if (value instanceof Literal) {
				return valueExpression(value);
			}
			throw new TransformBuildException("Cannot map value " + value.stringValue());
		}

		private ValueExpression valueExpression(Value value) throws TransformBuildException {
			if (value instanceof Literal) {
				Literal literal = (Literal) value;
				URI type = literal.getDatatype();
				if (type != null) {
					if (reasoner.isRealNumber(type)) {
						return new SignedNumericLiteral(new Double(literal.doubleValue()));
					}
					if (reasoner.isIntegerDatatype(type)) {
						return new SignedNumericLiteral(new Long(literal.longValue()));
					}
					return new StringLiteralExpression(literal.stringValue());
				}

			}
			
			if (value instanceof URI) {
				URI uri = (URI) value;
				return new StringLiteralExpression(uri.getLocalName());
			}
			throw new TransformBuildException("Cannot convert to ValueExpression: " + value.stringValue());
		}

		private void addDataChannels(SelectExpression select, FromExpression from, ShapeRule shapeRule) throws TransformBuildException {

			FromItem fromItem = shapeRule.getFromItem();
			if (fromItem == null) {
				throw new TransformBuildException("FromItem not defined for ShapeRule: " + shapeRule);
			}

			useAlias = fromItem instanceof JoinRule;

			TableItemExpression item = tableItemExpression(select, fromItem,shapeRule);
			from.add(item);

		}
		private void addDataChannelsForUpdate(FromExpression from, ShapeRule shapeRule) throws TransformBuildException {

			FromItem fromItem = shapeRule.getFromItem();
			if (fromItem == null) {
				throw new TransformBuildException("FromItem not defined for ShapeRule: " + shapeRule);
			}

			useAlias = true;

			TableItemExpression item = tableItemExpression(null, fromItem,shapeRule);
			from.add(item);

		}

		/**
		 * Get the "simple" TableItem associated with a channel. A simple
		 * TableItem is either a TableNameExpression or a TableAliasExpression.
		 */
		private TableItemExpression simpleTableItem(DataChannel channel) throws TransformBuildException {
			TableItemExpression e = toTableItemExpression(channel);
			if (e instanceof JoinExpression) {
				JoinExpression join = (JoinExpression) e;
				e = join.getRightTable();
			}
			return e;
		}

		private TableItemExpression tableItemExpression(SelectExpression select, FromItem fromItem, ShapeRule shapeRule) throws TransformBuildException {
			if (fromItem == null) {
				return null;
			}
			
			
			
			DataChannel channel = fromItem.primaryChannel();
			

			if (logger.isDebugEnabled()) {
				logger.debug("tableItemExpression(fromItem.primaryChannel.class: {})", channel.getClass().getSimpleName());
			}

			TableItemExpression tableItem = null;
			if (channel instanceof FilteredDataChannel) {
				tableItem = filteredTableItemExpression(select, fromItem, shapeRule, (FilteredDataChannel) channel);
			} else {
				tableItem = standardTableItemExpression(channel, shapeRule);
			}
			
			
			if (useAlias) {
				tableItem = new TableAliasExpression(tableItem, channel.getName());
			}
			tableItemMap.put(channel.getName(), tableItem);
			if (channel.getVariableName()!=null) {
				tableItemMap.put(channel.getVariableName(), tableItem);
			}

			if (fromItem instanceof JoinRule) {
				JoinRule joinRule = (JoinRule) fromItem;
				TableItemExpression left = tableItem;
				TableItemExpression right = tableItemExpression(select, joinRule.getRight(),shapeRule);
				OnExpression condition = onExpression(joinRule.getCondition());
				tableItem = new JoinExpression(left, right, condition);
			}

			return tableItem;
		}

		private TableItemExpression standardTableItemExpression(DataChannel channel, ShapeRule shapeRule) throws TransformBuildException {

			DataSource datasource = channel.getDatasource();
			if (datasource == null) {
				StringBuilder msg = new StringBuilder();
				msg.append("Cannot create table item for ");
				msg.append(TurtleElements.resource(channel.getShape().getId()));
				msg.append(". Datasource is not specified.");
				throw new TransformBuildException(msg.toString());
			}
			if (!(datasource instanceof TableDataSource)) {

				StringBuilder msg = new StringBuilder();
				msg.append("Cannot create table item for ");
				msg.append(TurtleElements.resource(channel.getShape().getId()));
				msg.append(". Specified datasource is not an instance of TableDataSource");
				throw new TransformBuildException(msg.toString());
			}
			TableDataSource tableSource = (TableDataSource) datasource;
			
			String tableName = tableName(tableSource, shapeRule);
			
			return new TableNameExpression(tableName);
		}

		private TableItemExpression filteredTableItemExpression(SelectExpression select, FromItem fromItem, ShapeRule shapeRule,
				FilteredDataChannel channel) throws TransformBuildException {
			
			QuantifiedExpression formula = channel.getFormula();
			PrimaryExpression primary = formula.asPrimaryExpression();
			if (primary instanceof PathExpression) {
				PathExpression path = (PathExpression) primary;
				List<PathStep> stepList = path.getStepList();
				
				PathStep first = stepList.get(0);
				if (first instanceof DirectionStep) {
					DirectionStep dir = (DirectionStep) first;
					if (dir.getDirection() == Direction.OUT) {
						PathTerm term = dir.getTerm();
						if (term instanceof VariableTerm) {
							VariableTerm var = (VariableTerm) term;
							
							String varName = var.getVarName();
							TableItemExpression varTableItem = tableForVariable(varName);
							
							String varTableName = null;
							if (varTableItem instanceof TableAliasExpression) {
								TableAliasExpression aliasExpression = (TableAliasExpression) varTableItem;
								varTableName = aliasExpression.getAlias();
							} else {
								throw new TransformBuildException("Unsupported TableItemExpression type: " + varTableItem.getClass().getName());
							}
							
							StringBuilder builder = new StringBuilder();
							builder.append(varTableName);
							boolean ok = true;
							for (int i=1; ok && i<stepList.size(); i++) {
								PathStep step = stepList.get(i);
								if (step instanceof DirectionStep) {
									dir = (DirectionStep) step;
									if (dir.getDirection() == Direction.OUT) {
										term = dir.getTerm();
										if (term instanceof IriValue) {
											IriValue iriValue = (IriValue) term;
											URI iri = iriValue.getIri();
											builder.append('.');
											builder.append(iri.getLocalName());
											
										} else {
											ok = false;
										}
									} else {
										ok = false;
									}
								} else if (step instanceof HasPathStep) {
									if (i < stepList.size()-1) {
										ok = false;
									}

									addFilteredChannelWhereRule(select, shapeRule, channel, (HasPathStep)step);
								}
							}
							if (ok) {
								String columnName = builder.toString();
								ColumnExpression column = new ColumnExpression(columnName);
								return new FunctionExpression(FunctionExpression.UNNEST, column);
							}
							
						}
					}
				}
			}
			throw new TransformBuildException("Unsupported formula for FilteredDataChannel: " + formula.toSimpleString());
		}

		private void addFilteredChannelWhereRule(SelectExpression select, ShapeRule shapeRule, FilteredDataChannel channel, HasPathStep step) throws TransformBuildException {
			List<PredicateObjectList> pairList = step.getConstraints();
			for (PredicateObjectList poList : pairList) {
			
				URI predicate = poList.getVerb().getIri();
				
				ObjectList objectList = poList.getObjectList();
				List<Expression> expressionList = objectList.getExpressions();
				for (Expression e : expressionList) {
					StringBuilder builder = new StringBuilder();
					builder.append(channel.getName());
					builder.append('.');
					builder.append(predicate.getLocalName());
					
					ValueExpression left = new ColumnExpression(builder.toString());
					ValueExpression right = valueExpression(e);
					ComparisonPredicate compare = new ComparisonPredicate(ComparisonOperator.EQUALS, left, right);
					select.addWhereCondition(compare);
				}
				
			}
			
		}

		private ValueExpression valueExpression(Expression e) throws TransformBuildException {

			PrimaryExpression primary = e.asPrimaryExpression();
			if (primary == null) {
				throw new TransformBuildException("Unsupported expression: " + e.getText());
			}
			
			PropertyConstraint constraint = new PropertyConstraint();
			constraint.setMaxCount(1);
			constraint.setMinCount(1);
			QuantifiedExpression formula = QuantifiedExpression.wrap(primary);
			constraint.setFormula(formula);
			
			SqlFormulaExchange request = SqlFormulaExchange.builder()
				.withProperty(constraint)
				.withTableMap(this)
				.build();
			
			return formulaFactory.formula(request);
		}

		private String tableName(TableDataSource tableSource, ShapeRule shapeRule) {

			String tableName = tableSource.getTableIdentifier();
			tableSource.getTableIdentifier();
			List<DataSource> targetDatasourceList = shapeRule.getTargetShape().getShapeDataSource();
			if(targetDatasourceList != null) {
				for(DataSource targetDatasource : targetDatasourceList) {
					if (targetDatasource instanceof GoogleBigQueryView) {
						StringBuilder tableNameBuilder = new StringBuilder();
						tableNameBuilder.append("{gcpProjectId}.");
						tableNameBuilder.append(tableName);
						tableName = tableNameBuilder.toString();
					}
				}
			}
			return tableName;
		}

		private OnExpression onExpression(BooleanExpression condition) throws TransformBuildException {
			OnExpression result = null;
			if (condition instanceof PropertyComparison) {
				PropertyComparison comparison = (PropertyComparison) condition;
				ValueExpression left = valueExpression(comparison.getLeft());
				ValueExpression right = valueExpression(comparison.getRight());

				ComparisonOperator operator = comparisonOperator(comparison.getOperator());

				result = new OnExpression(new ComparisonPredicate(operator, left, right));
			}
			return result;
		}

		private ValueExpression valueExpression(ChannelProperty p) throws TransformBuildException {
			TableItemExpression tableItem = tableItemMap.get(p.getChannel().getName());
			if (tableItem == null) {
				throw new TransformBuildException("TableItemExpression not found for Channel: " + p.getChannel());
			}
			URI predicate = p.getPredicate();
			return SqlUtil.columnExpression(tableItem, predicate);
		}

		private TableItemExpression toTableItemExpression(DataChannel channel) throws TransformBuildException {
			if (channel == null) {
				return null;
			}
			TableItemExpression tableItem = tableItemMap.get(channel.getName());

			if (tableItem == null) {
				
				DataSource datasource = channel.getDatasource();
				if (datasource == null) {
					StringBuilder msg = new StringBuilder();
					msg.append("Cannot create table item for ");
					msg.append(TurtleElements.resource(channel.getShape().getId()));
					msg.append(". Datasource is not specified.");
					throw new TransformBuildException(msg.toString());
				}
				if (!(datasource instanceof TableDataSource)) {

					StringBuilder msg = new StringBuilder();
					msg.append("Cannot create table item for ");
					msg.append(TurtleElements.resource(channel.getShape().getId()));
					msg.append(". Specified datasource is not an instance of TableDataSource");
					throw new TransformBuildException(msg.toString());
				}
				TableDataSource tableSource = (TableDataSource) datasource;
				String tableName = tableSource.getTableIdentifier();
				tableItem = new TableNameExpression(tableName);
				if (useAlias) {
					tableItem = new TableAliasExpression(tableItem, channel.getName());
				}

				JoinStatement join = channel.getJoinStatement();
				if (join != null) {
					TableItemExpression left = simpleTableItem(join.getLeft());
					TableItemExpression right = tableItem;
					OnExpression joinSpecification = onExpression(left, right, join);
					tableItem = new JoinExpression(left, right, joinSpecification);
				}

				tableItemMap.put(channel.getName(), tableItem);
				String varName = channel.getVariableName();
				if (varName != null) {
					tableItemMap.put(varName, tableItem);
				}
			}

			return tableItem;
		}

		private OnExpression onExpression(TableItemExpression left, TableItemExpression right, JoinStatement join)
				throws TransformBuildException {
			SearchCondition searchCondition = null;
			BooleanExpression condition = join.getCondition();
			if (condition instanceof BinaryBooleanExpression) {
				BinaryBooleanExpression binary = (BinaryBooleanExpression) condition;

				URI leftPredicate = binary.getLeftPredicate();
				URI rightPredicate = binary.getRightPredicate();

				ValueExpression leftValue = SqlUtil.columnExpression(left, leftPredicate);
				ValueExpression rightValue = SqlUtil.columnExpression(right, rightPredicate);
				ComparisonOperator comparisonOperator = comparisonOperator(binary.getOperator());

				searchCondition = new ComparisonPredicate(comparisonOperator, leftValue, rightValue);

			} else {
				throw new TransformBuildException(
						"Unsupported BooleanExpression: " + condition.getClass().getSimpleName());
			}

			return new OnExpression(searchCondition);
		}

		private ComparisonOperator comparisonOperator(TransformBinaryOperator operator) throws TransformBuildException {
			switch (operator) {
			case EQUAL:
				return ComparisonOperator.EQUALS;

			case NOT_EQUAL:
				return ComparisonOperator.NOT_EQUALS;
			}

			throw new TransformBuildException("Unsupported binary operator: " + operator);
		}

		@Override
		public TableItemExpression tableForVariable(String varName) {
			return tableItemMap.get(varName);
		}

	}

}
