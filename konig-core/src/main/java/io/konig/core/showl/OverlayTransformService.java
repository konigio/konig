package io.konig.core.showl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.datasource.DataSource;
import io.konig.shacl.Shape;

public class OverlayTransformService extends BasicTransformService {
	
	private Set<ShowlNodeShape> candidates;
	

	public OverlayTransformService(ShowlSchemaService schemaService, ShowlNodeShapeService nodeService,
			ShowlSourceNodeFactory sourceNodeFactory, Set<ShowlNodeShape> candidates) {
		super(schemaService, nodeService, sourceNodeFactory);
		this.candidates = candidates;
	}

	public Set<ShowlPropertyShapeGroup> computeTransform(ShowlNodeShape targetNode) throws ShowlProcessingException {
		
		ShowlSourceNodeFactory sourceNodeFactory = getSourceNodeFactory();
		
		OverlaySourceNodeFactory overlayFactory = new OverlaySourceNodeFactory(targetNode, sourceNodeFactory);
		setSourceNodeFactory(overlayFactory);
		
		try {
		
			List<ShowlNodeShape> targetNodeList = new ArrayList<>();
			for (ShowlNodeShape sourceNode : candidates) {
				overlayFactory.setSourceNode(sourceNode);
				
				ShowlNodeShape targetNodeClone = clone(targetNode);
				targetNodeList.add(targetNodeClone);
				basicComputeTransform(targetNodeClone);
				
			}
			
			Set<ShowlPropertyShapeGroup> unmapped = new HashSet<>();
			
			setSelectedExpressions(targetNode, targetNodeList, unmapped);
			
			return unmapped;
		} finally {
			setSourceNodeFactory(sourceNodeFactory);
		}
	}

	private void setSelectedExpressions(ShowlNodeShape targetNode, List<ShowlNodeShape> targetNodeList,
			Set<ShowlPropertyShapeGroup> unmapped) {
		
		for (ShowlNodeShape targetClone : targetNodeList) {
			setSelectedExpressions(targetNode, targetClone, unmapped);
		}
		
	}

	private void setSelectedExpressions(ShowlNodeShape targetNode, ShowlNodeShape targetClone,
			Set<ShowlPropertyShapeGroup> unmapped) {
				
		for (ShowlDirectPropertyShape direct : targetNode.getProperties()) {
			URI predicate = direct.getPredicate();
			ShowlDirectPropertyShape cloneDirect = targetClone.getProperty(predicate);
			
			ShowlExpression prior = direct.getSelectedExpression();
			ShowlExpression e = cloneDirect.getSelectedExpression();

			ShowlPropertyShapeGroup group = direct.asGroup();
			if (e == null && prior==null) {
				unmapped.add(group);
			} else 	if (e!=null) {
				unmapped.remove(group);
				if (prior == null) {
					direct.setSelectedExpression(e);
				} else if (prior instanceof ShowlOverlayExpression) {
					ShowlOverlayExpression overlay = (ShowlOverlayExpression) prior;
					overlay.add(e);
				} else {
					ShowlOverlayExpression overlay = new ShowlOverlayExpression();
					overlay.add(prior);
					overlay.add(e);
					direct.setSelectedExpression(overlay);
				}
			}
			
			ShowlNodeShape targetValue = direct.getValueShape();
			if (targetValue != null) {
				ShowlNodeShape cloneValue = cloneDirect.getValueShape();
				setSelectedExpressions(targetValue, cloneValue, unmapped);
			}
			
		}
		
	}

	private ShowlNodeShape clone(ShowlNodeShape targetNode) {
		
		Shape shape = targetNode.getShape();
		DataSource ds = targetNode.getShapeDataSource().getDataSource();
		return getNodeService().createNodeShape(shape, ds);
	}
}
