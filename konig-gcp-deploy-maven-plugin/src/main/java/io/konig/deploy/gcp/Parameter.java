package io.konig.deploy.gcp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Parameter {
	
	public static final String UNDEFINED = "[undefined]";

	String property();
	String defaultValue() default UNDEFINED;
}
