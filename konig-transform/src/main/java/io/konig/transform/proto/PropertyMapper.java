package io.konig.transform.proto;

import io.konig.transform.ShapeTransformException;

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


/**
 * A strategy for mapping source properties to target properties, and as a consequence, selecting the
 * set of source shapes that will participate in a transform.
 * @author Greg McFall
 *
 */
public interface PropertyMapper {

	void mapProperties(ShapeModel targetShape) throws ShapeTransformException;
}
