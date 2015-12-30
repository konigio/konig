/*
 * #%L
 * konig-web
 * %%
 * Copyright (C) 2015 Gregory McFall
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
$(function() {
	
var rdf = konig.rdf;
var owl = konig.owl;
var RdfResource = rdf.RdfResource;

/*****************************************************************************/
function OwlReasoner(data, schema) {
	this.data = data;
	this.schema = schema;
	this.applyInverse = true;
}	

/**
 * Select statements that match the specified pattern, applying semantic reasoning.
 * @param subject The subject that should be matched, or null if all subjects match.
 * @param predicate The predicate to be matched.
 * @param object The object to be matched, or null if all objects match.
 * @return An array of statements that match the pattern
 */
OwlReasoner.prototype.select = function(subject, predicate, object) {

	var result = null;
	if (subject) {

		var vertex = this.data.vertex(subject);
		result = vertex.select(subject, predicate, object);
		
		if (this.applyInverse && !object) {
			var memory = {};
			for (var i=0; i<result.length; i++) {
				var s = result[i];
				memory[s.key()] = s;
			}
			

			var inverse = this.inverseOf(predicate);
			
			var sNode = rdf.node(subject);
			var pNode = rdf.node(predicate);
		
			for (var i=0; i<inverse.length; i++) {
				var inverseProperty = inverse[i];
				var more = vertex.select(null, inverseProperty, subject);
				
				for (var i=0; i<more.length; i++) {
					var s = more[i];
					var s2 = new Statement(sNode, pNode, s.subject);
					var key = s2.key();
					if (!memory[key]) {
						memory[key] = s2;
						result.push(s2);
					}
				}
				
			}
		}
	}
	
	return result;
	
}

/**
 * Get the inverse of a predicate
 * @param predicate The predicate whose inverse is to be returned
 * @return {Array} An array of Vertex values for the inverses of the given predicate.  
 *   It is possible that the schema includes synonyms for the inverse, and in this case the 
 *   array may contain more than one value.  If there are no inverses declared in the schema,
 *   the result in an empty array.
 */
OwlReasoner.prototype.inverseOf = function(predicate) {
	// TODO: apply sameAs reasoning for synonyms of the inverses.
	
	var vertex = this.schema.vertex(predicate);
	
	var inward = vertex.v().inward(owl.inverseOf).toList();
	var outward = vertex.v().out(owl.inverseOf).toList();
	var map = {};
	for (var i=0; i<inward.length; i++) {
		var v = inward[i];
		map[v.id.stringValue] = v;
	}
	for (var k=0; k<outward.length; k++) {
		var v = outward[k];
		var key = v.id.stringValue;
		if (!map[key]) {
			map[key] = v;
			inward.push(v);
		}
	}
	return inward;
}

konig.OwlReasoner = OwlReasoner;

});
