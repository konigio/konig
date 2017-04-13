package io.konig.shacl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.core.Path;

public class PropertyStructure {
	private URI predicate;
	private Resource domain;
	private Resource datatype;
	private Resource valueClass;
	private Integer maxCount;
	private Set<Resource> domainIncludes;
	private boolean domainLocked;
	private boolean domainIncludesLocked;
	private Set<Shape> usedInShape = new HashSet<>();
	private String description;
	private Path equivalentPath;
	
	public PropertyStructure(URI predicate) {
		this.predicate = predicate;
	}

	public boolean isDomainIncludesLocked() {
		return domainIncludesLocked;
	}

	public void setDomainIncludesLocked(boolean domainIncludesLocked) {
		this.domainIncludesLocked = domainIncludesLocked;
	}

	public Set<Resource> getDomainIncludes() {
		return domainIncludes;
	}
	
	public void addShape(Shape shape) {
		usedInShape.add(shape);
	}

	public Set<Shape> getUsedInShape() {
		return usedInShape;
	}

	public boolean isDomainLocked() {
		return domainLocked;
	}

	public void setDomainLocked(boolean domainLocked) {
		this.domainLocked = domainLocked;
	}

	public void domainIncludes(Resource owlClass) {
		if (domainIncludes == null) {
			domainIncludes = new HashSet<>();
		}
		domainIncludes.add(owlClass);
	}

	public URI getPredicate() {
		return predicate;
	}

	public Resource getDomain() {
		return domain;
	}

	public void setDomain(Resource domain) {
		this.domain = domain;
	}

	public Resource getDatatype() {
		return datatype;
	}

	public void setDatatype(Resource datatype) {
		this.datatype = datatype;
	}

	public Resource getValueClass() {
		return valueClass;
	}

	public void setValueClass(Resource valueClass) {
		this.valueClass = valueClass;
	}

	public Integer getMaxCount() {
		return maxCount;
	}

	public void setMaxCount(Integer maxCount) {
		this.maxCount = maxCount;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String description() {
		if (description != null) {
			return description;
		}
		String result = null;
		for (Shape shape : usedInShape) {
			PropertyConstraint p = shape.getPropertyConstraint(predicate);
			if (p != null) {
				String comment = p.getComment();
				if (result == null) {
					result = comment;
				} else {
					if (!result.equals(comment)) {
						return null;
					}
				}
			}
		}
		
		return result;
	}

	public PropertyConstraint asPropertyConstraint() {
		PropertyConstraint p = new PropertyConstraint(predicate);
		if (datatype instanceof URI) {
			p.setDatatype((URI) datatype);
		}
		p.setValueClass(valueClass);
		p.setMaxCount(maxCount);
		
		return p;
	}
	
	public Path getEquivalentPath() {
		return equivalentPath;
	}

	public void setEquivalentPath(Path equivalentPath) {
		this.equivalentPath = equivalentPath;
	}

	public List<URI> domainIncludes() {
		List<URI> result = new ArrayList<>();
		if (domainIncludes == null) {
			if (domain instanceof URI) {
				result.add((URI) domain);
			}
		} else {
			for (Resource resource : domainIncludes) {
				if (resource instanceof URI) {
					result.add((URI)resource);
				}
			}
		}
		return result;
	}
	
	public List<URI> rangeIncludes() {
		List<URI> result = new ArrayList<>();
		if (datatype instanceof URI) {
			result.add((URI) datatype);
		}
		if (valueClass instanceof URI) {
			result.add((URI) valueClass);
		}
		return result;
	}
	
}