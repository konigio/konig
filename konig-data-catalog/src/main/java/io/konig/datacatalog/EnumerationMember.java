package io.konig.datacatalog;

/*
 * #%L
 * Konig Data Catalog
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

import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.vocab.Schema;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDFS;

public class EnumerationMember implements Comparable<EnumerationMember> {

    private Vertex vertex;
    private String href;
    private NamespaceManager nsMgr;

    public EnumerationMember(Vertex vertex, String href) {
        this.vertex = vertex;
        this.href = href;
        this.nsMgr = vertex.getGraph().getNamespaceManager();
    }

    private static String safeStringValue(Value value) {
        return value != null ? value.stringValue() : "";
    }

    public String getId() {
        return vertex.getId().toString();
    }

    public String getHref() {
        return href;
    }

    public String getName() {
        return safeStringValue(vertex.getValue(Schema.name));
    }

    public String getDescription() {
        return safeStringValue(vertex.getValue(RDFS.COMMENT));

    }

    public String getCode() {
        return safeStringValue(vertex.getValue(DCTERMS.IDENTIFIER));
    }

    /**
     *
     * @return String curie or full URI if no namespace defined
     */
    public String getCurie() {
        URI uri = new URIImpl(vertex.getId().toString());
        Namespace ns = nsMgr.findByName(uri.getNamespace());
        String prefix = ns != null ? ns.getPrefix() : null;
        return prefix != null ? prefix + ":" + uri.getLocalName() : uri.toString();
    }

    @Override
    public int compareTo(EnumerationMember o) {
        return 0;
    }
}
