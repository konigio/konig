package io.konig.yaml;

/*
 * #%L
 * Konig YAML
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


public enum AnchorFeature {
	/**
	 * Don't write any anchors.
	 * This feature should be used only if you know for sure that objects 
	 * do not appear in more than one place in the graph.
	 */
	NONE,
	
	/**
	 * Write anchors for all objects.
	 * This is the default.
	 */
	ALL,
	
	/**
	 * Write anchors only for objects that appear in more than one place
	 * in the graph.
	 */
	SOME
}
