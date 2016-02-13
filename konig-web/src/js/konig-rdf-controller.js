/*
 * #%L
 * konig-web
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
$(window).ready(function() {
	
var OUT = 0;
var IN = 1;

var ChangeSet = rdf.ChangeSet;
var ApplyChangeSet = rdf.ApplyChangeSet;
var IRI = rdf.IRI;
var RDF = rdf.RDF;
var xsd = rdf.xsd;
var KE = konig.ke;

var skos = konig.skos;
var rdfs = konig.rdfs;
var RDFS = rdf.RDFS;
var RdfResource = rdf.RdfResource;
var RdfVertex = rdf.Vertex;
var Literal = rdf.Literal;
var BNode = rdf.BNode;
var OWL = rdf.OWL;

var TOP = konig.geometry.TOP;
var RIGHT = konig.geometry.RIGHT;
var BOTTOM = konig.geometry.BOTTOM;
var LEFT = konig.geometry.LEFT;
var NORMAL = konig.geometry.NORMAL;


var Point = konig.Point;
var Rectangle = konig.Rectangle;
var Box = konig.Box;
var distance = konig.distance;


/**
 * Determine the point on a given ray that is a specified distance
 * from some specified reference point.
 * 
 * @param {Vector} a The reference point.
 * @param {Vector} b The starting point of the ray.
 * @param {Vector} normal The unit vector giving the direction of the ray that starts at b.
 * @param {Number} s The distance from reference point a to the point on the ray that is to be computed.
 * @return The distance q such that b + q*normal is the required point on the ray, or -1 if no such point exists.
 * 
 */
function pointOnRayAtDistanceFromReferencePoint(a, b, normal, s) {
	
	var p = a.clone().minus(b);
	var t = Math.abs(p.dot(normal));
	var p2 = p.lengthSquared();
	
	var s2 = s*s;
	if (s2<p2) {
		return -1;
	}
	return Math.sqrt(s2-p2+t*t) - t;
	
}

function positiveDegrees(radians) {
	if (radians<0) {
		radians = radians + 2*Math.PI;
	}
	var theta = Math.degrees(radians);
	return theta;
}
/**
 * Check whether an angle is within a sector.
 * 
 * @param {number} minAngle The minimum angle of the sector. May be negative.
 * @param {number} maxAngle The maximum angle of the sector. Must be positive and greater than minAngle.
 * @param {number} theta The angle being tested. Must be positive.
 * @return {boolean} True if the angle is within the sector, and false otherwise.
 */
function sectorContainsAngle(minAngle, maxAngle, theta) {
	if (theta<0) {
		theta = theta + 2*Math.PI;
	}
	if (minAngle < 0) {
		
		return (theta >= minAngle + 2*Math.PI) || theta<=maxAngle;
	}
	
	return minAngle <= theta && theta <=maxAngle;
}

function sectorContainsAngleWithTolerance(minAngle, maxAngle, theta, tolerance) {
	return sectorContainsAngle(minAngle, maxAngle, theta) ||
		angularDistance(minAngle, theta) < tolerance ||
		angularDistance(maxAngle, theta) < tolerance;
}

/**
 * Compute the angular distance between two angles.
 * @param {number} a An angle in the range [-2*PI, 2*PI]
 * @param {number} b Another angle in the range [-2*PI, 2*PI]
 * @return {number} The angular distance between a and b.
 */
function angularDistance(a,b) {
	if (a < 0) {
		a += 2*Math.PI;
	}
	if (b < 0) {
		b += 2*Math.PI;
	}
	if (a>b) {
		var c=a;
		a=b;
		b=c;
	}
	var delta = b-a;
	if (delta > Math.PI) {
		delta = 2*Math.PI - delta;
	}
	return delta;
}

/*
 * Line Segment intersection courtesy of http://jsfiddle.net/justin_c_rounds/Gd2S2/
 */
function checkLineIntersection(line1StartX, line1StartY, line1EndX, line1EndY, line2StartX, line2StartY, line2EndX, line2EndY) {
    // if the lines intersect, the result contains the x and y of the intersection (treating the lines as infinite) and booleans for whether line segment 1 or line segment 2 contain the point
    var denominator, a, b, numerator1, numerator2, result = {
        x: null,
        y: null,
        onLine1: false,
        onLine2: false
    };
    denominator = ((line2EndY - line2StartY) * (line1EndX - line1StartX)) - ((line2EndX - line2StartX) * (line1EndY - line1StartY));
    if (denominator == 0) {
        return result;
    }
    a = line1StartY - line2StartY;
    b = line1StartX - line2StartX;
    numerator1 = ((line2EndX - line2StartX) * a) - ((line2EndY - line2StartY) * b);
    numerator2 = ((line1EndX - line1StartX) * a) - ((line1EndY - line1StartY) * b);
    a = numerator1 / denominator;
    b = numerator2 / denominator;

    // if we cast these lines infinitely in both directions, they intersect here:
    result.x = line1StartX + (a * (line1EndX - line1StartX));
    result.y = line1StartY + (a * (line1EndY - line1StartY));
/*
        // it is worth noting that this should be the same as:
        x = line2StartX + (b * (line2EndX - line2StartX));
        y = line2StartX + (b * (line2EndY - line2StartY));
        */
    // if line1 is a segment and line2 is infinite, they intersect if:
    if (a > 0 && a < 1) {
        result.onLine1 = true;
    }
    // if line2 is a segment and line1 is infinite, they intersect if:
    if (b > 0 && b < 1) {
        result.onLine2 = true;
    }
    // if line1 and line2 are segments, they intersect if both of the above are true
    return result;
};

if (!Array.prototype.addAll) {
	Array.prototype.addAll = function(list) {
		for (var i=0; i<list.length; i++) {
			this.push(list[i]);
		}
	}
}

function Box(x, y, x2, y2) {
	this.x = x;
	this.y = y;
	this.x2 = x2;
	this.y2 = y2;
	this.width = x2-x;
	this.height = y2-y;
}
Box.prototype.moveTo = function(x, y) {
	this.x = x;
	this.y = y;
	this.x2 = x + this.width;
	this.y2 = y + this.height;
	this.cx = 0.5*(x + this.x2);
	this.cy = 0.5*(y + this.y2);
}

Box.prototype.moveCenterTo = function(cx, cy) {
	this.x = cx - 0.5*this.width;
	this.x2 = cx + 0.5*this.width;
	this.y = cy - 0.5*this.height;
	this.y2 = cy + 0.5*this.height;
	this.cx = cx;
	this.cy = cy;
}


Box.makeBox = function(box) {
	box.moveTo = Box.prototype.moveTo;
	box.moveCenterTo = Box.prototype.moveCenterTo;
	return box;
}

function ResourceNamer(graph) {
	this.graph = graph;
	this.defaultNamespace = "http://www.konig.io/entity/";
}

ResourceNamer.prototype.createIRI = function() {
	return this.defaultNamespace + uuid.v1();
}

ResourceNamer.prototype.propertyLabel = function(iriNode) {
	return iriNode.localName;
}

ResourceNamer.prototype.nodeLabel = function(node) {
	
	

	if (node instanceof Literal) {
		// TODO: truncate the string value if it is too long.
		return node.stringValue;
	}
	
	var vertex = 
		(node instanceof Vertex) ? node :
		this.graph.vertex(node);
	
	var label = 
		vertex.v().out(skos.prefLabel).first() ||
		vertex.v().out(skos.altLabel).first() ||
		vertex.v().out(rdfs.label).first() ||
		null;
	
	return label ? label.stringValue : vertex.id.localName;
}

ResourceNamer.prototype.isLabelProperty = function(predicateNodeOrStringValue) {
	var value = (typeof(predicateNodeOrStringValue) === 'string') ?
			predicateNodeOrStringValue : predicateNodeOrStringValue.stringValue;
	
	return RDFS.LABEL.stringValue == value;
}

ResourceNamer.prototype.vertexLabel = function(vertex) {
	var label = vertex.propertyValue(RDFS.LABEL);
	if (!label && (vertex.id instanceof IRI)) {
		return vertex.id.localName;
	}
	if (!label) {
		return vertex.id.stringValue;
	}
	return label.stringValue;
}



VertexData = function(rdfVertex, vertexView, statement) {
	this.rdfVertex = rdfVertex;
	this.vertexView = vertexView;
	this.statement = statement;
	this.loaded = false;
	this.bbox = null;
	this.rect = null;
}

/**
 * A sector that is open to receiving new vertices.
 */
OpenSector = function(layoutManager, prev, next) {
	this.layoutManager = layoutManager;
	this.layoutDataList = [];
	this.prev = prev;
	this.next = next;
	this.minAngle = this.maxAngle = this.maxRadius = 0;
	
	if (prev) {
		
//		console.log('prev', prev.toString());
		
		this.maxRadius = Math.max(prev.maxRadius, next.maxRadius);
		this.minAngle = prev.maxAngle;
		this.maxAngle = next.minAngle;
		
		
		if (this.maxAngle < 0) {
			this.maxAngle += 2*Math.PI;
		}
		
		if (this.minAngle > this.maxAngle) {
			this.minAngle -= 2*Math.PI;
		}
		if (this.maxAngle < this.minAngle) {
			this.maxAngle += 2*Math.PI;
		}
		
//		console.log(this.toString());
//		layoutManager.drawRay(this.minAngle, "red");
//		layoutManager.drawRay(this.maxAngle, "green");
		
	}
}

OpenSector.prototype.toString = function() {
	return "OpenSector(" + Math.degrees(this.minAngle) + ", " + Math.degrees(this.maxAngle) + ")";
}

OpenSector.prototype.radius = function() {
	var origin = this.layoutManager.origin;
	var pad = this.layoutManager.pad;
	
	var totalVertexWidth = this.totalVertexWidth();
	var radius = totalVertexWidth / (this.maxAngle - this.minAngle) ;
	radius += this.layoutManager.originHalfWidth;
	
	return Math.max(this.maxRadius + this.layoutManager.originHalfWidth + this.maxVertexWidth, radius, 4*pad);
}

OpenSector.prototype.snapAngles = function() {
	var twoPI = 2*Math.PI;

	var origin = this.layoutManager.origin;
	
	var box = this.layoutManager.originVertexData.bbox;

	var left = box.x - origin.x;
	var top = box.y - origin.y;
	var right = box.x2 - origin.x;
	var bottom = box.y2 - origin.y;
	
	var topLeft = Math.atan2(top, left);
	var topRight = Math.atan2(top, right);
	var bottomRight = Math.atan2(bottom, right);
	var bottomLeft = Math.atan2(bottom, left);
	
	var rightAngle = 0;
	var bottomAngle = 0.5*Math.PI;
	var leftAngle = Math.PI;
	var topAngle = 1.5*Math.PI;

	if (this.containsAngle(topAngle)) {
		this.snapAngle(topLeft+twoPI, topRight+twoPI, topAngle);
	}
	if (this.containsAngle(bottomAngle)) {
		this.snapAngle(bottomRight, bottomLeft, bottomAngle);
	}
	if (this.containsAngle(leftAngle)) {
		this.snapAngle(bottomLeft, topLeft+twoPI, leftAngle);
	}
		
	if (this.containsAngle(rightAngle)) {
		this.snapAngle(topRight, bottomRight, rightAngle);
	}
}

OpenSector.prototype.snapAngle = function(minAngle, maxAngle, theta) {
	var minDiff = 2*Math.PI;
	var best = null;
	var count = 0;
	for (var i=0; i<this.layoutDataList.length; i++) {
		var data = this.layoutDataList[i];

		var text = data.vertexData.vertexView.getLabelValue();
		if (sectorContainsAngle(minAngle, maxAngle, theta)) {
			count++;
			if (count > 1) {
				return;
			}
		}
		var diff = angularDistance(data.theta, theta);
		if (diff < minDiff) {
			minDiff = diff;
			best = data;
			
		}

	}
	if (best) {
		best.theta = theta;
		var text = best.vertexData.vertexView.getLabelValue();
//		console.log("SNAP ANGLE OF " + text + ": " + Math.degrees(theta));
	}
}



OpenSector.prototype.compactLayout = function() {
	this.angularLayout();
//	this.snapAngles();
	this.pushOut();
	this.pullIn();
	this.commit();
}

OpenSector.prototype.commitCenter = function() {
	var list = this.layoutDataList;
	for (var i=0; i<list.length; i++) {
		var data = list[i];
		var p = data.point;
		var box = data.bbox;
		var x = p.x - 0.5*box.width;
		var y = p.y - 0.5*box.height;
		data.vertexData.vertexView.moveTo(x, y);
	}
}

OpenSector.prototype.commit = function() {
	var list = this.layoutDataList;
	for (var i=0; i<list.length; i++) {
		var data = list[i];
		var p = data.point;
		data.vertexData.vertexView.moveTo(p.x, p.y, true);
	}
}

OpenSector.prototype.angularLayout = function() {

	// Special case when there is only one vertex to layout and this sector is 
	// the entire circle.  This forces us to start of in the right direction.
	if (this.minAngle === 0 && this.maxAngle === 2*Math.PI && this.layoutDataList.length==1) {
		this.layoutDataList[0].theta = 0;
		return;
	}
	
	var dtheta = (this.maxAngle - this.minAngle)/(this.layoutDataList.length+1);
	
	
	
	var theta = this.minAngle;
	for (var i=0; i<this.layoutDataList.length; i++) {
		var layoutData = this.layoutDataList[i];
		theta += dtheta;
		layoutData.theta = theta;
		
		var text = layoutData.vertexData.vertexView.getLabelValue();
	}
}

OpenSector.prototype.pushOut = function() {
	var radius = this.radius(); 
	var origin = this.layoutManager.origin;
	
	var paper = this.layoutManager.rdfController.presenter.paper;
	
	
	for (var i=0; i<this.layoutDataList.length; i++) {
		var layoutData = this.layoutDataList[i];
		var theta = layoutData.theta;
		
		var box = layoutData.bbox;
		
		var x = origin.x + radius*Math.cos(theta);
		var y = origin.y + radius*Math.sin(theta);
		
		box.moveCenterTo(x,y);
		layoutData.rect.fromBBox(box);
		layoutData.point.moveTo(x, y);
		
//		var line = paper.line(origin.x, origin.y, x, y).attr({stroke: "red"});
		
		
		var dx = box.cx - origin.x;
		var dy = box.cy - origin.y;
		theta = Math.atan2(dy,dx);
		var text = layoutData.vertexData.vertexView.getLabelValue();
//		console.log("PUSH-OUT ANGLE OF " + text + ": " + positiveDegrees(theta));
	}
	
}

OpenSector.prototype.pullIn = function() {

	var fixed = [];
	if (this.prev) {
		fixed.addAll(this.prev.vertexDataList);
		if (this.next != this.prev) {
			fixed.addAll(this.next.vertexDataList);
		}
	}
	var list = this.layoutDataList;
	for (var i=0; i<list.length; i++) {
		var layoutData = list[i];
		this.pullInNode(layoutData, fixed);
	}
}

OpenSector.prototype.pullInNode = function(target, fixed) {
	
	var targetRect = target.rect;
	var origin = this.layoutManager.originVertexData.rect;
	
	/*
	 *           |          p           |  Target
	 *           +----------*-----------+
	 *                      \
	 *                       \
	 *                        \
	 *                         \
	 *                          \
	 *                 +---------*-----------+
	 *                 |         q           |  Origin
	 */
	
	
	
	var q = origin.pointClosestTo(targetRect.cx, targetRect.cy);
	var p = targetRect.pointClosestTo(q);

	
	// u = p - q
	// Vector in direction from q to p.
	var u = p.clone().add(q, -1); 

	// Normalize vector u
	var uLength = u.length();
	u.scale(1.0/uLength);
	
	var textLength = target.arcLabelLength;
	
	// Move q in the direction u a distance of textLength.
	q.add(u, textLength);

	
	var minDistance = p.squaredDistanceTo(q);
	
	
	var endPoint = q.clone();
	
	// For each corner of the bbox around the target node.
	// Construct a segment parallel to pq, and check if it
	// intersects with the sides of any other nodes.
	
	for (var i=0; i<4; i++) {
		var segment = target.rect.side[i];
		var line1StartX = segment.x1;
		var line1StartY = segment.y1;
		var line1EndX = q.x;
		var line1EndY = q.y;
		
		for (var j=0; j<fixed.length; j++) {
			
			var node = fixed[j];
			for (var k=0; k<4; k++) {
				var side = node.rect.side[k];
				var line2StartX = side.x1;
				var line2StartY = side.y1;
				var line2EndX = side.x2;
				var line2EndY = side.y2;
				
				var info = checkLineIntersection(
					line1StartX, line1StartY, line1EndX, line1EndY,
					line2StartX, line2StartY, line2EndX, line2EndY
				);
				
				if (info.onLine1 && info.onLine2) {
					
					var dx = p.x-info.x;
					var dy = p.y-info.y;
					var d = dx*dx + dy*dy;
					if (d < minDistance) {

						q.x = info.x;
						q.y = info.y;
						
						minDistance = d;
					}
				}
			}
		}
		
	}
	var distance = Math.sqrt(minDistance);

	// r = q-p;
	var r = q.add(p, -1);
	var box = target.bbox;
	p = target.point;
	p.moveTo(box.x, box.y);
	p.add(r);
	
	fixed.push(target.vertexData);

	box.moveTo(p.x, p.y);
	target.rect.fromBBox(target.bbox);
	target.vertexData.bbox = box;
	target.vertexData.rect = target.rect;
}

OpenSector.prototype.layout = function() {
	var origin = this.layoutManager.origin;
	
	var totalVertexWidth = this.totalVertexWidth();
	var radius = (this.maxAngle - this.minAngle) / totalVertexWidth;
	radius += this.layoutManager.originHalfWidth;
	
	radius = Math.max(this.maxRadius, radius);
	
	var dtheta = (this.maxAngle - this.minAngle)/(this.layoutDataList.length+1);
//	console.log("Sector(" + Math.degrees(this.minAngle) + ', ' + Math.degrees(this.maxAngle) + ")");
	
	var theta = this.minAngle;
	for (var i=0; i<this.layoutDataList.length; i++) {
		var layoutData = this.layoutDataList[i];
		theta += dtheta;
		var dr = (theta>Math.PI/2.0 && theta<3*Math.PI/2.0) ?
			layoutData.bbox.width : 0;
		
		var dy = 0.5*layoutData.bbox.height;
		
		var r = radius + dr;
		var x = origin.x + r*Math.cos(theta);
		var y = origin.y + r*Math.sin(theta) - dy;
		
		layoutData.vertexData.vertexView.moveTo(x, y);
		
		
	}
}

OpenSector.prototype.totalVertexWidth = function() {
	this.maxVertexWidth = 0;
	var sum = 0;
	for (var i=0; i<this.layoutDataList.length; i++) {
		var data = this.layoutDataList[i];
		var width = data.bbox.width;
		if (width > this.maxVertexWidth) {
			this.maxVertexWidth = width;
		}
		sum += width;
	}
	return sum;
}

OpenSector.prototype.containsAngle = function(theta) {
	return sectorContainsAngle(this.minAngle, this.maxAngle, theta);
}

OpenSector.prototype.maxArcLength = function() {
	return (this.maxAngle - this.minAngle) * this.maxRadius;
}

LayoutData = function(vertexData) {
	this.vertexData = vertexData;
	this.arcLabelLength = 0;
	this.bbox = Box.makeBox(vertexData.vertexView.getBBox());
	this.rect = new Rectangle();
	this.halfWidth = 0.5*this.bbox.width;
	this.point = new Point(0,0);
}

LayoutData.prototype.incrementArcLabelLength = function(arcEnd, pad) {
	var len = arcEnd.textLength;
	if (len > 0) {
		this.arcLabelLength += (len + pad);
	}
}

/**
 * @classdesc A structure that asserts whether or not the position of a 
 * given vertex is fixed.  This is useful when performing the layout for an
 * RDF statement.  The vertex referenced by a ParticipantInfo structure represents 
 * either the subject or object of the statement.
 * 
 * @param {VertexData} vertexData The VertexData for the participant.
 * @param {boolean} isFixed A flag which specifies whether the position of the
 * participant is fixed.
 */
ParticipantInfo = function(vertexData, isFixed) {
	this.vertexData = vertexData;
	this.isFixed = isFixed;
}

LayoutManager = function(rdfController) {
	this.rdfController = rdfController;
	this.sectorList = [];
	this.openSectorList = [];
	this.fixedVertices = [];
	this.fixedArcData = [];
	this.layoutDataList = [];
	this.newArcList = [];
	this.arcLength = 0;
	this.totalArcAngle = 0;
	this.pad = rdfController.presenter.arcLabelPaddingLeft;
	this.originRequiresPlacement = false;
}

/**
 * Find an appropriate place for a vertex relative to the other vertices that have
 * already been placed.
 * @param {VertexData} vertexData The VertexData for the vertex that is to be placed.
 */
LayoutManager.prototype.placeVertex = function(vertexData) {
	this.placeOrigin(vertexData);
	this.collectFixedVertices();
	this.moveOrigin();
}

LayoutManager.prototype.drawRay = function(angle, color) {
	var paper = this.rdfController.presenter.paper;
	var radius = 200;
	var x0 = this.origin.x;
	var y0 = this.origin.y;
	
	var x1 = x0 + radius*Math.cos(angle);
	var y1 = y0 + radius*Math.sin(angle);
	
	
	paper.line(x0, y0, x1, y1).attr({stroke: color, fill: "none"});
}




LayoutManager.prototype.layoutDataForVertexView = function(vertexView) {
	if (!vertexView) {
		return null;
	}
	var list = this.layoutDataList;
	for (var i=0; i<list.length; i++) {
		var data = list[i];
		if (data.vertexData.vertexView === vertexView) {
			return data;
		}
	}
	return null;
}

LayoutManager.prototype.setOrigin = function(originVertexData) {
	this.originVertexData = originVertexData;
	
	var box = this.originVertexData.bbox = originVertexData.vertexView.getBBox();
	Box.makeBox(box);
	this.origin = new Point(box.cx, box.cy);
	this.originHalfWidth = 0.5*box.width;
	originVertexData.rect = new Rectangle(box);
	
}

LayoutManager.prototype.placeOrigin = function(originVertexData, viewBox) {
	this.viewBox = viewBox || this.viewBox || this.rdfController.presenter.viewBox().getBox();
	this.setOrigin(originVertexData);
	this.originRequiresPlacement = true;
}

LayoutManager.prototype.rectAroundFixedVertices = function() {
	var x = Number.POSITIVE_INFINITY;
	var y = Number.POSITIVE_INFINITY;
	var x2 = Number.NEGATIVE_INFINITY;
	var y2 = Number.NEGATIVE_INFINITY;
	
	var list = this.fixedVertices;
	for (var i=0; i<list.length; i++) {
		var vertexData = list[i];
		var bbox = vertexData.vertexView.getBBox();

		x = Math.min(x, bbox.x);
		y = Math.min(y, bbox.y);
		x2 = Math.max(x2, bbox.x2);
		y2 = Math.max(y2, bbox.y2);
	}
	
	return new Rectangle({x:x, y:y, x2:x2, y2:y2});
}

LayoutManager.prototype.moveOrigin = function() {
	if (this.originRequiresPlacement && this.fixedVertices.length > 0) {
		
		var point = this.centerOfMass();
		
		if (!point) {
			point = new Point(this.viewBox.cx, this.viewBox.cy);
		}

		
		
		var rect = this.rectAroundFixedVertices();
		
		point = rect.pointClosestTo(point);
		
		var normal = NORMAL[point.side];
		
		var pad = this.pad;
		
		var qMax = 2*pad;

		var subject = this.originVertexData.vertexView;
		var arcList = subject.arcList;
		var controller = this.rdfController;
		
		for (var i=0; i<arcList.length; i++) {

			var arc = arcList[i];
			var endA = arc.otherEnd(subject);
			if (endA) {
				var dataA = this.layoutDataForVertexView(endA.vertex);
				if (!dataA) {
					
					var endB = arc.otherEnd(endA);
					var aTextLength = endA.textLength;
					var bTextLength = endB.textLength;
					var length = aTextLength + bTextLength;
					if (aTextLength>0 && bTextLength>0) {
						length += pad;
					}
					length += 4*pad;
					
					var vertexData = controller.getVertexDataFromView(endA.vertex);
					
					var rect = new Rectangle();
					rect.fromBBox(vertexData.bbox);
					var a = rect.pointClosestTo(point);
					var q = pointOnRayAtDistanceFromReferencePoint(a, point, normal, length);
					

					if (q > qMax) {
						qMax = q;
					}
				}
				
			}
		}
		
		point.add(normal, qMax);
		
		
		var box = this.originVertexData.bbox;
		Box.makeBox(box);
		
		var center = new Point(0.5*box.width, 0.5*box.height);
		var distance = 0.5*center.dot(normal);
		
		point.add(normal, distance);
		box.moveCenterTo(point.x, point.y);
		
		subject.moveTo(box.x, box.y);
		this.origin.moveTo(box.cx, box.cy);

		this.originVertexData.rect.fromBBox(box);

		
		
		
	}
}


/**
 * Computes the average position of the fixed vertices that are connected 
 * to the origin vertex.
 */
LayoutManager.prototype.centerOfMass = function() {
	var subject = this.originVertexData.vertexView;
	
	var arcList = subject.arcList;
	var x = 0;
	var y = 0;
	var count = 0;
	for (var i=0; i<arcList.length; i++) {
		var arc = arcList[i];
		var other = arc.otherEnd(subject);
		if (other != null) {

			var otherData = this.layoutDataForVertexView(other.vertex);
			if (!otherData) {
				count++;
				var box = other.vertex.getBBox();
				x += box.cx;
				y += box.cy;
			}
		}
	}
	if (count > 0) {
		return new Point(x/count, y/count);
	}
	
	return null;
	
	
	
}


LayoutManager.prototype.layout = function() {
	this.collectFixedVertices();
	this.collectFixedArcData();
	this.collectLayoutData();
	this.moveOrigin();
	this.buildSectors();
	this.mergeSectors();
	this.sortSectors();
	this.buildOpenSectors();
	this.analyzeLabels();
	this.placeVertices();
	this.computeRoutes();
	
}

LayoutManager.prototype.computeRoutes = function() {
	// TODO: Make this method more efficient.
	// There is lots of redundant routing happening.
	var router = this.rdfController.presenter.router;
	for (var i=0; i<this.newArcList.length; i++) {
		var arc = this.newArcList[i];
		router.rerouteVertex(arc.a.vertex, {});
		router.rerouteVertex(arc.b.vertex, {});
		arc.draw();
	}
}

LayoutManager.prototype.analyzeLabels = function() {
	
	var pad = this.pad;
	for (var i=0; i<this.layoutDataList.length; i++) {
		var layoutData = this.layoutDataList[i];
		var vertexView = layoutData.vertexData.vertexView;
		var arcList = vertexView.arcList;
		for (var j=0; j<arcList.length; j++) {
			var arc = arcList[j];
			layoutData.incrementArcLabelLength(arc.a, pad);
			layoutData.incrementArcLabelLength(arc.b, pad);
			if (layoutData.arcLabelLength===0) {
				layoutData.arcLabelLength = 2*pad;
			} else {
				layoutData.arcLabelLength += pad;
			}
		}
		
	}
}

LayoutManager.prototype.placeVertices = function() {
//	if (this.layoutDataList.length == 1) {
//		this.placeOne();
//	} else {
//		this.placeMany();
//	}
	this.placeMany();
}

LayoutManager.prototype.placeOne = function() {
	var sector = this.findSectorByAngle(0);
	
	if (sector == null) {
		this.placeMany();
	} else {
		var layoutData = this.layoutDataList[0];
		var vertexView = layoutData.vertexData.vertexView;
		var x = this.origin.x + this.originHalfWidth + layoutData.arcLabelLength;
		var y = this.origin.y - 0.5*layoutData.bbox.height;
		vertexView.moveTo(x, y);
	}
}

LayoutManager.prototype.placeMany = function() {
	this.computeTotalArcAngle();
	this.distribute();
	for (var i=0; i<this.openSectorList.length; i++) {
		var sector = this.openSectorList[i];
		sector.compactLayout();
	}
}

LayoutManager.prototype.distribute = function() {
	var index = 0;
	for (var i=0; i<this.openSectorList.length; i++) {
		var sector = this.openSectorList[i];
		var end = 0;
		if (i == (this.openSectorList.length-1)) {
			end = this.layoutDataList.length;
		} else {

			var fraction = (sector.maxAngle-sector.minAngle)/this.totalArcAngle;
			var count = parseInt(fraction*this.layoutDataList.length + 0.5);
			end = index + count;
		}
		for (var j=index; j<end; j++) {
			var layoutData = this.layoutDataList[j];
			sector.layoutDataList.push(layoutData);
		}
		index = end;
	}
}

LayoutManager.prototype.computeTotalArcAngle = function() {
	var sum = 0;
	for (var i=0; i<this.openSectorList.length; i++) {
		var sector = this.openSectorList[i];
		sum += (sector.maxAngle - sector.minAngle);
	}
	this.totalArcAngle = sum;
}

LayoutManager.prototype.findSectorByAngle = function(theta) {
	for (var i=0; i<this.openSectorList.length; i++) {
		var sector = this.openSectorList[i];
		if (sector.containsAngle(theta)) {
			return sector;
		}
	}
	return null;
}

LayoutManager.prototype.collectLayoutData = function() {
	var rdfVertex = this.originVertexData.rdfVertex;
	var outStatements = rdfVertex.outStatements();
	var inStatements = rdfVertex.inStatements();
	
	this.analyzeStatements(OUT, outStatements);
	this.analyzeStatements(IN, inStatements);
}

LayoutManager.prototype.analyzeStatements = function(direction, list) {

	var controller = this.rdfController;
	var namer = controller.namer;
	
	var x = this.origin.x;
	var y = this.origin.y;
	
	for (var i=0; i<list.length; i++) {
		var statement = list[i];
		if (namer.isLabelProperty(statement.predicate)) {
			continue;
		}
		var subject = statement.subject;
		var object = statement.object;
		var target = (direction==OUT) ? object : subject;
		
		var vertexData = controller.getVertexData(target, statement);
		if (!vertexData) {
			vertexData = controller.vertexData(x, y, target, statement);

			if (!subject.equals(object)) {
				this.layoutDataList.push(new LayoutData(vertexData));
			}
		}

		var arcKey = statement.key();
		var arcData = controller.arcMap[arcKey];
		if (!arcData) {
			var arcLabel = statement.predicate.localName;


			var aVertexView = this.originVertexData.vertexView;
			var bVertexView = vertexData.vertexView;
			
			
			var arc = controller.presenter.arc(aVertexView, bVertexView);

			var arcEnd = (direction==OUT) ? arc.b : arc.a;
			arcEnd.setLabel(arcLabel, true);
			controller.bindStatement(statement, arcEnd);
			this.newArcList.push(arc);
			
			var event = new RdfStatementBindEvent(statement, arcEnd);
			controller.presenter.notifyHandlers(event);
		}
	
	}
	this.joinPeerToPeer();
}

LayoutManager.prototype.layoutDataByRdfKey = function(key) {
	for (var i=0; i<this.layoutDataList.length; i++) {
		var data = this.layoutDataList[i];
		var vertexKey = data.vertexData.rdfVertex.id.key();
		if (key === vertexKey) {
			return i;
		}
	}
	return -1;
}

LayoutManager.prototype.joinPeerToPeer = function() {

	var originVertexKey = this.originVertexData.rdfVertex.id.key();
	
	for (var i=0; i<this.layoutDataList.length; i++) {
		var data = this.layoutDataList[i];
		var rdfVertex = data.vertexData.rdfVertex;
		var subjectView = data.vertexData.vertexView;
		
		if (!rdfVertex) {
			// TODO:  Need to understand how it is possible for this condition to occur.
			continue;
		}
		var outStatements = rdfVertex.outStatements();
		for (var j=0; j<outStatements.length; j++) {
			var s = outStatements[j];

			var objectKey = s.object.key();
			if (objectKey !== originVertexKey) {
				var objectView = this.rdfController.vertexViewById(s.object.key());
				if (objectView) {

					var arcLabel = s.predicate.localName;
					var arcView = this.rdfController.presenter.arc(subjectView, objectView);
					arcView.b.setLabel(arcLabel);
					this.rdfController.bindStatement(s, arcView.b);
					this.newArcList.push(arcView);
				}
			}
			
			
		}
		
		var inStatements = rdfVertex.inStatements();
		var objectView = subjectView;
		for (var j=0; j<inStatements.length; j++) {
			var s = inStatements[j];
			if (s.subject.key() !== originVertexKey) {
				subjectView = this.rdfController.vertexViewById(s.subject.key());
				if (subjectView) {
					var layoutIndex = this.layoutDataByRdfKey(s.subject.key());
					if (layoutIndex === -1) {
						var arcLabel = s.predicate.localName;
						var arcView = this.rdfController.presenter.arc(subjectView, objectView);
						arcView.b.setLabel(arcLabel);
						this.rdfController.bindStatement(s, arcView.b);
						this.newArcList.push(arcView);
					}
				}
			}
		}
	}
}


LayoutManager.prototype.buildOpenSectors = function() {
//	console.log('-------------------------');
	if (this.sectorList.length==0) {
		var s = new OpenSector(this);
		s.minAngle = 0;
		s.maxAngle = 2*Math.PI;
		
		this.openSectorList.push(s);
		return;
	}
	var prev = this.sectorList[this.sectorList.length-1];
	for (var i=0; i<this.sectorList.length; i++) {
		var nextIndex = i+1;
		if (nextIndex === this.sectorList.length) {
			nextIndex = 0;
		}
		var next = this.sectorList[nextIndex];
		
		var s = new OpenSector(this, prev, next)
//		console.log(s.toString());
		this.openSectorList.push(s);
		prev = this.sectorList[i];
		
	}
}

LayoutManager.prototype.sortSectors = function() {
	this.sectorList.sort(function(a,b){
		return a.minAngle - b.minAngle;
	});
}

LayoutManager.prototype.mergeSectors2 = function() {

	this.sectorList.sort(function(a,b){
		return 
			(a.minAngle < b.minAngle) ? -1 : 
			(a.minAngle > b.minAngle) ? 1 :
			0;
	});
	
	var merged = false;
	outerLoop: for (var i=0; i<this.sectorList.length; i++) {
		var s1 = this.sectorList[i];
		
		for (var j=i+1; j<this.sectorList.length; j++) {
			var s2 = this.sectorList[j];
			
//			console.log("COMPARE "  + s1.toString() + ' AND ' + s2.toString());
			if (s2.overlaps(s1)) {
				s2.consume(s1);
//				console.log('MERGED AS (' + Math.degrees(s2.minAngle) + ', ' + Math.degrees(s2.maxAngle) + ')');
				this.sectorList[i] = null;
				merged = true;
				continue outerLoop;
			}
//			console.log("NOT MERGED");
		}
	}
	if (!merged) {
		return;
	}
	var newList = [];
	for (var i=0; i<this.sectorList.length; i++) {
		var s = this.sectorList[i];
		if (s) {
			newList.push(s);
		}
	}
	
	this.sectorList = newList;
}


LayoutManager.prototype.mergeSectors = function() {
	if (this.sectorList.length<2) {
		return;
	}
	
	for (var i=0; i<this.sectorList.length; i++) {
		for (var j=0; j<i; j++) {
			var a = this.sectorList[i];
			var b = this.sectorList[j];
			
			if (a && b && a.overlaps(b)) {
				a.consume(b);
				this.sectorList[j] = null;
			}
		}
	}
	var newList = [];
	for (var i=0; i<this.sectorList.length; i++) {
		var a = this.sectorList[i];
		if (a) {
			newList.push(a);
		}
	}
	this.sectorList = newList;
}

LayoutManager.prototype.collectFixedArcData = function() {
	var controller = this.rdfController;
	var arcList = controller.presenter.arcList();
	for (var i=0; i<arcList.length; i++) {
		var arcView = arcList[i];
		if (arcView.a.statement || arcView.b.statement) {
			this.fixedArcData.push(arcView);
			
		} else {
			console.log('ArcView ends are not bound to statements');
		}
	}
}

LayoutManager.prototype.collectFixedVertices = function() {
	
	var vertexViewList = this.rdfController.presenter.vertexList();
	for (var i=0; i<vertexViewList.length; i++) {
		var v = vertexViewList[i];
		var resource = v.attr('resource');
		if (!resource) {
			console.log("Resource not found on vertex")
		} else {
			var data = this.rdfController.getVertexData(resource);
			if (data != this.originVertexData ) {
				this.fixedVertices.push(data);
				data.bbox = Box.makeBox(v.getBBox());
				data.rect = new Rectangle(data.bbox);
			}
		}
	}
}

LayoutManager.prototype.buildSectors = function() {
	var origin = this.origin;
	var fixed = this.fixedVertices;
	for (var i=0; i<fixed.length; i++) {
		var data = fixed[i];
		var sector = new Sector();
		sector.vertexDataList.push(data);
		var box = data.bbox;
		sector.analyzeRay(origin, box.x, box.y);
		sector.analyzeRay(origin, box.x2, box.y);
		sector.analyzeRay(origin, box.x2, box.y2);
		sector.analyzeRay(origin, box.x, box.y2);
		sector.normalizeRange();
		this.sectorList.push(sector);
//		console.log(sector.toString());
	}
	
	
	fixed = this.fixedArcData;
	for (var i=0; i<fixed.length; i++) {
		var arcView = fixed[i];
		var sector = new Sector();
		sector.arcDataList.push(arcView);
		var a = arcView.a;
		var b = arcView.b;
		sector.analyzeRay(origin, a.point.x, a.point.y);
		sector.analyzeRay(origin, b.point.x, b.point.y);
		sector.normalizeRange();
		if (sector.minAngle != sector.maxAngle) {
			this.sectorList.push(sector);
		}
//		console.log(sector.toString());
		
	}
	
//	console.log('--------------------------');
}


Sector = function() {
	this.minAngle = Number.MAX_VALUE;
	this.maxAngle = 0;
	this.minRadius = Number.MAX_VALUE;
	this.maxRadius = 0;
	this.vertexDataList = [];
	this.arcDataList = [];
}

Sector.prototype.consume = function(other) {
//	console.log('-----------------------------------------');
//	console.log("merge", other.toString());
//	console.log("with", this.toString());
	
	
	var min1 = this.minAngle;
	var max1 = this.maxAngle;
	
	var min2 = other.minAngle;
	var max2 = other.maxAngle;
	
	var twoPI = 2*Math.PI;
	
	var tolerance = Math.PI/100.0;
	
	if ((min1>=0 && min2>=0) || (min1<0 && min2<0)) {
		this.minAngle = Math.min(min1, min2);
		this.maxAngle = Math.max(max1, max2);
		
	} else if (min1<0) {
		
		if (sectorContainsAngleWithTolerance(min1, max1, max2, tolerance)) {
			this.minAngle = Math.min(min1, min2 - twoPI);
		} else {
			this.maxAngle = Math.max(max1, max2);
		}
		
		
	} else if (min2<0) {
		
		if (sectorContainsAngleWithTolerance(min2, max2, max1, tolerance)) {
			this.minAngle = Math.min(min2, min1 - twoPI);
			this.maxAngle = max2;
		} else {
			console.log('sector does not contain angle');
			this.minAngle = min2;
			this.maxAngle = Math.max(max1, max2);
		}
		
	}
	
	
	
	
//	if (
//		(this.minAngle<0 && other.minAngle<0) ||
//		(this.minAngle>0 && other.minAngle>0)
//	) {
//		this.minAngle = Math.min(this.minAngle, other.minAngle); 
//	} else if (this.minAngle<0) {
//	}
//	
//	
//	this.maxAngle = Math.max(this.maxAngle, other.maxAngle);
	
	
	
	this.minRadius = Math.min(this.minRadius, other.minRadius);
	this.maxRadius = Math.max(this.maxRadius, other.maxRadius);
	this.vertexDataList = this.vertexDataList.concat(other.vertexDataList);
	this.arcDataList = this.arcDataList.concat(other.arcDataList);
	
//	console.log("yields", this.toString());
}

Sector.prototype.toString = function() {
	var buffer = "Sector(" + Math.degrees(this.minAngle) + 
		', ' + Math.degrees(this.maxAngle) + ')[';
	
	for (var i=0; i<this.vertexDataList.length; i++) {
		var data = this.vertexDataList[i];
		if (i>0) {
			buffer += ', ';
		}
		buffer += data.vertexView.getLabelValue();
	}
	for (var i=0; i<this.arcDataList.length; i++) {
		var arcView = this.arcDataList[i];
		if (i>0 || this.vertexDataList.length>0) {
			buffer += ', ';
		}
		buffer += arcView.toString();
	}
	buffer += ']';
	return buffer;
}

Sector.prototype.analyzeRay = function(start, endX, endY) {
	var dx = endX - start.x;
	var dy = endY - start.y;
	
	var radius = distance(start.x, start.y, endX, endY);
	if (radius < this.minRadius) {
		this.minRadius = radius;
	}
	if (radius > this.maxRadius) {
		this.maxRadius = radius;
	}
	var theta = Math.atan2(dy, dx);
//	if (theta < 0) {
//		theta += 2*Math.PI;
//	}
	if (theta < this.minAngle) {
		this.minAngle = theta;
	}
	if (theta > this.maxAngle) {
		this.maxAngle = theta;
	}
	
}


Sector.prototype.overlaps = function(other) {
	// FIXME
	var a1 = this.minAngle;
	var a2 = this.maxAngle;
	var b1 = other.minAngle;
	var b2 = other.maxAngle;
	
	var tolerance = Math.PI/100.0;
	
	
	var result =
		sectorContainsAngle(a1, a2, b1) ||
		sectorContainsAngle(a1, a2, b2) ||
		sectorContainsAngle(b1, b2, a1) ||
		sectorContainsAngle(b1, b2, a2) ||
		angularDistance(b1, a1) < tolerance ||
		angularDistance(b1, a2) < tolerance ||
		angularDistance(b2, a1) < tolerance ||
		angularDistance(b2, a2) < tolerance;
	

//	console.log("Compare ", this.toString());
//	console.log("with", other.toString());
//	console.log("result", result);
	
	return result;
	
}

Sector.prototype.normalizeRange = function() {
	var delta = this.maxAngle - this.minAngle;

	if (delta > Math.PI) {

		var max = this.minAngle;
		this.minAngle = this.maxAngle - 2*Math.PI;
		this.maxAngle = max;
		
//		var max = 2*Math.PI + this.minAngle;
//		this.minAngle = this.maxAngle;
//		this.maxAngle = max;
	}

//	console.log("Normalize Range", Math.degrees(delta), this.toString());
}

/************************************************************************/
BindProperty = function(arcEnd, finalProperty, initialProperty) {
	this.type = "BindProperty";
	this.object = finalProperty;
	this.target = arcEnd;
	this.initialProperty = initialProperty;
	
}

BindProperty.prototype.serialize = function() {
	var json = {
		"@type" : this.type,
		object: this.object.stringValue,
		target: this.target.getId()
	};
	if (this.initialProperty) {
		json.initialProperty = this.initialProperty.stringValue;
	}
	return json;
}

// TODO: Eliminate RdfStatementBindEvent and use BindProperty only.

/************************************************************************/
/**
 * @classdesc An event announcing that a given RDF statement has been bound
 * to an ArcEnd.
 */
RdfStatementBindEvent = function(statement, arcEnd) {
	this.type = "RdfStatementBind";
	this.statement = statement;
	this.arcEnd = arcEnd;
	this.suppressPublication = true;
}

/************************************************************************/
MarshalingRdfStatementBindEvent = function(statement, arcEnd) {
	RdfStatementBindEvent.call(this, statement, arcEnd);
}

MarshalingRdfStatementBindEvent.prototype = Object.create(RdfStatementBindEvent.prototype);
MarshalingRdfStatementBindEvent.constructor = MarshalingRdfStatementBindEvent;

MarshalingRdfStatementBindEvent.prototype.marshal = function(doc) {
	var s = this.statement;
	doc.actions.push({
		"@type" : "RdfStatementBind",
		statement : {
			subject: s.subject.stringValue,
			predicate: s.predicate.stringValue,
			object: s.object.stringValue
		},
		arcEnd: {
			arc: this.arcEnd.arc.path.attr('id'),
			accessor: this.arcEnd.accessor()
		}
	});
}

/************************************************************************/
RdfVertexDelete = function(resourceId) {
	this.type = "RdfVertexDelete";
	this.resourceId = resourceId;
}
/************************************************************************/

RdfVertexBindEvent = function(vertexData) {
	this.type = "RdfVertexBind";
	this.vertexData = vertexData;
}

/************************************************************************/

RdfStatementUnbindEvent = function(statement, arcEnd) {
	this.type = "RdfStatementUnbind";
	this.statement = statement;
	this.arcEnd = arcEnd;
}

/************************************************************************/

RdfVertexUnbindEvent = function(vertexData) {
	this.type = "RdfVertexUnbind";
	this.vertexData = vertexData;
}

/************************************************************************/
ChangeSetAction = function(changeSet, graph, rdfController) {
	ApplyChangeSet.call(this, changeSet, graph);
	this.rdfController = rdfController;
}
ChangeSetAction.prototype = Object.create(ApplyChangeSet.prototype);
ChangeSetAction.constructor = ChangeSetAction;

ChangeSetAction.prototype.redo = function() {
	ApplyChangeSet.prototype.redo.call(this);
	this.rdfController.redoChangeSet(this);
}

ChangeSetAction.prototype.undo = function() {
	ApplyChangeSet.prototype.undo.call(this);
	this.rdfController.undoChangeSet(this);
}


/************************************************************************/
RdfController = function(presenter) {
	
	this.graph = rdf.graph();
	this.presenter = presenter;
	this.vertexMap = {};
	this.arcMap = {};
	this.namer = new ResourceNamer(this.graph);
	
	var selectHandler = new RDFSelectHandler(this);
	presenter.bind("SelectPresenterElement", selectHandler.select, selectHandler);
	presenter.bind("DeleteElement", this.deleteElement, this);
	presenter.bind("AddElement", this.addElement, this);
	presenter.addElementDecorator(this);
	presenter.vertexCreator = this;
}



RdfController.prototype.getRdfObjectFromView = function(vertexView) {

	var data = vertexView.rdfControllerData;
	if (data && data.rdfVertex) {
		return data.rdfVertex;
	}
	
	var value = vertexView.getLabelValue();
	var rdfType = vertexView.getRdfType();
	
	if (rdfType) {
		for (var i=0; i<rdfType.length; i++) {
			var type = rdfType[i];
			var iri = new IRI(type);
			// For now, we assume that literals are always in the XSD namespace.
			// TODO: Implement more generic type handling mechanism.
			
			var namespace = iri.namespace;
			if (namespace === xsd.NAMESPACE) {
				return this.graph.typedLiteral(value, iri);
			}
		}
	}
	
	return null;
}

RdfController.prototype.changeArcEndLabel = function(end) {
	
	var object = this.getRdfObjectFromView(end.vertex);
	
	
	if (object) {
		
		var oldStatement = end.statement;
		var initialProperty = oldStatement ? oldStatement.predicate : null;
		
		var arc = end.arc;
		var otherEnd = arc.otherEnd(end);
		var subject = this.getRdfVertexFromView(otherEnd.vertex);
		if (subject) {
			// TODO: check to see if there is another Property with the
			// same label.
			
			var label = this.graph.langString(end.snapTextValue, "en");
			
			
			
			var predicate = new IRI(this.namer.createIRI());
			var statement = this.graph.statement(subject, predicate, object);
			var s2 = this.graph.statement(predicate, RDF.TYPE, RDF.PROPERTY);
			var s3 = this.graph.statement(predicate, RDFS.LABEL, label);
			
			this.bindStatement(statement, end);

			var actionHistory = this.presenter.actionHistory;
			var txn = actionHistory.beginTransaction();
			
			var changeSet = new ChangeSet();
			changeSet.addStatement(s2);
			changeSet.addStatement(s3);
			changeSet.addStatement(statement);
			
			var apply = new ChangeSetAction(changeSet, this.graph, this);
			actionHistory.append(apply);
			actionHistory.append(new BindProperty(end, predicate, initialProperty));
			
			
			var event = new MarshalingRdfStatementBindEvent(statement, end);
			actionHistory.append(event);
			actionHistory.commit(txn);
		}
		
	}
}

RdfController.prototype.marshalArc = function(arcView, json) {
	if (arcView.a.statement) {
		json.a.resource = arcView.a.statement.predicate.stringValue;
	}
	if (arcView.b.statement) {
		json.b.resource = arcView.b.statement.predicate.stringValue;
	}
}


RdfController.prototype.showStatement = function(statement) {
	var arcData = this.arcMap[statement.key()];
	if (!arcData) {
		this.suppressAddElementEvents = true;
		var subject = this.getVertexData(statement.subject, statement);
		var object = this.getVertexData(statement.object, statement);
		
		var viewBox = this.presenter.viewBox().getBox();
		var cx = viewBox.cx;
		var cy = viewBox.cy;
		
		if (!subject) {
			var layoutManager = new LayoutManager(this);
			subject = this.vertexData(cx, cy, statement.subject);
			layoutManager.placeVertex(subject);
		}
		var placeObject = false;
		if (!object) {
			placeObject = true;
			object = this.vertexData(cx, cy, statement.object, statement);
		}
		

		var arcLabel = statement.predicate.localName;
		arcView = this.presenter.arc(subject.vertexView, object.vertexView);
		arcView.b.setLabel(arcLabel);
		var arcData = this.bindStatement(statement, arcView.b);
		
		if (placeObject) {
			var layoutManager = new LayoutManager(this);
			layoutManager.placeVertex(object);
		}
		
		this.presenter.router.reroute(subject.vertexView);
		
		this.suppressAddElementEvents = false;
	}
}


/**
 * Bind an RDF statement to an ArcEnd.
 * @param {RdfStatement} statement The RDF statement that is getting bound to an ArcEnd
 * @param {ArcEnd} [arcEnd] The ArcEnd to which the RDF statement is getting bound.
 */
RdfController.prototype.bindStatement = function(statement, arcEnd) {
	
	arcEnd.statement = statement;
	this.arcMap[statement.key()] = arcEnd.arc;
	if (arcEnd.snapText) {
		arcEnd.snapText.attr({resource: statement.predicate.stringValue});
	}
}

RdfController.prototype.undoChangeSet = function(action) {
	
	var graph = action.target;
	var presenter = this.presenter;
	
	// Collect the subjects from the additions
	var subjectMap = {};
	var changeSet = action.object;
	var addition = changeSet.addition;
	if (addition) {

		for (var i=0; i<addition.length; i++) {
			var subject = addition[i].subject;
			subjectMap[subject.stringValue] = subject;
		}
		// Check to see if each subject has any statements left.
		
		for (var s in subjectMap) {
			var vertex = graph.vertex(s, true);
			if (!vertex) {
				var vertexData = this.getVertexData(s);
				if (vertexData) {
					this.unbindVertex(vertexData);
					presenter.notifyHandlers(new RdfVertexDelete(s));
				}
			}
		}
	}
}

RdfController.prototype.redoChangeSet = function(action) {
	console.log("Redo ChangeSet");
}

RdfController.prototype.createVertex = function(vertexView) {
	
	var type = vertexView.getRdfType();
	if (type && type.indexOf(KE.ENTITY.stringValue) >= 0) {
		var conceptIRI = vertexView.getResource();
		// TODO: Get user's language preference.
		var label = vertexView.getLabelValue();
		var subject = this.createConcept(KE.ENTITY, conceptIRI, label, "en");
		this.bind(subject, vertexView);
	}
	
}

RdfController.prototype.addElement = function(event) {

	if (!this.suppressAddElementEvents) {
		var e = event.element;
		if (konig.isVertexView(e)) {
			
		} else if (konig.isArcView(e)) {
			var arc = e;
			if (arc.a.snapText && !arc.a.statement) {
				var predicateValue = arc.a.snapText.attr('resource');
				if (predicateValue) {
					
					var subject = this.getOrCreateRdfVertexFromVertexView(arc.b.vertex);
					var predicate = this.graph.iri(predicateValue);
					this.graph.statement(predicate, RDF.TYPE, RDF.PROPERTY);
					// For now, we assume that all object values are resources.
					// TODO: Handle Literal object values
					var object = this.getOrCreateRdfVertexFromVertexView(arc.a.vertex);
					var statement = this.graph.statement(subject, predicate, object);
					this.bindStatement(statement, arc.a);
					
				} else {
					// TODO: create predicate
					console.log("TODO: create predicate");
				}
			}
			if (arc.b.snapText && !arc.b.statement) {
				var predicateValue = arc.b.snapText.attr('resource');
				if (predicateValue) {
					
					var subject = this.getOrCreateRdfVertexFromVertexView(arc.a.vertex);
					var predicate = this.graph.iri(predicateValue);
					this.graph.statement(predicate, RDF.TYPE, RDF.PROPERTY);
					// For now, we assume that all object values are resources.
					// TODO: Handle Literal object values
					var object = this.getOrCreateRdfVertexFromVertexView(arc.b.vertex);
					if (subject && predicate && object) {
						var statement = this.graph.statement(subject, predicate, object);
						this.bindStatement(statement, arc.b);
					} else {
						throw Error("Invalid statement");
					}
					
				} else {
					// TODO: create predicate
					console.log("TODO: create predicate");
				}
			}
			
		}
	}
}

RdfController.prototype.getOrCreateRdfVertexFromVertexView = function(vertexView) {
	var rdfVertex = this.getRdfVertexFromView(vertexView);
	if (!rdfVertex) {
		this.addElement({element: vertexView});
		rdfVertex = this.getRdfVertexFromView(vertexView);
	}
	return rdfVertex;
}

RdfController.prototype.deleteElement = function(event) {
	var e = event.element;
	if (konig.isVertexView(e)) {
		this.unbindVertex(e.rdfControllerData);
	} else if (konig.isArcView(e)) {
		this.unbindArc(e);
	} else if (e.length) {
		for (var i=0; i<e.length; i++) {
			var v = e[i];
			if (konig.isVertexView(v)) {
				this.unbindVertex(v.rdfControllerData);
			} else if (konig.isArcView(v)) {
				this.unbindArc(v);
			}
		}
	}
}

RdfController.prototype.createConcept = function(type, conceptIRI, label, language) {
	
	var iri = conceptIRI || this.namer.createIRI();
	
	
	
	var subject = this.graph.iri(iri);
	
	var literal = language ? 
		this.graph.langString(label, language) : 
		this.graph.literal(label);
		
		
	
	var s1 = this.graph.statement(subject, RDF.TYPE, type);
	var s2 = this.graph.statement(subject, RDFS.LABEL, literal);
	
	var changeSet = new ChangeSet();
	changeSet.addStatement(s1);
	changeSet.addStatement(s2);
	var activity = new ChangeSetAction(changeSet, this.graph, this);
	
	this.presenter.actionHistory.append(activity);
	
	if (conceptIRI) {
		konig.workspaceManager.getActiveWorkspace().loadEntity(subject);
	}
	
	return subject;
}

RdfController.prototype.vertexViewById = function(idValue) {
	var v = this.vertexMap[idValue];
	return v ? v.vertexView : null;
}

RdfController.prototype.arcViewById = function(idValue) {
	return this.arcMap[idValue];
}



RdfController.prototype.vertexView = function(x, y, rdfNode, statement) {
	var data = this.vertexData(x, y, rdfNode, statement);
	return data.vertexView;
}

RdfController.prototype.key = function(rdfNode, statement) {
	if (rdfNode instanceof Literal) {
		return statement.key();
	}
	return rdfNode.key();
}

RdfController.prototype.getRdfVertexFromView = function(vertexView) {
	var data = vertexView.rdfControllerData;
	return data ? data.rdfVertex : null;
}

RdfController.prototype.getVertexDataFromView = function(vertexView) {
	return vertexView.rdfControllerData;
}

RdfController.prototype.getVertexData = function(rdfNodeOrIRI, statement) {
	if (typeof(rdfNodeOrIRI) === 'string') {
		return this.vertexMap[rdfNodeOrIRI];
	}
	if (rdfNodeOrIRI instanceof RdfResource) {
		return this.vertexMap[rdfNodeOrIRI.key()];
	}
	
	return statement ? this.vertexMap[statement.key()] : null;
}

/**
 * Bind an RDF node to a VertexView
 * @param {RdfVertex|RdfNode|string} element The RDF node that is getting bound. If the
 *    supplied value is a string, it must be the string value of an IRI.
 * @param {VertexView} vertexView The VertexView to which the Vertex will be bound.
 * @returns {VertexData} The VertexData that describes the binding.
 */
RdfController.prototype.bind = function(element, vertexView) {
	
	var rdfVertex = null;
	if (element instanceof RdfVertex) {
		rdfVertex = element;
	} else if (element instanceof IRI) {
		rdfVertex = this.graph.vertex(element);
	} else if (typeof(element) === 'string') {
		var node = this.graph.iri(element);
		rdfVertex = this.graph.vertex(node);
	} else {
		// TODO: Implement special handling if element is a Literal
		throw Error("Invalid argument");
	}
	
	var vertexData = this.getVertexData(rdfVertex.id);
	if (!vertexData) {
		vertexData = new VertexData(rdfVertex, vertexView);

		vertexView.rdfControllerData = vertexData;
		vertexView.attr({resource: rdfVertex.id.stringValue});
		
		this.vertexMap[rdfVertex.id.key()] = vertexData;
		
		var event = new RdfVertexBindEvent(vertexData);
		this.presenter.notifyHandlers(event);
	}
	return vertexData;
}


RdfController.prototype.unbindArc = function(arcView) {
	this.unbindArcEnd(arcView.a);
	this.unbindArcEnd(arcView.b);
}

RdfController.prototype.unbindArcEnd = function(arcEnd) {
	var statement = arcEnd.statement;
	if (statement) {
		delete arcEnd.statement;
		var key = statement.key();
		delete this.arcMap[key];
		this.presenter.notifyHandlers(new RdfStatementUnbindEvent(statement, arcEnd));
	}
}

RdfController.prototype.unbindVertex = function(vertexData) {
	
	var arcList = vertexData.vertexView.removedArcList;
	for (var i=0; i<arcList.length; i++) {
		var arcView = arcList[i];
		this.unbindArc(arcView);
	}
	var key = vertexData.rdfVertex.id.key();
	delete this.vertexMap[key];
	this.presenter.notifyHandlers(new RdfVertexUnbindEvent(vertexData));
}


RdfController.prototype.vertexData = function(x, y, rdfNode, statement) {
	
	if (rdfNode instanceof Literal) {
		var data = this.vertexMap[statement.key()];
		if (data) {
			return data;
		}
		rdfNode.objectOf = statement;
		var label = this.namer.nodeLabel(rdfNode);
		var vertexView = this.presenter.vertex(x, y, label);
		var data = new VertexData(null, vertexView, statement);
		vertexView.rdfControllerData = data;
		this.vertexMap[statement.key()] = data;
		return data;
		
	}
	
	var data = this.vertexMap[rdfNode.stringValue];
	if (data) {
		return data;
	}
	
	var rdfVertex = this.graph.vertex(rdfNode);
	var label = this.namer.nodeLabel(rdfVertex);
	var vertexView = this.presenter.vertex(x, y, label);
	
	data = this.bind(rdfVertex, vertexView);
	
	return data;
}

RdfController.prototype.resourceLabel = function(rdfVertex) {
	var label = null;
	var labelLiteral = rdfVertex.propertyValue(RDFS.LABEL);
	if (labelLiteral) {
		label = labelLiteral.stringValue;
	} else {
		
		
		label = (rdfVertex.id instanceof BNode) ? 
				'Object:' + rdfVertex.id.localName :
				rdfVertex.id.localName;
	}
	
	return label;
	
}

RdfController.prototype.showResource = function(resourceIRI) {
	
	var rdfNode = this.graph.resource(resourceIRI);
	
	
	
	var subject = this.getVertexData(rdfNode);
	
	if (subject && subject.loaded) {

		
		var action = this.presenter.centerViewBoxAt(subject.vertexView);
		var animator = new this.presenter.SproutAnimator(subject.vertexView, action);
		animator.animate();
		
		
		return null;
	}
	
	var history = this.presenter.actionHistory;
	var txn = history.beginTransaction();
	try {

		var layoutManager = new LayoutManager(this);
		if (!subject) {
			var viewBox = this.presenter.viewBox().getBox();
			var x = viewBox.cx;
			var y = viewBox.cy;
			subject = this.vertexData(x, y, rdfNode);
			layoutManager.placeOrigin(subject, viewBox);
		} else {
			layoutManager.setOrigin(subject);		
		}

		layoutManager.layout();
		
		subject.loaded = true;

		this.presenter.centerViewBoxAt(subject.vertexView);
		var composite = history.commit(txn);
		var animator = new this.presenter.SproutAnimator(subject.vertexView, composite);
		animator.animate();
		
	} catch (error) {
		// TODO: report error
		console.log(error);
		history.abort(txn);
		subject = null;
	}

	return subject;
}

RdfController.prototype.analyzeNewNodes = function(subjectView, list, pad) {
	for (var i=0; i<list.length; i++) {
		var ringNode = list[i];
		
		var objectView = ringNode.vertexData.vertexView;
		var bbox = ringNode.vertexData.vertexView.getBBox();
		ringNode.vertexWidth = bbox.width;
		
		var maxOut=0;
		var maxIn=0;
		var arcList = objectView.arcList;
		for (var j=0; j<arcList.length; j++) {
			var arc = arcList[j];
			var a = arc.a;
			var b = arc.b;
			if (b.vertex == subjectView) {
				var c = a;
				a = b;
				b = c;
			}
			if (a.textLength > maxOut) {
				maxOut = a.textLength;
			}
			if (b.textLength > maxIn) {
				maxIn = b.textLength;
			}
			var len = 2*pad + maxIn;
			if (maxOut>0) {
				len = len + pad + maxOut;
			}
			ringNode.arcTextLength = len;
		}
		
	}
}


RDFSelectHandler = function(controller) {
	this.controller = controller;
}

RDFSelectHandler.prototype.select = function(event) {
	var list = event.elementList;
	if (list.length === 1) {
		var e = list[0];
		if (konig.isVertexView(e)) {

			var iriValue = e.attr('resource');
			
			if (iriValue) {
				this.controller.showResource(iriValue);
			}
		}
	}
}


konig.rdfController = new RdfController(konig.presenter);
if (konig.test) {

	konig.test.pointOnRayAtDistanceFromReferencePoint = pointOnRayAtDistanceFromReferencePoint;

}

});
