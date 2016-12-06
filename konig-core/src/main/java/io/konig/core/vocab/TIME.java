package io.konig.core.vocab;

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


import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class TIME {

	public static final String NAMESPACE = "http://www.w3.org/2006/time#";
	
	public static final URI TemporalUnit = new URIImpl("http://www.w3.org/2006/time#TemporalUnit");
	public static final URI unitYear = io.konig.core.vocab.TemporalUnit.YEAR.getURI();
	public static final URI unitMonth = io.konig.core.vocab.TemporalUnit.MONTH.getURI();
	public static final URI unitWeek = io.konig.core.vocab.TemporalUnit.WEEK.getURI();
	public static final URI unitDay = io.konig.core.vocab.TemporalUnit.DAY.getURI();
	public static final URI unitHour = io.konig.core.vocab.TemporalUnit.HOUR.getURI();
	public static final URI unitMinute = io.konig.core.vocab.TemporalUnit.MINUTE.getURI();
	public static final URI unitSecond = io.konig.core.vocab.TemporalUnit.SECOND.getURI();
	

}
