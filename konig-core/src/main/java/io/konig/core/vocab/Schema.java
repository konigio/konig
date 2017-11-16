package io.konig.core.vocab;

import java.util.List;

import org.openrdf.model.Resource;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 Gregory McFall
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
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;

public class Schema {

	public static final String NAMESPACE = "http://schema.org/";
	public static final URI NAMESPACE_URI = new URIImpl(NAMESPACE);
	
	
	public static final URI Boolean = new URIImpl("http://schema.org/Boolean");
	public static final URI BuyAction = new URIImpl("http://schema.org/BuyAction");
	public static final URI ContactPoint = new URIImpl("http://schema.org/ContactPoint");
	public static final URI CreativeWork = new URIImpl("http://schema.org/CreativeWork");
	public static final URI DataType = new URIImpl("http://schema.org/DataType");
	public static final URI Date = new URIImpl("http://schema.org/Date");
	public static final URI DateTime = new URIImpl("http://schema.org/DateTime");
	public static final URI Float = new URIImpl("http://schema.org/Float");
	public static final URI Integer = new URIImpl("http://schema.org/Integer");
	public static final URI MediaObject = new URIImpl("http://schema.org/MediaObject");
	public static final URI Enumeration = new URIImpl("http://schema.org/Enumeration");
	public static final URI GenderType = new URIImpl("http://schema.org/GenderType");
	public static final URI Number = new URIImpl("http://schema.org/Number");
	public static final URI Organization = new URIImpl("http://schema.org/Organization");
	public static final URI School = new URIImpl("http://schema.org/School");
	public static final URI Person = new URIImpl("http://schema.org/Person");
	public static final URI Product = new URIImpl("http://schema.org/Product");
	public static final URI PostalAddress = new URIImpl("http://schema.org/PostalAddress");
	public static final URI Text = new URIImpl("http://schema.org/Text");
	public static final URI Thing = new URIImpl("http://schema.org/Thing");
	public static final URI Time = new URIImpl("http://schema.org/Time");
	public static final URI VideoObject = new URIImpl("http://schema.org/VideoObject");
	public static final URI WebPage = new URIImpl("http://schema.org/WebPage");
	public static final URI about = new URIImpl("http://schema.org/about");
	public static final URI address = new URIImpl("http://schema.org/address");
	public static final URI addressLocality = new URIImpl("http://schema.org/addressLocality");
	public static final URI addressRegion = new URIImpl("http://schema.org/addressRegion");
	public static final URI addressCountry = new URIImpl("http://schema.org/addressCountry");
	public static final URI alumniOf = new URIImpl("http://schema.org/alumniOf");
	public static final URI agent = new URIImpl("http://schema.org/agent");
	public static final URI author = new URIImpl("http://schema.org/author");
	public static final URI brand = new URIImpl("http://schema.org/brand");
	public static final URI byArtist = new URIImpl("http://schema.org/byArtist");
	public static final URI children = new URIImpl("http://schema.org/children");
	public static final URI contactPoint = new URIImpl("http://schema.org/contactPoint");
	public static final URI contactType = new URIImpl("http://schema.org/contactType");
	public static final URI dateCreated = new URIImpl("http://schema.org/dateCreated");
	public static final URI datePublished = new URIImpl("http://schema.org/datePublished");
	public static final URI description = new URIImpl("http://schema.org/description");
	public static final URI duns = new URIImpl("http://schema.org/duns");
	public static final URI email = new URIImpl("http://schema.org/email");
	public static final URI exampleOfWork = new URIImpl("http://schema.org/exampleOfWork");
	public static final URI familyName = new URIImpl("http://schema.org/familyName");
	public static final URI givenName = new URIImpl("http://schema.org/givenName");
	public static final URI hasPart = new URIImpl("http://schema.org/hasPart");
	public static final URI knows = new URIImpl("http://schema.org/knows");
	public static final URI lastReviewed = new URIImpl("http://schema.org/lastReviewed");
	public static final URI manufacturer = new URIImpl("http://schema.org/manufacturer");
	public static final URI memberOf = new URIImpl("http://schema.org/memberOf");
	public static final URI name = new URIImpl("http://schema.org/name");
	public static final URI object = new URIImpl("http://schema.org/object");
	public static final URI parent = new URIImpl("http://schema.org/parent");
	public static final URI postalCode = new URIImpl("http://schema.org/postalCode");
	public static final URI streetAddress = new URIImpl("http://schema.org/streetAddress");
	public static final URI thumbnailUrl = new URIImpl("http://schema.org/thumbnailUrl");
	public static final URI timeRequired = new URIImpl("http://schema.org/timeRequired");
	public static final URI rangeIncludes = new URIImpl("http://schema.org/rangeIncludes");
	public static final URI domainIncludes = new URIImpl("http://schema.org/domainIncludes");
	public static final URI Male = new URIImpl("http://schema.org/Male");
	public static final URI Female = new URIImpl("http://schema.org/Female");
	public static final URI gender = new URIImpl("http://schema.org/gender");
	public static final URI telephone = new URIImpl("http://schema.org/telephone");
	public static final URI contactOption = new URIImpl("http://schema.org/contactOption");
	public static final URI HearingImpairedSupported = new URIImpl("http://schema.org/HearingImpairedSupported");
	public static final URI TollFree = new URIImpl("http://schema.org/TollFree");
	public static final URI areaServed = new URIImpl("http://schema.org/areaServed");
	public static final URI TradeAction = new URIImpl("http://schema.org/TradeAction");
	public static final URI price = new URIImpl("http://schema.org/price");
	public static final URI birthDate = new URIImpl("http://schema.org/birthDate");
	public static final URI birthPlace = new URIImpl("http://schema.org/birthPlace");
	public static final URI location = new URIImpl("http://schema.org/location");
	public static final URI worksFor = new URIImpl("http://schema.org/worksFor");
	public static final URI Place = new URIImpl("http://schema.org/Place");
	public static final URI Country = new URIImpl("http://schema.org/Country");
	public static final URI LocalBusiness = new URIImpl("http://schema.org/LocalBusiness");
	public static final URI containedInPlace = new URIImpl("http://schema.org/containedInPlace");
	public static final URI legalName = new URIImpl("http://schema.org/legalName");
	public static final URI sponsor = new URIImpl("http://schema.org/sponsor");
	public static final URI founder = new URIImpl("http://schema.org/founder");
	public static final URI hoursAvailable = new URIImpl("http://schema.org/hoursAvailable");
	public static final URI opens = new URIImpl("http://schema.org/opens");
	public static final URI closes = new URIImpl("http://schema.org/closes");
	public static final URI answerCount = new URIImpl("http://schema.org/answerCount");
	public static final URI AudioObject = new URIImpl("http://schema.org/AudioObject");
	public static final URI ImageObject = new URIImpl("http://schema.org/ImageObject");
	public static final URI Barcode = new URIImpl("http://schema.org/Barcode");
	public static final URI category = new URIImpl("http://schema.org/category");
	public static final URI SoftwareApplication =  new URIImpl("http://schema.org/SoftwareApplication");
	public static final URI identifier = new URIImpl("http://schema.org/identifier");
	public static final URI taxID = new URIImpl("http://schema.org/taxID");
	
	

}