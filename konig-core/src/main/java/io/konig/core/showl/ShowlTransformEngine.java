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


import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.impl.RdfUtil;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class ShowlTransformEngine {
	private static final Logger logger = LoggerFactory.getLogger(ShowlTransformEngine.class);
	private ShowlTargetNodeShapeFactory targetNodeShapeFactory;
	private ShapeManager shapeManager;
	private ShowlTransformService transformService;
	private ShowlNodeShapeConsumer consumer;

	
	public ShowlTransformEngine(ShowlTargetNodeShapeFactory targetNodeShapeFactory, ShapeManager shapeManager,
			ShowlTransformService transformService, ShowlNodeShapeConsumer consumer) {
		this.targetNodeShapeFactory = targetNodeShapeFactory;
		this.shapeManager = shapeManager;
		this.transformService = transformService;
		this.consumer = consumer;
	}

	/**
	 * Build transforms for all target NodeShapes, and notify the consumer for post processing.
	 */
	public void run() {
		for (Shape shape : shapeManager.listShapes()) {
			List<ShowlNodeShape> targetNodeList = targetNodeShapeFactory.createTargetNodeShapes(shape);
			for (ShowlNodeShape targetNode : targetNodeList) {
				Set<ShowlPropertyShapeGroup> unmapped = transformService.computeTransform(targetNode);
				if (unmapped.isEmpty()) {
					if (consumer != null) {
						consumer.consume(targetNode);
					}
				} else if (logger.isWarnEnabled()){
					StringBuilder builder = new StringBuilder();
					builder.append("run: Failed to compute transform for ");
					builder.append(RdfUtil.localName(shape.getId()));
					builder.append(".  The following properties were not mapped:\n");
					for (ShowlPropertyShapeGroup group : unmapped) {
						builder.append("   ");
						builder.append(group.pathString());
						builder.append("\n");
					}
					logger.warn(builder.toString());
				}
			}
		}
	}

}
