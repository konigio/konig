package io.konig.transform.assembly;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.util.TurtleElements;
import io.konig.core.util.ValueFormat;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.rule.PropertyRule;
import io.konig.transform.rule.RankedVariable;
import io.konig.transform.rule.ShapeRule;
import io.konig.transform.rule.VariableNamer;

public class Blackboard  {
	
	private static final Logger logger = LoggerFactory.getLogger(Blackboard.class);
	
	private Graph graph;
	private OwlReasoner owlReasoner;
	private ValueFormat canonicalShapeIriTemplate;
	private ShapeManager shapeManager;
	private PropertyManager propertyManager;
	private VariableNamer variableNamer;
	private ShapeRankingAgent shapeRankingAgent;
	
	private Blackboard parent;
	private Map<URI,Blackboard> childMap = new HashMap<>();
	private Shape focusShape;
	private PropertyConstraint focusProperty;
	private ShapeRule shapeRule;
	
	
	private Map<TransformEventType, List<TransformEventHandler>> eventHandlerMap = new HashMap<>();
	private Map<Shape,RankedVariable<Shape>> shapeVariableMap = new HashMap<>();
	private Map<URI, List<PropertyRule>> propertyRuleMap = new HashMap<>();
	private TransformEvent event;
	
	public Blackboard getChild(URI predicate) {
		return childMap.get(predicate);
	}


	public Collection<Blackboard> getChildren() {
		return childMap.values();
	}
	
	public Blackboard provideChild(PropertyConstraint p) throws ShapeTransformException {
		Blackboard child = getChild(p.getPredicate());
		if (child == null) {
			child = createChild(p);
		}
		return child;
	}

	public Blackboard createChild(PropertyConstraint p) throws ShapeTransformException {
		Shape valueShape = p.getShape();
		if (valueShape == null) {
			throw new ShapeTransformException("Shape must be defined for predicate: " + TurtleElements.iri(p.getPredicate()));
		}
		Blackboard child = new Blackboard();
		child.setGraph(graph);
		child.setOwlReasoner(owlReasoner);
		child.setCanonicalShapeIriTemplate(canonicalShapeIriTemplate);
		child.setShapeManager(shapeManager);
		child.setVariableNamer(variableNamer);
		child.setShapeRankingAgent(shapeRankingAgent);
		child.setFocusShape(valueShape);
		child.setFocusProperty(p);
		child.parent = this;
		
		childMap.put(p.getPredicate(), child);
		
		return child;
	}
	
	public PropertyConstraint getFocusProperty() {
		return focusProperty;
	}

	public void setFocusProperty(PropertyConstraint focusProperty) {
		this.focusProperty = focusProperty;
	}

	public Blackboard getParent() {
		return parent;
	}

	public ShapeRule getShapeRule() {
		return shapeRule;
	}

	public void setShapeRule(ShapeRule shapeRule) {
		this.shapeRule = shapeRule;
	}

	public ShapeRankingAgent getShapeRankingAgent() throws ShapeTransformException {
		if (shapeRankingAgent == null) {
			throw new ShapeTransformException("shapeRankingAgent must be defined");
		}
		return shapeRankingAgent;
	}

	public void setShapeRankingAgent(ShapeRankingAgent shapeRankingAgent) {
		this.shapeRankingAgent = shapeRankingAgent;
	}

	public Collection<RankedVariable<Shape>> getSourceShapeVariables() {
		return shapeVariableMap.values();
	}
	
	public ValueFormat getCanonicalShapeIriTemplate() {
		return canonicalShapeIriTemplate;
	}
	public void setCanonicalShapeIriTemplate(ValueFormat canonicalShapeIriTemplate) {
		this.canonicalShapeIriTemplate = canonicalShapeIriTemplate;
	}
	public void addEventHandler(TransformEventHandler handler) {
		TransformEventType[] typeList = handler.respondsTo();
		for (TransformEventType eventType : typeList) {
			addEventHandler(eventType, handler);
		}
	}
	public void addEventHandler(TransformEventType eventType, TransformEventHandler handler) {
		List<TransformEventHandler> list = eventHandlerMap.get(eventType);
		if (list == null) {
			list = new ArrayList<>();
			eventHandlerMap.put(eventType, list);
		}
		if (!list.contains(handler)) {
			list.add(handler);
		}
	}
	
	public void removeEventHandler(TransformEvent eventType, TransformEventHandler handler) {
		List<TransformEventHandler> list = eventHandlerMap.get(eventType);
		if (list != null) {
			list.remove(handler);
		}
	}
	
	public void dispatch(TransformEventType type) throws ShapeTransformException {
		dispatch(new BasicTransformEvent(type, this));
	}
	
	public void dispatch(TransformEvent event) throws ShapeTransformException {
		this.event = event;
		TransformEventType type = event.getType();
		List<TransformEventHandler> list = eventHandlerMap.get(type);
		if (logger.isInfoEnabled()) {
			logger.info("Dispatching event: {}", event.toString());
		}
		if (list != null) {
			for (TransformEventHandler handler : list) {
				handler.handle(event);
			}
		}
	}
	
	public List<PropertyRule> propertyRuleList(URI predicate) {
		return propertyRuleMap.get(predicate);
	}
	
	/**
	 * Get the current event being processed, or the last event processed if no handler is active.
	 */
	public TransformEvent getEvent() {
		return event;
	}
	
	public Shape getFocusShape() {
		return focusShape;
	}
	public void setFocusShape(Shape focusShape) {
		this.focusShape = focusShape;
	}
	public ShapeManager getShapeManager() throws ShapeTransformException {
		if (shapeManager == null) {
			shapeManager = new MemoryShapeManager();
			ShapeLoader shapeLoader = new ShapeLoader(shapeManager);
			shapeLoader.load(getGraph());
		}
		return shapeManager;
	}
	public void setShapeManager(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
	}
	
	
	
	
	public Graph getGraph() throws ShapeTransformException {
		if (graph == null) {
			throw new ShapeTransformException("graph must be defined");
		}
		return graph;
	}
	public void setGraph(Graph graph) {
		this.graph = graph;
	}
	
	public OwlReasoner getOwlReasoner() throws ShapeTransformException {
		if (owlReasoner == null) {
			if (graph == null) {
				throw new ShapeTransformException("graph must be defined");
			}
			owlReasoner = new OwlReasoner(graph);
		}
		return owlReasoner;
	}
	public void setOwlReasoner(OwlReasoner owlReasoner) {
		this.owlReasoner = owlReasoner;
	}
	public VariableNamer getVariableNamer() throws ShapeTransformException {
		if (variableNamer==null) {
			throw new ShapeTransformException("variableNamer must be defined");
		}
		return variableNamer;
	}
	public void setVariableNamer(VariableNamer variableNamer) {
		this.variableNamer = variableNamer;
	}
	
	public PropertyManager getPropertyManager() throws ShapeTransformException {
		if (propertyManager == null) {
			propertyManager = new PropertyManager(getShapeManager());
		}
		return propertyManager;
	}
	public void setPropertyManager(PropertyManager propertyManager) {
		this.propertyManager = propertyManager;
	}
	
	public RankedVariable<Shape> shapeVariable(Shape shape) throws ShapeTransformException {
		RankedVariable<Shape> var = shapeVariableMap.get(shape);
		if (var == null) {
			String name = getVariableNamer().next();
			var = new RankedVariable<Shape>(name, shape);
			shapeVariableMap.put(shape, var);
		}
		return var;
	}
	
	public void addPropertyRule(PropertyRule rule) {
		List<PropertyRule> list = propertyRuleMap.get(rule.getFocusPredicate());
		if (list == null) {
			list = new ArrayList<>();
			propertyRuleMap.put(rule.getFocusPredicate(), list);
		}
		list.add(rule);
	}

	public PropertyRule removePropertyRule(RankedVariable<Shape> sourceShapeVar, URI predicate) {
		List<PropertyRule> list = propertyRuleList(predicate);
		if (list != null) {
			Iterator<PropertyRule> sequence = list.iterator();
			while (sequence.hasNext()) {
				PropertyRule propertyRule = sequence.next();
				RankedVariable<Shape> shapeVar = propertyRule.getSourceShapeVariable();
				if (shapeVar == sourceShapeVar) {
					sequence.remove();
					return propertyRule;
				}
			}
		}
		return null;
	}
	
	public int countDistinctProperties(ShapeRule rule) {
		Set<ShapeRule> memory = new HashSet<>();
		return countDistinctProperties(memory, rule);
	}

	private int countDistinctProperties(Set<ShapeRule> memory, ShapeRule rule) {
		int count = 0;
		if (!memory.contains(rule)) {
			memory.add(rule);
			List<PropertyRule> list = rule.getPropertyRules();
			count+= list.size();
			for (PropertyRule p : list) {
				ShapeRule nestedRule = p.getNestedRule();
				if (nestedRule != null) {
					count += countDistinctProperties(memory, nestedRule);
				}
			}
		}
		return count;
	}
	
}
