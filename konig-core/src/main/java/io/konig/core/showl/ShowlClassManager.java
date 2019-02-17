package io.konig.core.showl;

import java.util.Collection;

import org.openrdf.model.URI;

import io.konig.core.OwlReasoner;

public interface ShowlClassManager {

	OwlReasoner getReasoner();
	Collection<ShowlClass> listClasses();
	ShowlClass findClassById(URI classId);
}
