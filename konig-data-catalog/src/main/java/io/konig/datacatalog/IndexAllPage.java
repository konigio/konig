package io.konig.datacatalog;

/*
 * #%L
 * Konig Data Catalog
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


import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.openrdf.model.URI;

public class IndexAllPage {
	private static final String TEMPLATE = "data-catalog/velocity/index-all.vm";

	public void render(PageRequest request, PageResponse response) throws DataCatalogException {
		URI pageId = DataCatalogBuilder.INDEX_ALL_URI;

		request.setPageId(pageId);
		request.setActiveLink(pageId);
		Set<URI> indexSet = request.getIndexSet();
		List<URI> uriList = new ArrayList<>(indexSet);
		Collections.sort(uriList, new Comparator<URI>() {
			@Override
			public int compare(URI a, URI b) {
				String x = a.getLocalName();
				String y = b.getLocalName();
				return x.compareToIgnoreCase(y);
			}
		});
		
		List<SectionLink> contents = new ArrayList<>();
		List<IndexSection> sectionList = new ArrayList<>();
		Context context = request.getContext();
		context.put("Contents", contents);
		context.put("SectionList", sectionList);
		
		IndexSection section = null;
		for (URI uri : uriList) {
			String localName = uri.getLocalName();
			char c = Character.toUpperCase(localName.charAt(0));
			String sectionName = Character.toString(c);
			if (section==null || !sectionName.equals(section.getName())) {
				char k = 'A' - 1;
				if (section != null) {
					k = section.getName().charAt(0);
				}
				for (char i=(char)(k+1); i<c; i++) {
					String name = Character.toString(i);
					contents.add(new SectionLink(name));
				}
				contents.add(new SectionLink(sectionName, "#Section_" + c));
				section = new IndexSection(sectionName);
				sectionList.add(section);
				
			}
			String href = request.relativePath(pageId, uri);
			Link link = new Link(uri.getLocalName(), href);
			section.add(link);
		}
		if (!contents.isEmpty()) {
			SectionLink last = contents.get(contents.size()-1);
			char c = last.getName().charAt(0);
			for (char k=(char)(c+1); k<='Z'; k++) {
				contents.add(new SectionLink(Character.toString(k)));
			}
		}

		VelocityEngine engine = request.getEngine();
		Template template = engine.getTemplate(TEMPLATE);

		PrintWriter out = response.getWriter();
		template.merge(context, out);
		out.flush();
		
	}
	
	public static class SectionLink {
		private String name;
		private String href;
		public SectionLink(String name, String href) {
			this.name = name;
			this.href = href;
		}
		public SectionLink(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		public String getHref() {
			return href;
		}
		
		public String toString() {
			return name;
		}
		
	}
	
	public static class IndexSection {
		String name;
		List<Link> linkList = new ArrayList<>();
		
		public IndexSection(String name) {
			this.name = name;
		}
		
		public void add(Link link) {
			linkList.add(link);
		}

		public String getName() {
			return name;
		}

		public List<Link> getLinkList() {
			return linkList;
		}
		
	}
	
}
