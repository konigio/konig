package io.konig.core.showl;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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
import java.util.List;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.OwlReasoner;
import io.konig.core.vocab.Konig;

public class EnumMappingAction {
	private static final Logger logger = LoggerFactory.getLogger(EnumMappingAction.class);
	private ShowlNodeShape enumTargetShape;
	private ShowlFactory factory;
	private OwlReasoner reasoner;

	public EnumMappingAction(ShowlNodeShape enumTargetShape, ShowlFactory factory, OwlReasoner reasoner) {
		this.enumTargetShape = enumTargetShape;
		this.factory = factory;
		this.reasoner = reasoner;
	}

	public ShowlNodeShape getEnumTargetShape() {
		return enumTargetShape;
	}

	public void execute() {
		
		List<ShowlPropertyShape> inverseFunctionals = listInverseFunctionals();
		
		if (inverseFunctionals.isEmpty()) {
			if (logger.isWarnEnabled()) {
				StringBuilder builder = new StringBuilder();
				String comma = "";
				List<ShowlDirectPropertyShape> unmapped = unmappedProperties();
				for (ShowlDirectPropertyShape direct : unmapped) {
					builder.append(comma);
					builder.append(direct.getPredicate().getLocalName());
					comma = ", ";
				}
				logger.warn("In enumeration shape {}, no mapping found for: {} ", 
						enumTargetShape.getPath(), builder.toString());
			}
		} else {
			ShowlNodeShape enumSourceShape = factory.logicalNodeShape(enumTargetShape.getOwlClass().getId());
			

			
			for (ShowlPropertyShape key : inverseFunctionals) {
				
				for (ShowlJoinCondition join : key.listJoinConditions()) {
					
					for (ShowlDirectPropertyShape direct : enumTargetShape.getProperties()) {
						
						if (direct == key) {
							continue;
						}
						
						// For now, we assume that every property of the enumTargetShape is available statically within
						// the background graph.
						
						// We may need to back off that assumption later.
						
						ShowlStaticPropertyShape staticProperty = enumSourceShape.staticProperty(direct.getProperty());
						
						new ShowlMapping(join, staticProperty, direct);
					}
				}
			}
		}
	}

	private List<ShowlPropertyShape> listInverseFunctionals() {
		List<ShowlPropertyShape> list = new ArrayList<>();
		for (ShowlDirectPropertyShape direct : enumTargetShape.getProperties()) {
			URI predicate = direct.getPredicate();
			if (!direct.getMappings().isEmpty()  && reasoner.isInverseFunctionalProperty(predicate)) {
				list.add(direct);
				
			} else {
				ShowlPropertyShape peer = direct.getPeer();
				if (peer!=null && !peer.getMappings().isEmpty() && reasoner.isInverseFunctionalProperty(peer.getPredicate())) {
					list.add(direct);
				}
			}
		}
		return list;
	}

	private List<ShowlDirectPropertyShape> unmappedProperties() {
		List<ShowlDirectPropertyShape> list = new ArrayList<>();
		for (ShowlDirectPropertyShape direct : enumTargetShape.getProperties()) {
			if (direct.getPredicate().equals(Konig.id)) {
				continue;
			}
			if (direct.getMappings().isEmpty()) {
				list.add(direct);
			}
		}
		return list;
	}


}
