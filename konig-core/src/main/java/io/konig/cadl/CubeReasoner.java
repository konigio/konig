package io.konig.cadl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.impl.RdfUtil;
import io.konig.core.showl.ShowlClass;
import io.konig.core.showl.ShowlClassManager;

/**
 * A reasoner that sets properties of the cube based on heuristics.
 * The reasoner performs two functions:
 * <ol>
 *   <li> Set formulas for dimensions, levels and attributes.
 *   <li> Define the <code>rollUpTo</code> relationships between levels
 * </ol>
 * 
 * The reasoner uses the heuristic rules described below.
 * In these descriptions, <code>?source</code> denotes the variable used
 * for a <code>cadl:source</code> of the Cube.
 * 
 * <ul>
 *   <li> 
 *   	If the local name <code>source</code> is case-insenstive-equal to the local name of 
 *      a unique owl:Class, then that owl:Class is set as the `cadl:valueType`
 *      of the source.
 *   </li>
 *   <li>
 *      If the local name of a Dimension ends with the suffix "Dim" (e.g. "accountDim"), then let
 *      let dimName be the part of the name preceding the suffix (e.g. "account").  Otherwise, let
 *      dimName be the entire local name of the Dimension. If dimName uniquely matches the local name of 
 *      a property of ?source, then the formula for the dimension has the form: ?source.{dimName}
 *   </li>
 *   <li>
 *       If the local name of a Level is equal to the <code>dimName</code> value for the declaring
 *       Dimension, then the formula for the Level is identical to the formula for the Dimension.
 *   </li>
 *   <li>
 *       Let levelB be some Level.  If the local name of levelB uniquely matches the local name of 
 *       a property of a preceding Level (call it levelA), then
 *       <ol>
 *       	<li>
 *       		The formula of the levelB is given by {formula-of-levelA}.{local-name-of-levelB}
 *       	</li>
 *          <li> The relationship, levelA cadl:rolesUpTo levelB is asserted.
 *       </ol>
 *   </li>
 *   <li>
 *   	Let attrName be the local name of an Attribute.  If attrName uniquely matches the local name
 *      of a property of the declaring Level, then the formula for the Attribute shall be given by
 *      {levelFormula}.{attrName}
 *   </li>
 *   <li>
 *      Let dim be a Dimension whose rdf:type is xsd:dateTime.  Then the following rules apply.
 *      <ol>
 *        <li> 
 *            If the name of a Level is case-insensitive-equal to a time unit (such as "DAY"),
 *            then the formula for the Level is given by: DATE_TRUNC({levelName}, {formula-of-dim})
 *        </li>
 *        <li> 
 *            The levels of dim shall be sorted by granularity and roll-up relationships established
 *            based on that order.
 *        
 *      </ol>
 *   </li>
 * </ul>
 * @author Greg McFall
 *
 */
public class CubeReasoner {
	private static Logger logger = LoggerFactory.getLogger(CubeReasoner.class);
	private ShowlClassManager classManager;
	private Map<String, Set<ShowlClass>> classesByLocalName;
	
	
	
	public CubeReasoner(ShowlClassManager classManager) {
		this.classManager = classManager;
	}

	public void visit(Cube cube) {
		buildMap();
		setSourceType(cube);
	}

	private void setSourceType(Cube cube) {
		
		if (cube.getSource().getValueType() == null) {
			Variable source = cube.getSource();
			URI sourceId = source.getId();
			if (sourceId != null) {
				String key = sourceId.getLocalName().toLowerCase();
				Set<ShowlClass> set = classesByLocalName.get(key);
				if (set != null && set.size()==1) {
					URI classId = set.iterator().next().getId();
					source.setValueType(classId);
					if (logger.isTraceEnabled()) {
						logger.trace(
							"setSourceType - Set {} as Value Type of {}", 
							curie(classId), sourceId);
					}
				}
			}
		}
		
	}

	private Object curie(URI id) {
		return RdfUtil.optionalCurie(classManager.getReasoner().getGraph().getNamespaceManager(), id);
	}

	private void buildMap() {
		if (classesByLocalName == null) {
			classesByLocalName = new HashMap<>();
			for (ShowlClass owlClass : classManager.listClasses()) {
				URI classId = owlClass.getId();
				String key = classId.getLocalName().toLowerCase();
				
				Set<ShowlClass> set = classesByLocalName.get(key);
				if (set == null) {
					set = new HashSet<>();
					classesByLocalName.put(key, set);
				}
				set.add(owlClass);
			}
		}
		
	}

}
