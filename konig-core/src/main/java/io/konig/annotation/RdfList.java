package io.konig.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * An annotation which marks a Java class as a container that corresponds to an RDF List.
 * Any Java class with this annotation must have an 'add' method to add the elements of the list.
 * The 'add' method must have a single parameter.
 * @author Greg McFall
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RdfList {

}
