package io.konig.core.util;

import static io.konig.core.util.StringUtil.LABEL_TO_SNAKE_CASE;

/*
 * #%L
 * Konig Core
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


import static io.konig.core.util.StringUtil.PascalCase;
import static io.konig.core.util.StringUtil.SNAKE_CASE;
import static io.konig.core.util.StringUtil.camelCase;
import static io.konig.core.util.StringUtil.normalizedLocalName;
import static io.konig.core.util.StringUtil.label;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringUtilTest {
	
	@Test
	public void testLabel() {

		assertEquals("Courseware Fulfillment Profile", label("CoursewareFulfillmentProfile"));
		assertEquals("Foo Bar Wiz", label("FooBar_Wiz") );
		assertEquals("Foo Bar Wiz", label("Foo_Bar_Wiz") );
		assertEquals("Foo Bar Wiz", label("foo_bar_wiz") );
		assertEquals("Is Deleted", label("IsDeleted"));
		assertEquals("DO NOT CALL", label("DO_NOT_CALL"));
		assertEquals("Foo-Bar", label("Foo-Bar"));
	}
	
	@Test
	public void testNormalizedLabel() {

		assertEquals("FooBar_Wiz", normalizedLocalName("FooBar Wiz") );
		assertEquals("FooBar_x28_Wiz_x29", normalizedLocalName("FooBar (Wiz)") );
		assertEquals("Foo_Bar_Wiz", normalizedLocalName("Foo_Bar_Wiz") );
		assertEquals("IsDeleted", normalizedLocalName("IsDeleted"));
		assertEquals("DO_NOT_CALL", normalizedLocalName("DO_NOT_CALL"));
		assertEquals("Foo-Bar", normalizedLocalName("Foo-Bar"));
	}
	
	@Test
	public void testLabelToSnakeCase() {
		assertEquals("FOO_BAR_WIZ", LABEL_TO_SNAKE_CASE("FooBar Wiz") );
		assertEquals("FOO_BAR_x28_WIZ_x29", LABEL_TO_SNAKE_CASE("FooBar (Wiz)") );
		assertEquals("FOO_BAR_WIZ", LABEL_TO_SNAKE_CASE("Foo_Bar_Wiz") );
		assertEquals("IS_DELETED", LABEL_TO_SNAKE_CASE("IsDeleted"));
		assertEquals("DO_NOT_CALL", LABEL_TO_SNAKE_CASE("DO_NOT_CALL"));
	}

	@Test
	public void testCamelCase() {

		assertEquals("fooBar", camelCase("foo_bar"));
		assertEquals("fooBarWiz", camelCase("Foo_Bar_Wiz"));
		assertEquals("fooBar", camelCase("FOO_BAR"));
		assertEquals("foo", camelCase("foo"));
		assertEquals("fooBar", camelCase("FooBar"));
		assertEquals("fooBar", camelCase("fooBar"));
	}
	
	@Test
	public void testPascalCase() {

		assertEquals("FooBar", PascalCase("foo_bar"));
		assertEquals("FooBarWiz", PascalCase("Foo_Bar_Wiz"));
		assertEquals("FooBar", PascalCase("FOO_BAR"));
		assertEquals("Foo", PascalCase("foo"));
		assertEquals("FooBar", PascalCase("FooBar"));
	}
	
	@Test
	public void testSnakeCase() {

		assertEquals("FOO_BAR", SNAKE_CASE("foo_bar"));
		assertEquals("FOO_BAR_WIZ", SNAKE_CASE("Foo_Bar_Wiz"));
		assertEquals("FOO_BAR", SNAKE_CASE("FOO_BAR"));
		assertEquals("FOO_BAR", SNAKE_CASE("fooBar"));
		assertEquals("FOO", SNAKE_CASE("foo"));
		assertEquals("FOO_BAR", SNAKE_CASE("FooBar"));
	}

}
