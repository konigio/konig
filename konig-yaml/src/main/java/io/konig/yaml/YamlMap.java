package io.konig.yaml;

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
