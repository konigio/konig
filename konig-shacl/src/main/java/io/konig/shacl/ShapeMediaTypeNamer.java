package io.konig.shacl;

/*
 * #%L
 * konig-shacl
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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


public interface ShapeMediaTypeNamer {
	
	/**
	 * Generate a vendor-specific media-type base name for a given Shape.
	 * For example, given a Shape with URI <code>http://www.konig.io/shapes/v1/schema/Person</code>, this
	 * method might return <code>application/vnd.konig.v1.schema.person</code>.
	 * As a best practice, concrete media types are formed by appending a suffix to the base name.
	 * For instance, you might form a json-ld media type by appending '+jsonld' to the base name.
	 * @param shape The Shape for which a media-type base name will be generated.
	 * @return The base name for a vendor-specific media-type associated with the Shape.
	 */
	public String baseMediaTypeName(Shape shape);

}
