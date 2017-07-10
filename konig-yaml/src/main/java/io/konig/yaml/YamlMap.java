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


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a method that consumes values in a map.
 * <p>
 * The map values must be an object with a constructor that takes
 * the key as a String argument.
 * <p>
 * For example, suppose you have a YAML document like this:
 * <pre>
 * contact:
 *   work:
 *     email: alice@work.com
 *     telephone: (555)123-4567
 *   home:
 *     email: alice@home.com
 *     telphone: (555)987-6543
 * </pre>
 * 
 * We could serialize this document into the following Person and ContactPoint
 * classes.
 * <pre>
 * public class Person {
 *   private Map<String,ContactPoint> contactPoints = new HashMap<>();
 *   
 *   @YamlMap
 *   public void addContactPoint(ContactPoint point) {
 *   	contactPoints.put(point.getContactType(), point);
 *   }
 *   
 *   // more accessors
 * }
 * 
 * public class ContactPoint {
 *   private String email;
 *   private String telephone;
 *   private String contactType;
 *   
 *   public ContactPoint(String contactType) {
 *     this.contactType = contactType;
 *   }
 *   
 *   // Getters and setters
 * }
 * </pre>
 * 
 * @author Greg McFall
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface YamlMap {
	String value();
}
