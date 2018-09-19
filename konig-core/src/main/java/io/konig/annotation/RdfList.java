package io.konig.annotation;

/*
 * #%L
 * Konig Core
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


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * An annotation which marks a Java class as a container that corresponds to an RDF List.
 * Any Java class with this annotation must satisfy the following design patterns:
 * <ol> 
 *   <li> Must have an 'add' method to add the elements of the list. This method must accpet a single parameter.
 *   <li> Must implement java.util.list, or have a single method whose name starts with "get" and returns an object whose class extends Collection.
 * </ol>
 * @author Greg McFall
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RdfList {

}
