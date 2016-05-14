package io.konig.core.util;

/*
 * #%L
 * konig-core
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

import io.konig.core.Context;
import io.konig.core.Term;
import io.konig.core.impl.BasicContext;

/**
 * A utility that extends a baseline JSON-LD context by adding new terms.
 * @author Greg McFall
 *
 */
public class ContextExtender {
	
	/**
	 * Extend a baseline JSON-LD context by adding extra terms contained
	 * in another context.  This method preserves the baseline context,
	 * adds the new terms and throws an exception if the new terms are not 
	 * compatible with the baseline.
	 * @param baseline The baseline JSON-LD context that will be preserved.
	 * @param extraTerms A JSON-LD context that contains new terms, and possibly 
	 * duplicates from the baseline.
	 * @return A JSON-LD context that extends the baseline with the supplied exraTerms
	 */
	public Context extend(Context baseline, Context extraTerms) throws TermConflictException {
		Context result = new BasicContext(baseline.getContextIRI());
		
		baseline.compile();
		extraTerms.compile();
		
		for (Term term : baseline.asList()) {
			result.add(term);
		}
		
		for (Term extra : extraTerms.asList()) {
			
			Term required = baseline.getTerm(extra.getKey());
			
			if (accept(required, extra)) {
				result.add(extra);
			} 
		}
		
		
		return result;
	}

	private boolean accept(Term a, Term b)
	throws TermConflictException {
		
		if (a == null) {
			return true;
		}
		
		String aId = a.getExpandedIdValue();
		String bId = b.getExpandedIdValue();
		
		URI aType = a.getExpandedType();
		URI bType = b.getExpandedType();
		
		if (!aId.equals(bId) || !compatible(aType, bType)) {
			throw new TermConflictException(bId, null);
		}
		
		
		return false;
	}
	


	private boolean compatible(URI a, URI b) {
		
		return a==null || b==null || (a!=null && a.equals(b));
	}
	

}
