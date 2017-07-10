package io.konig.content.gae;

/*
 * #%L
 * Konig Content System, Google App Engine implementation
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


import static io.konig.content.gae.GaeContentSystemUtil.doGet;
import static io.konig.content.gae.GaeContentSystemUtil.doPost;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GaeAssetServletTest extends DatastoreTest {
	


	@Test
	public void testPostGet() throws Exception {
		
		doPost("/quotes/1.0/shakespeare/hamlet.txt", "To thine own self be true");
		
		String actual = doGet("/quotes/1.0/shakespeare/hamlet.txt");

		assertEquals("To thine own self be true", actual);
	}


	

}
