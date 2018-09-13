package io.konig.shacl.io;

/*
 * #%L
 * Konig Core
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.openrdf.rio.RDFHandlerException;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.shacl.Shape;

/**
 * A utility that writes "auxiliary" information about shapes to a single Turtle file.
 * This class was designed to write information about DDL files, but could (in theory) be used
 * for other purposes.
 * 
 * @author Greg McFall
 *
 */
public class ShapeAuxiliaryWriter {
	
	private List<ShapeEmitter> emitterList = null;
	
	private File outFile;
	
	
	public ShapeAuxiliaryWriter(File outFile) {
		this.outFile = outFile;
	}


	public void addShapeEmitter(ShapeEmitter emitter) {
		if (emitterList == null) {
			emitterList = new ArrayList<>();
		}
		emitterList.add(emitter);
	}
	
	public List<ShapeEmitter> getEmitterList() {
		return emitterList == null ? Collections.emptyList() : emitterList;
	}

	public void writeAll(NamespaceManager nsManager, Collection<Shape> shapeList) throws IOException, RDFHandlerException {
		
		if (outFile != null && emitterList!=null && !emitterList.isEmpty()) {
			
			Graph target = new MemoryGraph(nsManager);
			for (Shape shape : shapeList) {
				for (ShapeEmitter emitter : emitterList) {
					emitter.emitShape(shape, target);
				}
			}
			outFile.getParentFile().mkdirs();
			RdfUtil.prettyPrintTurtle(nsManager, target, outFile);
			
		}
	}

}
