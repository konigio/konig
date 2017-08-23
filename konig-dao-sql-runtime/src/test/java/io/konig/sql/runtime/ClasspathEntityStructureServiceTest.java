package io.konig.sql.runtime;

/*
 * #%L
 * Konig DAO SQL Runtime
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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

public class ClasspathEntityStructureServiceTest {

	private ClasspathEntityStructureService service = ClasspathEntityStructureService.defaultInstance();
	
	@Test
	public void test() throws Exception {
		
		EntityStructure struct = service.structureOfShape("http://example.com/shapes/PersonShape");
		
		assertTrue(struct!=null);
		assertEquals("schema.Person", struct.getName());
		
		List<FieldInfo> fieldList = struct.getFields();
		assertTrue(fieldList!=null);
		assertEquals(3, fieldList.size());
		assertEquals("givenName", fieldList.get(0).getName());
		assertEquals("familyName", fieldList.get(1).getName());
		
		FieldInfo address = fieldList.get(2);
		assertEquals("address", address.getName());
		
		struct = address.getStruct();
		fieldList = struct.getFields();
		assertTrue(fieldList != null);
		assertEquals(1, fieldList.size());
		assertEquals("postalAddress", fieldList.get(0).getName());
		
		struct = service.forMediaType("application/vnd.example.person");
		assertEquals("schema.Person", struct.getName());
		
	}

}
