package io.konig;

/*
 * #%L
 * Camel GoogleBigquery Component
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;

import org.apache.camel.impl.UriEndpointComponent;

/**
 * Represents the component that manages {@link GoogleBigqueryEndpoint}.
 */
public class GoogleBigqueryComponent extends UriEndpointComponent {
    
    public GoogleBigqueryComponent() {
        super(GoogleBigqueryEndpoint.class);
    }

    public GoogleBigqueryComponent(CamelContext context) {
        super(context, GoogleBigqueryEndpoint.class);
    }

    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        Endpoint endpoint = new GoogleBigqueryEndpoint(uri, this);
        setProperties(endpoint, parameters);
        return endpoint;
    }
}
