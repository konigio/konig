package io.konig.transform;

/*
 * #%L
 * Konig Transform
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


import java.io.File;
import java.io.IOException;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.rio.RDFHandlerException;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.SH;
import io.konig.shacl.Shape;
import io.konig.transform.rule.DataChannel;
import io.konig.transform.rule.DataChannelRule;
import io.konig.transform.rule.FromItemIterator;
import io.konig.transform.rule.ShapeRule;
import io.konig.transform.rule.TransformPostProcessor;

public class TransformProcessor implements TransformPostProcessor  {
	
	File outDir;
public TransformProcessor(File outDir) {
	this.outDir=outDir;
}
	@Override
	public void process(ShapeRule shapeRule) throws ShapeTransformException {
		Shape targetShape = shapeRule.getTargetShape();
	
		  Graph graph = new MemoryGraph();
		  URI targetShapeId = (URI)targetShape.getId();
		  NamespaceManager nsManager=new MemoryNamespaceManager();
		  nsManager.add("owl", OWL.NAMESPACE);
		  nsManager.add("sh", SH.NAMESPACE);
		  nsManager.add("konig", Konig.NAMESPACE);
		  graph.setNamespaceManager(nsManager);
		  
		  
		  if (!shapeRule.getChannelRules().isEmpty()) {
			  for (DataChannelRule rule : shapeRule.getChannelRules()) {
				  Shape sourceShape = rule.getChannel().getShape();
				  if (sourceShape.getId() instanceof URI) {
					  graph.edge(targetShapeId, Konig.DERIVEDFROM, sourceShape.getId());
				  }
			  }
		  } else if (targetShapeId != null) {
			FromItemIterator sequence = new FromItemIterator(shapeRule.getFromItem());
		    while (sequence.hasNext()) {
		      Shape sourceShape = sequence.next();
		      URI sourceShapeId = (URI)sourceShape.getId();
		      graph.edge(targetShapeId, Konig.DERIVEDFROM, sourceShapeId);
		    }
		    Namespace n = nsManager.findByName(targetShapeId.getNamespace());
		    if(outDir!=null)
		    	outDir=new File(outDir,"shape-dependencies");
		    String fileName = (n!=null?n.getPrefix()+"_"+targetShapeId.getLocalName():"localNamespace"+"_"+targetShapeId.getLocalName());
		    File turtleFile = new File(outDir,fileName+".ttl");
		    
		    try {
		    	if(outDir!=null)
					RdfUtil.prettyPrintTurtle(graph.getNamespaceManager(), graph, turtleFile);
			} catch (RDFHandlerException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		  }
		
	}

}
