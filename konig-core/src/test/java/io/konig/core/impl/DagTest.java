package io.konig.core.impl;

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


import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.vocabulary.OWL;

import io.konig.core.vocab.Schema;

public class DagTest {

	@Test
	public void test() {
		
		Dag dag = new Dag();
		
		dag.addEdge(Schema.CreativeWork, OWL.THING);
		dag.addEdge(Schema.MediaObject, Schema.CreativeWork);
		dag.addEdge(Schema.VideoObject, Schema.MediaObject);
		dag.addEdge(Schema.AudioObject, Schema.MediaObject);
		dag.addEdge(Schema.ImageObject, Schema.MediaObject);
		dag.addEdge(Schema.Barcode, Schema.ImageObject);
		dag.addEdge(Schema.Organization, OWL.THING);
		dag.addEdge(Schema.LocalBusiness, Schema.Organization);
		dag.addEdge(Schema.Place, OWL.THING);
		dag.addEdge(Schema.LocalBusiness, Schema.Place);
		
		List<Resource> list = dag.sort();
		
		int work = list.indexOf(Schema.CreativeWork);
		int media = list.indexOf(Schema.MediaObject);
		int video = list.indexOf(Schema.VideoObject);
		int audio = list.indexOf(Schema.AudioObject);
		int image = list.indexOf(Schema.ImageObject);
		int barcode = list.indexOf(Schema.Barcode);
		int org = list.indexOf(Schema.Organization);
		int business = list.indexOf(Schema.LocalBusiness);
		int place = list.indexOf(Schema.Place);
		
		assertTrue(media < work);
		assertTrue(video < media);
		assertTrue(audio < media);
		assertTrue(image < media);
		assertTrue(barcode < image);
		assertTrue(business < org);
		assertTrue(business < place);
		
	}

}
