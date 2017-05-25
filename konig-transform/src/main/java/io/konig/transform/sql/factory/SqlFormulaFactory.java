package io.konig.transform.sql.factory;

import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;

import io.konig.core.KonigException;
import io.konig.core.pojo.BeanUtil;
import io.konig.formula.Addend;
import io.konig.formula.AdditiveOperator;
import io.konig.formula.BinaryOperator;
import io.konig.formula.BinaryRelationalExpression;
import io.konig.formula.ConditionalAndExpression;
import io.konig.formula.Direction;
import io.konig.formula.Expression;
import io.konig.formula.GeneralAdditiveExpression;
import io.konig.formula.IfFunction;
import io.konig.formula.LiteralFormula;
import io.konig.formula.MultiplicativeExpression;
import io.konig.formula.NumericExpression;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;
import io.konig.formula.PathTerm;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.UnaryExpression;
import io.konig.formula.ValueLogical;
import io.konig.shacl.PropertyConstraint;
import io.konig.sql.query.AdditiveValueExpression;
import io.konig.sql.query.BooleanTerm;
import io.konig.sql.query.ComparisonOperator;
import io.konig.sql.query.ComparisonPredicate;
import io.konig.sql.query.IfExpression;
import io.konig.sql.query.NumericValueExpression;
import io.konig.sql.query.QueryExpression;
import io.konig.sql.query.SignedNumericLiteral;
import io.konig.sql.query.StringLiteralExpression;
import io.konig.sql.query.TableItemExpression;
import io.konig.sql.query.ValueExpression;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.factory.TransformBuildException;

public class SqlFormulaFactory {
	

	public ValueExpression formula(TableItemExpression sourceTable, PropertyConstraint p) throws TransformBuildException {
		Worker worker = new Worker(sourceTable);
		
		return worker.formula(p);
	}
	
	private static class Worker {

		private TableItemExpression sourceTable;
		
		public Worker(TableItemExpression sourceTable) {
			this.sourceTable = sourceTable;
		}

		public ValueExpression formula(PropertyConstraint p) throws TransformBuildException {
			
			
			Expression source = p.getFormula();
			if (source == null) {
				throw new TransformBuildException("The 'formula' must be defined for property " + p.getPredicate().stringValue());
			}

			
			try {
				return valueExpression(source);
			} catch (Throwable e) {
				throw new TransformBuildException("Failed to generate formula for property " +
						p.getPredicate().stringValue(), e
				);
			}
		}

		private ValueExpression valueExpression(Expression source) throws ShapeTransformException {
			
			List<ConditionalAndExpression> orList = source.getOrList();
			
			if (orList==null || orList.isEmpty()) {
				throw new KonigException("Invalid expression");
			}
			
			if (orList.size() != 1) {
				throw new KonigException("Unsupported expression");
			}
			ConditionalAndExpression and = orList.get(0);
			
			return andExpression(and);
		}

		private ValueExpression andExpression(ConditionalAndExpression and) throws ShapeTransformException {
			List<ValueLogical> valueLogicalList = and.getAndList();
			if (valueLogicalList.size() == 1) {
				ValueLogical valueLogical = valueLogicalList.get(0);
				return valueLogical(valueLogical);
			} 
			throw new KonigException("Unsupported expression");
			
		}

		private ValueExpression valueLogical(ValueLogical valueLogical) throws ShapeTransformException {

			if (valueLogical instanceof BinaryRelationalExpression) {
				BinaryRelationalExpression binary = (BinaryRelationalExpression) valueLogical;
				return binaryRelational(binary);
			} 
			
			throw new KonigException("Unsupported expression");
			
		}

		private ValueExpression binaryRelational(BinaryRelationalExpression binary) throws ShapeTransformException {
			ValueExpression left = valueExpression(binary.getLeft());
			
			if (binary.getRight()==null) {
				return left;
			} else {
				ValueExpression right = valueExpression(binary.getRight());
				ComparisonOperator op = comparisonOperator(binary.getOperator());
				return new ComparisonPredicate(op, left, right);
			}
			
		}

		private ValueExpression valueExpression(NumericExpression e) throws ShapeTransformException {

			if (e instanceof GeneralAdditiveExpression) {
				GeneralAdditiveExpression additive = (GeneralAdditiveExpression) e;
				return additiveExpression(additive);
			} else {
				throw new KonigException("Unsupported expression type: " + e.getClass().getSimpleName());
			}
		}

		private ComparisonOperator comparisonOperator(BinaryOperator operator) {
			switch (operator) {
			case EQUALS: return ComparisonOperator.EQUALS;
			case GREATER_THAN: return ComparisonOperator.GREATER_THAN;
			case GREATER_THAN_OR_EQUAL: return ComparisonOperator.GREATER_THAN_OR_EQUALS;
			case LESS_THAN: return ComparisonOperator.LESS_THAN;
			case LESS_THAN_OR_EQUAL: return ComparisonOperator.LESS_THAN_OR_EQUALS;
			case NOT_EQUAL: return ComparisonOperator.NOT_EQUALS;
			}
			throw new KonigException("Unsupported comparison operator: " + operator.getText());
		}

		private ValueExpression additiveExpression(GeneralAdditiveExpression additive) throws ShapeTransformException {
			
			ValueExpression left = multiplicativeExpression(additive.getLeft());
			List<Addend> addendList = additive.getAddendList();
			if (addendList != null) {
				for (Addend a : addendList) {
					AdditiveOperator op = a.getOperator();
					ValueExpression right = multiplicativeExpression(a.getRight());
					left = new AdditiveValueExpression(op, left, right);
				}
			}
			
			return left;
		}

		private ValueExpression multiplicativeExpression(MultiplicativeExpression source) throws ShapeTransformException {
			UnaryExpression unary = source.getLeft();
			if (unary.getOperator() != null) {
				throw new KonigException("Unary operators not supported");
			}
			if (source.getMultiplierList() != null && !source.getMultiplierList().isEmpty()) {
				throw new KonigException("Multipliers not supported");
			}
			PrimaryExpression primary = unary.getPrimary();
			if (primary instanceof LiteralFormula) {
				return literal((LiteralFormula) primary);
			}
			
			if (primary instanceof IfFunction) {
				return ifFunction((IfFunction)primary);
			}
			
			if (primary instanceof PathExpression) {
				QueryExpression e = pathExpression((PathExpression) primary);
				if (e instanceof NumericValueExpression) {
					return (NumericValueExpression) e;
				}
				throw new KonigException("Expected a NumericValueExpression but found " + e.getClass().getSimpleName());
			}
			
			throw new KonigException("Expression type not supported: " + primary.getClass().getSimpleName());
		}

		private QueryExpression pathExpression(PathExpression primary) {
			
			List<PathStep> stepList = primary.getStepList();
			if (stepList!=null && stepList.size()==1) {
				PathStep step = stepList.get(0);
				
				if (step.getDirection() == Direction.OUT) {
					PathTerm term = step.getTerm();
					URI predicate = term.getIri();
					
					return SqlUtil.columnExpression(sourceTable, predicate);
				}
			}
			
			throw new KonigException("Unsupported expression");
		}

		private NumericValueExpression ifFunction(IfFunction primary) throws ShapeTransformException {
			
			BooleanTerm condition = booleanTerm(primary.getCondition());
			ValueExpression whenTrue = valueExpression(primary.getWhenTrue());
			ValueExpression whenFalse = valueExpression(primary.getWhenFalse());
			
			return new IfExpression(condition, whenTrue, whenFalse);
			
		}

		private BooleanTerm booleanTerm(Expression condition) throws ShapeTransformException {
			ValueExpression e = valueExpression(condition);
			if (e instanceof BooleanTerm) {
				return (BooleanTerm) e;
			}
			throw new ShapeTransformException("Unsupported expression type: " + e.getClass().getSimpleName());
		}

		private 
		ValueExpression literal(LiteralFormula primary) {
			Literal literal = primary.getLiteral();
			Object obj = BeanUtil.toJavaObject(literal);
			if (obj instanceof Number) {
				return new SignedNumericLiteral((Number) obj);
			} if (obj instanceof String) {
				return new StringLiteralExpression((String)obj);
			}
			
			throw new KonigException("Unsupported literal: " + literal);
		}
	}

}
