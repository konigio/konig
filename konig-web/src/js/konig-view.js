$(function() {

var HistoryManager = konig.HistoryManager; 
var CompositeAction = konig.CompositeAction;
	
MOVE = 1;
CONNECT = 2;
	
var geom = new Object();


BOTTOM = geom.BOTTOM = 0;
LEFT = geom.LEFT = 1;
TOP = geom.TOP = 2;
RIGHT = geom.RIGHT = 3;

function normalVector(side, x, y, arrowHeadDx, arrowHeadDy) {
	var point = new Point(x, y);
	point.side = side;
	point.rotation = ((side+3)%4) * 90;
	point.arrowHeadDx = arrowHeadDx;
	point.arrowHeadDy = arrowHeadDy;
	return point;
}

normalBottom = normalVector(BOTTOM,  0,  1, -1, 0);
normalLeft =   normalVector(LEFT,   -1,  0, -1, 0);
normalTop =    normalVector(TOP,     0, -1, -1, 0);
normalRight =  normalVector(RIGHT,   1,  0, -1, 0);

NORMAL = geom.NORMAL = [normalBottom, normalLeft, normalTop, normalRight];


sideName = ["BOTTOM", "LEFT", "TOP", "RIGHT"];

Math.radians = function(degrees) {
  return degrees * Math.PI / 180;
};
 
// Converts from radians to degrees.
Math.degrees = function(radians) {
  return radians * 180 / Math.PI;
};

var bboxToViewBoxState = function(bbox) {
	return '' + bbox.x + ' ' + bbox.y + ' ' + bbox.width + ' ' + bbox.height;
}

if (!String.prototype.trim) {
    (function() {
        // Make sure we trim BOM and NBSP
        var rtrim = /^[\s\uFEFF\xA0]+|[\s\uFEFF\xA0]+$/g;
        String.prototype.trim = function() {
            return this.replace(rtrim, '');
        };
    })();
}

arcDoubleClick = function(event) {
	if (this.konigArc) {
		event.stopPropagation();
		this.konigArc.ondblclick(event);
	}
}
	
vertexOnstart = function(x, y, event) {
	// TODO: If this vertex does not have a konigVertex
	//       and it has RDFa markup, then try to wire
	//       it to a VertexView.
	
	this.konigVertex.onstart(x, y, event);
}

vertexOnmove = function(dx, dy, x, y, event) {
	this.konigVertex.onmove(dx, dy, x, y, event);
}

vertexOnend = function(event) {
	this.konigVertex.onend(event);
}




ObliqueArcRenderer = function() {}

ObliqueArcRenderer.prototype.toSvgPath = function(arc) {
	return "M " + this.a.x + ' ' + this.a.y + ' L ' + this.b.x + ' ' + this.b.y;
}


ObliqueArcRenderer.prototype.draw = function(arc) {
	
	var path = this.toSvgPath(arc);
	if (path) {
		arc.path.attr({d: path});
		AbstractArcRenderer.prototype.draw.call(this, arc);
	}
}

ViewBox = function(svgNode) {
	this.svgNode = svgNode;	
}

ViewBox.prototype.getBox = function() {
	var rect = new Box();
	var value = this.svgNode.getAttribute('viewBox');
	if (!value) {
		rect.width = parseFloat(this.svgNode.getAttribute('width'));
		rect.height = parseFloat(this.svgNode.getAttribute('height'));
		rect.x = 0;
		rect.y = 0;
		
	} else {
		var array = value.split(/\s/);
		rect.x = parseFloat(array[0]);
		rect.y = parseFloat(array[1]);
		rect.width = parseFloat(array[2]);
		rect.height = parseFloat(array[3]);
	}
	rect.update();
	
	return rect;
}

ViewBox.prototype.setBox = function(box) {
	var value = box.x + ' ' + box.y + ' ' + box.width + ' ' + box.height;
	this.svgNode.setAttribute('viewBox', value);
}

ViewBox.prototype.translate = function(dx, dy) {
	var rect = this.getBox();
	rect.left += dx;
	rect.top += dy;
	
	var value = rect.left + ' ' + rect.top + ' ' + rect.width + ' ' + rect.height;
	this.svgNode.setAttribute('viewBox', value);
}

AbstractArcRenderer = function() {
	
}

AbstractArcRenderer.prototype.draw = function(arc) {
	this.alignArrowHead(arc.a);
	this.alignArrowHead(arc.b);
}


AbstractArcRenderer.prototype.alignArrowHead = function(arcEnd) {
	
	if (arcEnd.arrowHead) {
		var p = arcEnd.point;
		var n = arcEnd.normal;
		if (!p || !n || !p.x || !p.y) {
			return;
		}
		var presenter = arcEnd.arc.a.vertex.presenter;
		var length = presenter.arrowHeadLength;
		
		
		var px = p.x;
		var py = p.y;
		
		var dx = length * n.arrowHeadDx;
		var dy = length * n.arrowHeadDy;
		
		var transformValue = 
				"rotate(" + n.rotation + ',' + px + ',' + py + ') ' +
				'translate(' + dx + ',' + dy + ')';
		
		
		arcEnd.arrowHead.attr({
			x: px,
			y: py,
			transform: transformValue
		});
		
	}


}


SmartArcRenderer = function() {
	this.rectilinear = new RectilinearArcRenderer();
	this.curved = new CurvedArcRenderer();
}

SmartArcRenderer.prototype = Object.create(AbstractArcRenderer.prototype);

SmartArcRenderer.prototype.draw = function(arc) {
	if (arc.pointList) {
		this.rectilinear.draw(arc);
	} else {
		this.curved.draw(arc);
	}
	
}

RectilinearArcRenderer = function() {
}
RectilinearArcRenderer.prototype = Object.create(AbstractArcRenderer.prototype);

RectilinearArcRenderer.prototype.draw = function(arc) {
	
	var pointList = arc.pointList;
	if (!pointList) {
		pointList = [arc.a.point, arc.b.point];
	}
	
	var pathString = Snap.format(
		"M{x},{y}",
		{x:pointList[0].x, y:pointList[0].y}
	);
	
	for (var i=1; i<pointList.length; i++) {
		var text = Snap.format(
			" L {x},{y}",
			{x:pointList[i].x, y:pointList[i].y}
		);
		pathString = pathString + text;
	}
	
	arc.path.attr({d: pathString});
	
}


CurvedArcRenderer = function() {}
CurvedArcRenderer.prototype = Object.create(AbstractArcRenderer.prototype);

CurvedArcRenderer.prototype.toSvgPath = function(arc) {
	var a = arc.a;
	var b = arc.b;
	
//	if (a.point.x===b.point.x && a.point.y===b.point.y) {
//		var x = a.point.x;
//		var y = a.point.y;
//		
//		return "M";
//	}
	
	if (b.point.x < a.point.x) {
		var c = a;
		a = b;
		b = c;
	}
	
	var x0 = a.point.x;
	var y0 = a.point.y;
	var n0 = a.normal;
	
	var arrowHeadLength = arc.a.vertex.presenter.arrowHeadLength;
	if (a.arrowHead) {
		x0 += arrowHeadLength*n0.x;
		y0 += arrowHeadLength*n0.y;
		
	}
	
	var x1 = b.point.x;
	var y1 = b.point.y;
	var n1 = b.normal;

	if (b.arrowHead) {
		x1 += arrowHeadLength*n1.x;
		y1 += arrowHeadLength*n1.y;
	}
	
	var sx;
	var sy;
	
	
	if (a.vertex === b.vertex) {
		var bbox = a.vertex.getBBox();
		
		if (n0 == n1) {
			// ends are on the same edge of the vertex rectangle.
			// Use a quadratic bezier
			
			var control = a.point.average(b.point);
			control.add(n0, 30);
			
			return 'M ' + x0 + ' ' + y0 +
				' Q ' + control.x + ' ' + control.y + ', ' +
				x1 + ' ' + y1;
			
		} 
		
		sx = sy = bbox.height + bbox.width;
	} else {
		sx = Math.abs(x1-x0)/2.0;
		sy = Math.abs(y1-y0)/2.0;
	}
	
	
	var c0x = x0 + (n0 ? sx*n0.x : 0);
	var c0y = y0 + (n0 ? sy*n0.y : 0);
	
	var c1x = x1 + (n1 ? sx*n1.x : 0);
	var c1y = y1 + (n1 ? sy*n1.y : 0);
	
	
	
	var result = 'M ' + x0 + ' ' + y0 + 
		' C ' + c0x + ' ' + c0y + ', ' + c1x + ' ' + c1y + ', ' +
		x1 + ' ' + y1;
	
	if (result.indexOf('NaN')>=0) {
		console.log("invalid path: " + arc.toString());
		result = null;
	}
	return result;
}


CurvedArcRenderer.prototype.draw = ObliqueArcRenderer.prototype.draw;

function Point(x, y) {
	this.x = x;
	this.y = y;
}

Point.prototype.copy = function(other) {
	this.x = other.x;
	this.y = other.y;
}

Point.prototype.minus = function(other) {
	this.x -= other.x;
	this.y -= other.y;
	return this;
}

Point.prototype.dot = function(other) {
	return this.x*other.x + this.y*other.y;
}

Point.prototype.clone = function() {
	var other = new Point(this.x, this.y);
	other.side = this.side;
	return other;
}

Point.prototype.theta = function() {
	var dx;
	var dy;
	if (arguments.length==1) {
		var p = arguments[0];
		dx = p.x - this.x;
		dy = p.y - this.y;
	} else {
		dx = arguments[0] - this.x;
		dy = arguments[1] - this.y;
	}
	
	return Math.atan2(dy, dx);
}

Point.prototype.add = function(other, scale) {
	if (!scale) {
		scale = 1;
	}
	this.x += scale*other.x;
	this.y += scale*other.y;
	
	return this;
}

Point.prototype.normalize = function() {
	return this.scale(1.0/this.distanceTo(0,0));
}

Point.prototype.length = function() {
	return Math.sqrt(this.lengthSquared());
}

Point.prototype.lengthSquared = function() {
	return this.x*this.x + this.y*this.y;
}

Point.prototype.scale = function(scale) {
	this.x = this.x*scale;
	this.y = this.y*scale;
	return this;
}

Point.prototype.average = function(p) {
	var x = 0.5*(this.x + p.x);
	var y = 0.5*(this.y + p.y);
	return new Point(x, y);
}

Point.prototype.moveTo = function(x, y) {
	this.x = x;
	this.y = y;
}

Point.prototype.translate = function(dx, dy) {
	this.x += dx;
	this.y += dy;
}

Point.prototype.positiveTheta = function() {
	var theta = this.theta.apply(this, arguments);
	if (theta < 0) {
		theta = 2*Math.PI + theta;
	}
	return theta;
}

Point.prototype.distanceTo = function() {
	var square = this.squaredDistanceTo.apply(this, arguments);
	return Math.sqrt(square);
} 

Point.prototype.squaredDistanceTo = function() {
	var x;
	var y;
	if (arguments.length==1) {
		var point = arguments[0];
		x = point.x;
		y = point.y;
	} else {
		x = arguments[0];
		y = arguments[1];
	}
	
	var dx = x - this.x;
	var dy = y - this.y;
	
	return dx*dx + dy*dy;
	
}

Point.prototype.toString = function() {
	return '(' + this.x  + ', ' + this.y + ')';
}

Box = function(x, y, width, height) {
	this.x = x || 0;
	this.y = y || 0;
	this.width = width || 0;
	this.height = height || 0;
	if (x && y && width && height) {
		this.update();
	}
}

Box.prototype.toViewBoxState = function() {
	return bboxToViewBoxState(this);
}

Box.prototype.copy = function(other) {
	this.x = other.x;
	this.y = other.y;
	this.width = other.width;
	this.height = other.height;
	this.update();
}

Box.prototype.moveCenterTo = function(x,y) {
	this.cx = x;
	this.cy = y;
	this.x = this.left = x - 0.5*this.width;
	this.y = this.top = y - 0.5*this.height;
	this.x2 = this.right = x + 0.5*this.width;
	this.y2 = this.bottom = y + 0.5*this.height;
}

Box.prototype.moveTo = function(left, top) {
	this.x = this.left = left;
	this.y = this.right = top;
	this.update();
}

Box.prototype.translate = function(dx, dy) {
	this.x += dx;
	this.y += dy;
	this.update();
}


Box.prototype.update = function() {
	this.left = this.x;
	this.top = this.y;
	this.cx = this.x + 0.5*this.width;
	this.cy = this.y + 0.5*this.height;
	this.x2 = this.right = this.x + this.width;
	this.y2 = this.bottom = this.y + this.height;
}

function Rectangle(bbox) {
	this.side = [];
	this.cx=0;
	this.cy=0;
	if (bbox) {
		this.fromBBox(bbox);
	}
}

Rectangle.prototype.pointClosestTo = function() {
	var x,y;
	if (arguments.length===1) {
		var p = arguments[0];
		if (!p) {
			throw "Illegal Argument";
		}
		x = p.x;
		y = p.y;
	} else {
		x = arguments[0];
		y = arguments[1];
	}
	
	var p = null;
	var best = Number.MAX_VALUE;
	
	for (var i=0; i<4; i++) {
		var q = this.side[i].pointClosestTo(x, y);
		var dist = q.distanceTo(x,y);
		if (dist < best) {
			best = dist;
			p = q;
			p.side = i;
		}
	}
	return p;
}


Rectangle.prototype.toBBox = function() {
	this.x = this.side[LEFT].x1;
	this.y = this.side[TOP].y1;
	this.x2 = this.side[RIGHT].x1;
	this.y2 = this.side[BOTTOM].y1;
	this.cx = 0.5*(this.x + this.x2);
	this.cy = 0.5*(this.y + this.y2);
	this.height = Math.abs(this.y2 - this.y);
	this.width = Math.abs(this.x2-this.x);
	
	return this;
}


Rectangle.prototype.fromBBox = function(bbox) {
	this.side[LEFT] = new Segment(bbox.x, bbox.y2, bbox.x, bbox.y);
	this.side[TOP] = new Segment(bbox.x, bbox.y, bbox.x2, bbox.y);
	this.side[RIGHT] = new Segment(bbox.x2, bbox.y, bbox.x2, bbox.y2);
	this.side[BOTTOM] = new Segment(bbox.x2, bbox.y2, bbox.x, bbox.y2);
	this.toBBox();
	return this;
}

function Segment(x1, y1, x2, y2) {
	this.x1=x1;
	this.y1=y1;
	this.x2=x2;
	this.y2=y2;
}

Segment.prototype.split = function(n) {
	var array = [];
	var m = n+1;
	var dx = (this.x2 - this.x1)/m;
	var dy = (this.y2 - this.y1)/m;
	var x = this.x1;
	var y = this.y1;
	
	for (var i=0; i<n; i++) {
		x += dx;
		y += dy;
		array.push(new Point(x, y));
	}
	
	return array;
}

Segment.prototype.length = function() {
	if (!this.length) {
		var dx = x2-x1;
		var dy = y2-y1;
		this.length = Math.sqrt(dx*dx + dy*dy);
	}
	return this.length;
}


function distance(vx, vy, wx, wy) {
	return Math.sqrt(dist2(vx, vy, wx, wy));
}

function dist2(vx, vy, wx, wy) {
	var dx = wx-vx;
	var dy = wy-vy;
	return dx*dx + dy*dy;
}

/**
 * Returns the point on the segment (vx,vy)-(wx,wy)
 * closest to (px,py).
 */
function projectPointOntoSegment(px, py, vx, vy, wx, wy) {
  var l2 = dist2(vx, vy, wx, wy);
  if (l2 === 0) return new Point(vx, vy);
  
  var t = ((px - vx) * (wx - vx) + (py - vy) * (wy - vy)) / l2;
  if (t < 0) return new Point(vx, vy);
  if (t > 1) return new Point(wx, wy);
  return new Point( vx + t * (wx - vx),
                    vy + t * (wy - vy));
}

Segment.prototype.pointClosestTo = function(x, y) {
	
	return projectPointOntoSegment(x, y, this.x1, this.y1, this.x2, this.y2);
}

function ArcEnd(vertex, point, normal) {
	this.id = null;
	this.arc = null;
	this.vertex = vertex;
	this.point = point;
	this.normal = normal;
	this.textLength = 0;
	this.snapText = null;
	this.snapTextValue = null;
	this.arrowHead = null;
}

/**
 * Compute the fully-qualified IRI values for the type value of this
 * ArcEnd, as given by the RDFa "typeof" attribute on the SVG element.
 * @returns {Array.<string>} Array of IRI values for the type of this vertex.
 */
ArcEnd.prototype.getRdfType = function() {
	return this.snapText ? rdf.rdfaType(this.snapText.node) : [];
}

ArcEnd.prototype.reconstructText = function() {
	var selfId = this.getId();
	var node = document.getElementById(selfId);
	if (node) {
		var presenter = this.vertex.presenter;
		var paper = presenter.paper;
		
		this.snapText = Snap(node);
		this.snapTextBg = Snap('#' + selfId + "-bg" );
		var textPath = $(node).find("textPath");
		this.snapTextValue = textPath.text();

		var tmp = paper.text(0, 0, this.snapTextValue).attr(presenter.arcLabelConfig);
		var bbox = tmp.getBBox();
		tmp.remove();
		this.textLength = bbox.width;
	}
}

ArcEnd.prototype.getId = function() {
	if (!this.id) {
		this.id = this.arc.id + "-end-" + this.accessor();
	}
	return this.id;
}

ArcEnd.prototype.accessor = function() {
	return this.arc.a === this ? 'a' : 'b';
}

ArcEnd.prototype.serialize = function() {
	var self = {
		"@id" : this.getId(),
		"vertex" : this.vertex.snapElement.attr("id")
	};
	
	if (this.snapTextValue) {
		self.label = this.snapTextValue;
		var rdfType = this.getRdfType();
		if (rdfType.length == 1) {
			self.resource = rdfType[0];
		} else if (rdfType.length > 1) {
			self.resource = rdfType;
		}
	}
	
	
	
	return self;
}

ArcEnd.prototype.createArrowHead = function() {
	var presenter = this.arc.a.vertex.presenter;
	var arrowHeadHref = '#' + presenter.arrowHeadConfig.id;
	
	this.arrowHead = presenter.paper.use();
	this.arrowHead.attr({'xlink:href': arrowHeadHref});
	presenter.paper.append(this.arrowHead);
	
	var u = $(this.arrowHead.node);
	u.append('<desc property="konig:arrowHeadOf">' + this.getId() + "</desc>");

	
	this.arc.renderer.alignArrowHead(this);
}

ArcEnd.prototype.setTextPathOffset = function(domText, value) {
	
	$(domText).find('textPath')[0].setAttribute('startOffset', value);
}

ArcEnd.prototype.inject = function() {
	if (this.snapText) {
		this.vertex.presenter.paper.append(this.snapTextBg);
		this.vertex.presenter.paper.append(this.snapText);
	}
	if (this.arrowHead) {
		this.vertex.presenter.paper.append(this.arrowHead);
	}
}

ArcEnd.prototype.remove = function() {
	if (this.snapText) {
		this.snapText.remove();
		this.snapTextBg.remove();
	}
	if (this.arrowHead) {
		this.arrowHead.remove();
	}
}

ArcEnd.prototype.setLabel = function(value, suppressHistory) {
	if (value === this.snapTextValue) {
		return;
	}
	var path = this.arc.path.node;
	if (value == null) {
		if (this.snapText) {
			this.snapText.remove();
			this.snapTextBg.remove();
			this.snapText = null;
			this.snapTextValue = null;
			
		}
	}
	if (!this.arrowHead) {
		this.createArrowHead();
	}
	
	var presenter = this.arc.a.vertex.presenter;
	if (value != null) {

		var pathId = path.id;
		var endId = this.arc.a == this ? 'a' : 'b';
		var labelId = this.getId();
		var backgroundId = labelId + '-bg';
		
		var snapText = Snap('#' + labelId);
		var textPath = null;
		var paper = presenter.paper;
		if (!snapText) {

			snapTextBg = paper.text(0, 0, value);
			snapTextBg.attr({
				id: backgroundId, 
				textpath: path, 
				stroke: 'white', 
				strokeWidth: 6});
			this.snapTextBg = snapTextBg;
			
			snapText = paper.text(0, 0, value);
			snapText.addClass('konig-arc-label');
			snapText.attr({id: labelId, textpath: path, 
				'z-index' : 10});
			textPath = $(snapText.node).find('textPath');
			
			var snapTextPath = Snap(textPath[0]);
			snapTextPath.konigArcEnd = this;
			
			
		} else {
			textPath = $(snapText.node).find('textPath');
			textPath.text(value);
			$(this.snapTextBg.node).find('textPath').text(value);
			
		}

		var tmp = paper.text(0, 0, value).attr(presenter.arcLabelConfig);
		var bbox = tmp.getBBox();
		tmp.remove();
		
		this.textLength = bbox.width;
		this.snapText = snapText;
		snapText.attr(presenter.arcLabelConfig);
		this.snapTextBg.attr(presenter.arcLabelConfig);

		this.updateStartOffset();
	}
	
	var originalLabel = this.snapTextValue;
	this.snapTextValue = value;
	
	
	if (!suppressHistory) {
		this.vertex.presenter.actionHistory.append(
				new ChangeArcEndLabelAction(this, originalLabel, value));
	}
	if (this.snapText) {

		var self = this;
		this.snapText.dblclick(function(event) {
			event.stopPropagation();
			self.arc.ondblclick(event, self);
		});
	} else {
		console.log("snapText is null");
	}

}


ArcEnd.prototype.updateStartOffset = function() {

	// TODO: delegate this operation to arc.renderer
	if (!this.snapText) return;
	
	var a = this.arc.a;
	var b = this.arc.b;
	
	var offset = this.arc.a.vertex.presenter.arcLabelPaddingLeft;
	if (
		((b == this) && ((a.point.x < b.point.x) || this.arc.pointList)) ||
		((a == this) && (a.point.x > b.point.x) && !this.arc.pointList)
		
	){

		var path = this.arc.path;
		var totalLength = path.getTotalLength();
		offset = path.getTotalLength() - this.textLength - offset;
	} 

	// For some reason, the textPath element does not show up unless
	// we animate it.
	// TODO: find a better workaround, if possible.
	if (this.snapText.textPath) {
		this.snapText.textPath.animate({startOffset: offset}, 1);
	}
	if (this.snapTextBg.textPath) {
		this.snapTextBg.textPath.animate({startOffset: offset}, 1);
	}
	
}

ArcEnd.prototype.alignLabelToPathStart = function() {
if (!this.snapText) return;
	
	var offset = this.arc.a.vertex.presenter.arcLabelPaddingLeft;

	this.snapText.textPath.animate({startOffset: offset}, 1);
	this.snapTextBg.textPath.animate({startOffset: offset}, 1);
}

ArcEnd.prototype.alignLabelToPathEnd = function() {

	var pad = this.arc.a.vertex.presenter.arcLabelPaddingLeft;
	var path = this.arc.path;
	var totalLength = path.getTotalLength();
	var offset = totalLength - this.textLength - pad;
	this.setTextPathOffset(this.snapText.node, offset);
	this.setTextPathOffset(this.snapTextBg.node, offset);
}

ArcEnd.prototype.moveTo = function(x, y) {
	this.point.x = x;
	this.point.y = y;
}

ArcEnd.prototype.increment = function(dx, dy) {
	var p = this.point;
	p.x += dx;
	p.y += dy;
}

function ArcView(id, renderer, a, path, b) {
	this.id = id;
	this.renderer = renderer;
	this.a = a;
	this.b = b;
	a.arc = this;
	b.arc = this;
	this.path = path;

	path.konigArc = this;
	path.addClass('konig-arc');
	if (a.vertex && b.vertex) {
		var p = $(path.node);
		p.append('<desc property="konig:vertexA">' + a.vertex.id + "</desc>");
		p.append('<desc property="konig:vertexB">' + b.vertex.id + "</desc>");
	}
}

ArcView.prototype.toDetailString = function() {

	return 'ArcView(' + this.a.vertex.getLabelValue() + ', ' + this.b.vertex.getLabelValue() + ') = ' +
		this.a.point.toString() + '[' + this.a.normal.x + ', ' + this.a.normal.y + ']-' + 
		this.b.point.toString() + '[' + this.b.normal.x + ', ' + this.b.normal.y + ']';
}

ArcView.prototype.serialize = function() {
	var json = {
		"@type" : "ArcView",
		"@id" : this.path.attr('id'),
		"a" : this.a.serialize(),
		"b" : this.b.serialize()
	};

	this.a.vertex.presenter.marshalArc(this, json);
	return json;
}

ArcView.prototype.marshal = function(doc) {
	var array = doc.actions;
	var json = {
		"@type" : "AddElement",
		"element" : {
			"@type" : "ArcView",
			"@id" : this.path.attr('id'),
			"a" : this.a.serialize(),
			"b" : this.b.serialize()
		}
	};
	
	this.a.vertex.presenter.marshalArc(this, json.element);

	array.push(json);
}

ArcView.prototype.animate = function(attr) {
	this.path.animate(attr);
	return this;
}

ArcView.prototype.toString = function() {
	return 'ArcView(' + this.a.vertex.getLabelValue() + ', ' + this.b.vertex.getLabelValue() + ')';
}

ArcView.prototype.inject = function() {
	this.a.vertex.presenter.paper.append(this.path);
	this.a.vertex.arcList.push(this);
	if (this.b != this.a) {
		this.b.vertex.arcList.push(this);
	}
	this.a.inject();
	this.b.inject();
}

ArcView.prototype.remove = function() {
	this.a.vertex.removeArc(this);
	this.b.vertex.removeArc(this);
	
	this.b.remove();
	this.a.remove();
	this.path.remove();
}

ArcView.prototype.ondblclick = function(event, end) {

	event.stopPropagation();
	
	var presenter = this.a.vertex.presenter;
	var p = presenter.mouseToSvg(event.clientX, event.clientY);
	
	var x = p.x;
	var y = p.y;
	
	var aPoint = this.a.point;
	var bPoint = this.b.point;
	
	if (!end) {
		var aDist = aPoint.squaredDistanceTo(x, y);
		var bDist = bPoint.squaredDistanceTo(x, y);
		
		end = (aDist<bDist) ? this.a : this.b;
	}
	
	var vertex = end.vertex;
	
	var originalText = end.snapTextValue || "";
	
	var overlay = $('<div class="ui-widget-overlay" style="z-index: 100"></div>');
	
	var inputFieldHTML = Snap.format(
		"<input id='konig-dialog-input' " +
			"style='position: absolute; left: {left}px; top: {top}px; z-index: 200' " +
			"value='{arcLabel}'></input>", 
		{
			arcLabel : originalText,
			left: event.clientX,
			top: event.clientY
		}
	);
	
	if (!originalText) {
		originalText = null;
	}
	
	var inputField = $(inputFieldHTML);
	var self = this;
	inputField.keypress(function(event) {
		var keycode = (event.keyCode ? event.keyCode : event.which);
		
		if (keycode == '13') {
			var actionHistory = end.vertex.presenter.actionHistory;
			var txn = actionHistory.beginTransaction();
			
			var finalText = $(this).val();
			end.setLabel(finalText, true);
			
			var list = presenter.elementDecoratorList;
			if (list) {
				for (var i=0; i<list.length; i++) {
					var decorator = list[i];
					if (decorator.changeArcEndLabel) {
						decorator.changeArcEndLabel(end);
					}
				}
			}
			
			actionHistory.append(
					new ChangeArcEndLabelAction(end, originalText, finalText))
			overlay.remove();
			inputField.remove();
			if (self.isSelfArc()) {
				self.a.vertex.pack();
			}
			actionHistory.commit(txn);
		}
	});
	
	overlay.click(function() {
		overlay.remove();
		inputField.remove();
	});
	
	$('body').append(overlay).append(inputField);
	inputField.focus();
	
}


ArcView.prototype.selfEnd = function(vertex) {
	if (this.a.vertex === vertex) {
		return this.a;
	}
	return this.b;
}

ArcView.prototype.otherEnd = function(vertexOrEnd) {
	
	if (vertexOrEnd===this.a) {
		return this.b;
	}
	if (vertexOrEnd===this.b) {
		return this.a;
	}
	if (this.a.vertex == this.b.vertex) {
		return null;
	}
	if (this.a.vertex === vertexOrEnd) {
		return this.b;
	}
	return this.a;
}

ArcView.prototype.select = function() {
	this.path.addClass('konig-selected-arc');
	if (this.a.arrowHead) {
		this.a.arrowHead.addClass('konig-selected-arrowhead');
	}
	if (this.b.arrowHead) {
		this.b.arrowHead.addClass('konig-selected-arrowhead');
	}
}


ArcView.prototype.unselect = function() {

	this.path.removeClass('konig-selected-arc');
	if (this.a.arrowHead) {
		this.a.arrowHead.removeClass('konig-selected-arrowhead');
	}
	if (this.b.arrowHead) {
		this.b.arrowHead.removeClass('konig-selected-arrowhead');
	}
}

ArcView.prototype.isSelfArc = function() {
	return this.a.vertex === this.b.vertex;
}

ArcView.prototype.draw = function() {
	this.renderer.draw(this);
	this.a.updateStartOffset();
	this.b.updateStartOffset();
}

function VertexView(presenter, group, background, snapText, hotspot) {
	this.presenter = presenter;
	this.arcList = [];
	this.snapElement = group;
	this.snapText = snapText || group.select('.konig-vertex-label');
	this.background = background || group.select('.konig-vertex-rect');
	this.hotspot = hotspot || group.select('.konig-vertex-hotspot');
	var id = group.attr('id');
	if (!id) {
		this.id = "vertex-" + presenter.nextId();
		group.attr({id: this.id});
	} else {
		this.id = id;
	}
	group.konigVertex = this;
	group.drag(vertexOnmove, vertexOnstart, vertexOnend);
	
    var self = this;
	this.snapText.dblclick(function(event) {
		self.ondblclick(event);
	});
	
	this.background.dblclick(function(event){
		self.ondblclick(event);
	});
	
}

VertexView.prototype.setResource = function(iri) {
	this.snapElement.attr({resource: iri});
}

VertexView.prototype.getResource = function() {
	return rdf.rdfaResource(this.snapElement.node);
}

VertexView.prototype.setRdfType = function(rdfType) {
	this.snapElement.attr({"typeof": rdfType});
}

/**
 * Compute the fully-qualified IRI values for the type value of this
 * vertex, as given by the RDFa "typeof" attribute on the SVG element.
 * @returns {Array.<string>} Array of IRI values for the type of this vertex.
 */
VertexView.prototype.getRdfType = function() {
	return rdf.rdfaType(this.snapElement.node);
}


VertexView.prototype.toString = function() {
	return 'Vertex(' + this.getLabelValue() + ')';
}

VertexView.prototype.attr = function(value) {
	return this.snapElement.attr(value);
	
}

VertexView.prototype.animate = function(attrs, duration, easing, callback) {
	this.snapElement.animate(attrs, duration, easing, callback);
	return this;
}

VertexView.prototype.sideCount = function() {
	var array = [0,0,0,0];
	for (var i=0; i<this.arcList.length; i++) {
		var arc = this.arcList[i];
		if (arc.a.vertex == this) {
			var side = arc.a.normal.side;
			array[side] = array[side]+1;
		}
		if (arc.b.vertex == this) {
			var side = arc.b.normal.side;
			array[side] = array[side] + 1;
		}
	}
	return array;
	
}
VertexView.prototype.serialize = function() {
	var bbox = this.snapElement.getBBox();
	var json = {
		"@type" : "VertexView",
		"@id" : this.snapElement.attr('id'),
		"x" : bbox.x,
		"y" : bbox.y,
		"label" : this.getLabelValue()
	};
	
	var rdfType = this.getRdfType();
	if (rdfType.length == 1) {
		json["typeof"] = rdfType[0];
		
	} else if (rdfType.length > 1) {
		json["typeof"] = rdfType;
	}
	var resource = this.getResource();
	if (resource) {
		json.resource = resource;
	}

	return json;
	
}
VertexView.prototype.marshal = function(doc) {
	var array = doc.actions;
	var bbox = this.snapElement.getBBox();
	var json = {
		"@type" : "AddElement",
		"element" : {
			"@type" : "VertexView",
			"@id" : this.snapElement.attr('id'),
			"x" : bbox.x,
			"y" : bbox.y,
			"label" : this.getLabelValue()
		}
	};
	this.presenter.marshalVertex(this, json.element);
	array.push(json);
}

VertexView.prototype.setLabel = function(value, suppressHistory) {
	var originalText = this.getLabelValue();
	var snapText = $(this.getLabel().node).text(value);
	this.pack();
	
	if (!suppressHistory) {
		this.presenter.actionHistory.append(
			new SetVertexLabelAction(this, originalText, value));
	}
}

VertexView.prototype.pack = function() {
	var padding = this.presenter.padding;
	var labelBBox = this.getLabel().getBBox();
	var rect = this.getRect();
	
	var bgHeight = labelBBox.height + 2*padding;
	var bgWidth = labelBBox.width + 2*padding;
	
	var sideCount = this.sideCount();
	var heightCount = Math.max(sideCount[LEFT], sideCount[RIGHT]);
	var widthCount = Math.max(sideCount[TOP], sideCount[BOTTOM]);
	
	
	var fontHeight = this.presenter.arcLabelFontHeight + 4;
	var totalFontHeight = (heightCount+1) * fontHeight;
	var totalFontWidth = (widthCount+1) * fontHeight;
	
	if (bgHeight < totalFontHeight) {
		bgHeight = totalFontHeight;
	}
	if (bgWidth < totalFontWidth) {
		bgWidth = totalFontWidth;
	}
	
	var bgLeft = labelBBox.cx - 0.5*bgWidth;
	var bgTop = labelBBox.cy - 0.5*bgHeight;
	
	var rectBox = {
		height: bgHeight,
		width: bgWidth,
		x: bgLeft,
		y: bgTop
	};
	
	var outerLeft = bgLeft - padding;
	var outerTop = bgTop - padding;
	var outerWidth = bgWidth + 2*padding;
	var outerHeight = bgHeight + 2*padding;
	
	var innerLeft = bgLeft + padding;
	var innerTop = bgTop + padding;
	var innerWidth = bgWidth - 2*padding;
	var innerHeight = bgHeight - 2*padding;
	
	var pathString = Snap.format(
		"M{outerRight},{outerBottom}H{outerLeft}V{outerTop}H{outerRight}z " +
		"M{innerLeft},{innerBottom}H{innerRight}V{innerTop}H{innerLeft}z",
		{
			outerLeft: outerLeft,
			outerTop: outerTop,
			outerRight: outerLeft + outerWidth,
			outerBottom: outerTop + outerHeight,
			
			innerLeft: innerLeft,
			innerTop: innerTop,
			innerRight: innerLeft + innerWidth,
			innerBottom: innerTop + innerHeight
		}
	);
	
	this.hotspot.attr({d: pathString});
	
	rect.attr(rectBox);
	
	rectBox.cx = labelBBox.cx;
	rectBox.cy = labelBBox.cy;
	
	this.presenter.router.reroute(this);
	
	return rectBox;
	
}

/**
 * Compute the matrix required to move this vertex to the specified position.
 * @param {Number} x The x-coordinate of the desired position 
 * @param {Number} y The y-coordinate of the desired position
 * @returns {Matrix} The matrix that will move this VertexView to the specified location.
 */
VertexView.prototype.moveToMatrix = function(x, y) {
	var bbox = this.getBBox();
	var dx = x - bbox.x;
	var dy = y - bbox.y;

	var delta = new Snap.Matrix();
	delta.translate(dx, dy);
	return this.snapElement.transform().localMatrix.add(delta);
	
}

VertexView.prototype.moveTo = function(x, y, suppressReroute) {
	
	var matrix = this.moveToMatrix(x, y);
	this.snapElement.transform(matrix);
	if (!suppressReroute) {
		this.presenter.router.reroute(this);
	}
}

function bboxToString(bbox) {
	return "bbox(" + bbox.x + ', ' + bbox.y + ', ' + bbox.width + ', ' + bbox.height + ')';
}
VertexView.prototype.inject = function() {
	
	this.presenter.paper.append(this.snapElement);
	// This next step should not be necessary.  Seems to be a workaround for a Snap bug.
	// TODO: report this bug
	this.snapElement.removed = false;
	
	if (this.removedArcList) {
		for (var i=this.removedArcList.length-1; i>=0; i--) {
			this.removedArcList[i].inject();
		}
		delete this.removedArcList;
	}
	
	
}

VertexView.prototype.remove = function() {
	this.removedArcList = this.arcList.slice(0);
	
	for (var i=this.arcList.length-1; i>=0; i--) {
		this.arcList[i].remove();
	}
	this.snapElement.remove();
}

VertexView.prototype.unselect = function() {
	this.getRect().attr(this.presenter.vertexRectAttr);
}

VertexView.prototype.select = function() {
	this.getRect().attr(this.presenter.vertexRectSelectedAttr);
}

VertexView.prototype.getBBox = function() {
	var padding = this.presenter.padding;
	var bbox = this.snapElement.getBBox();
	bbox.h = bbox.height = bbox.h - 2*padding;
	bbox.w = bbox.width = bbox.w - 2*padding;
	bbox.x = bbox.x + padding;
	bbox.y = bbox.y + padding;
	bbox.x2 = bbox.x2 - padding;
	bbox.y2 = bbox.y2 - padding;
	
	return bbox;
}

VertexView.prototype.projectOntoBoundary = function(x, y) {
	
	
	
	var rect = this.getRect();
	var bbox = rect.getBBox();
	
	var matrix = this.snapElement.transform().localMatrix;
	var x1 = matrix.x(bbox.x, bbox.y);
	var y1 = matrix.y(bbox.x, bbox.y);
	var x2 = matrix.x(bbox.x2, bbox.y2);
	var y2 = matrix.y(bbox.x2, bbox.y2);
	
	
	
	var left = new Segment(x1, y1, x1, y2);
	var right = new Segment(x2, y1, x2, y2);
	var top = new Segment(x1, y1, x2, y1);
	var bottom = new Segment(x1, y2, x2, y2);
	
	var pLeft = left.pointClosestTo(x, y);
	var pRight = right.pointClosestTo(x, y);
	var pTop = top.pointClosestTo(x, y);
	var pBottom = bottom.pointClosestTo(x,y);
	
	var dLeft = pLeft.squaredDistanceTo(x, y);
	var dRight = pRight.squaredDistanceTo(x, y);
	var dTop = pTop.squaredDistanceTo(x, y);
	var dBottom = pBottom.squaredDistanceTo(x, y);
	
	
	var p;
	var d;
	var side;
	if (dLeft < dRight) {
		side = LEFT;
		p = pLeft;
		d = dLeft;
	} else {
		side = RIGHT;
		p = pRight;
		d = dRight;
	}
	
	if (dTop < d) {
		side = TOP;
		p = pTop;
		d = dTop;
	}
	
	if (dBottom < d) {
		side = BOTTOM;
		p = pBottom;
		d = dBottom;
	}
	
	p.distance = Math.sqrt(d);
	p.side = side;
	
	return p;
	
}

VertexView.prototype.getRect = function() {
	return this.snapElement.select('rect');
}

VertexView.prototype.getLabel = function() {
	return this.snapElement.select('.konig-vertex-label');
}

VertexView.prototype.getLabelValue = function() {
	return this.getLabel().innerSVG();
}


VertexView.prototype.removeArc = function(arc) {
	var i = this.arcList.indexOf(arc);
	if (i>=0) {
		this.arcList.splice(i, 1);
	}
}

VertexView.prototype.onend = function(event) {
	if (this.presenter.tool.vertexOnEnd) {
		this.presenter.tool.vertexOnEnd(this, event);
	}
}

VertexView.prototype.selfArc = function(arc) {
	
	var bbox = this.getBBox();
	var x0 = bbox.x + 0.5*bbox.width;
	var y0 = bbox.y;
	
	var x1 = bbox.x2;
	var y1 = bbox.y + 0.5*bbox.height;
	
	arc.a.moveTo(x0, y0);
	arc.a.normal = normalTop;
	arc.b.moveTo(x1, y1);
	arc.b.normal = normalRight;
}

VertexView.prototype.addArc = function(arc) {
	if (this.arcList.indexOf(arc) == -1) {
		this.arcList.push(arc);
	}
}


VertexView.prototype.onstart = function(x, y, event) {
	if (this.presenter.tool.vertexOnStart) {
		this.presenter.tool.vertexOnStart(this, x, y, event);
	}
}

VertexView.prototype.saveNormals = function() {
	for (var i=0; i<this.arcList.length; i++) {
		var arc = this.arcList[i];
		var end = arc.otherEnd(this);
		if (end) {
			end.saveNormal = end.normal;
		}
	}
}

VertexView.prototype.unsaveNormals = function() {
	var array = [];
	for (var i=0; i<this.arcList.length; i++) {
		var arc = this.arcList[i];
		var end = arc.otherEnd(this);
		
		if (end && end.normal != end.saveNormal && array.indexOf(end.vertex)==-1) {
			array.push(end.vertex);
		}
		if (end) {
			delete end.saveNormal;			
		}
	}
	return array;
}

VertexView.prototype.ondblclick = function(event) {
	event.stopPropagation();
	
	this.presenter.vertexDialog.open(event, this);
}

VertexView.prototype.onmove = function(dx, dy, mouseX, mouseY, event) {
	
	if (this.presenter.tool.vertexOnMove) {
		this.presenter.tool.vertexOnMove(this, dx, dy, mouseX, mouseY, event);
	}
	
}



function Router() {
	
}

Router.prototype.reroute = function(vertex, map) {
	var drawArcs = false;
	if (!map) {
		map = {}
		drawArcs = true;
	}
	var vertexMap = {};
	this.rerouteVertex(vertex, map);
	vertexMap[vertex.id] = vertex;
	var list = vertex.arcList;
	for (var i=0; i<list.length; i++) {
		arc = list[i];
		if (!arc.isSelfArc()) {
			var other = arc.otherEnd(vertex).vertex;
			if (!vertexMap[other.id]) {
				vertexMap[other.id] = other;
				this.rerouteVertex(other, map);
			}
		}
	}
	
	for (var key in map) {
		var arc = map[key];
		arc.draw();
	}
}

Router.prototype.rerouteVertex = function(vertex, map) {

	var arcList = vertex.arcList;
	if (arcList.length ==0) {
		return;
	}
	
	var presenter = vertex.presenter;
	var bbox = vertex.getBBox();
	var x0 = bbox.cx;
	var y0 = bbox.cy;
	
	var origin = new Point(x0, y0);
	
	var rightBottom = origin.positiveTheta(bbox.x2, bbox.y2);
	var leftBottom = origin.positiveTheta(bbox.x, bbox.y2);
	var leftTop = origin.positiveTheta(bbox.x, bbox.y);
	var rightTop = origin.positiveTheta(bbox.x2, bbox.y);
	
	var polarList = [];
	var selfConnectCount = 0;
	for (var i=0; i<arcList.length; i++) {
		var arc = arcList[i];
		arc.pointList = null;
		var radians;
		var end;
		
		map[arc.id] = arc;
		var selfConnectKey = 0;
		if (arc.a.vertex == arc.b.vertex) {
			selfConnectKey = ++selfConnectCount;
			
			radians = rightTop;
			polarList.push({theta: radians - 2*Math.PI, arc: arc, end: arc.b, selfConnectKey: -selfConnectKey});
			
			radians = rightTop;
			end = arc.a;
			
		} else {

			
			end = arc.otherEnd(vertex);

			
			var otherVertex = end.vertex;
			var otherBBox = otherVertex.getBBox();
			
			radians = origin.positiveTheta(otherBBox.cx, otherBBox.cy);
			
			
			if (radians > rightTop) {
				radians -= 2*Math.PI;
			}

		}
	
		polarList.push({theta: radians, arc: arc, end: end, selfConnectKey: selfConnectKey});
		
		
	}
	
	polarList.sort(function(x,y){
		if (x === y) {
			return 0;
		}
		
		// We sort arcs by the angle, theta, of the vector from the center of the source
		// vertex to the center of the target vertex.
		
		var a = x.theta;
		var b = y.theta;
		
		if (a === b) {
			
			// The angle of x.arc and y.arc are the same.
			//
			// Typically, this happens if the two arcs connect the same vertices.

			var t1;
			var t2;
			
			// We need a sortable unique key for each arc so that we sort them 
			// the same way each time.  We use arc.id for this purpose.
			// But we cannot simply sort on id.  Consider the following diagram.
			
			//
			// |       Bottom of Vertex x        |
			// +---------------------------------+
			//   <--------- increasing theta
			//
			//        increasing theta ------->
			// +---------------------------------+
			// |         Top of Vertex y         |
			//
			//  Notice that theta increases in different directions on
			//  opposing faces.  To avoid arcs that cross each other
			//  we need to 
			//  
			
			var c = x.selfConnectKey - y.selfConnectKey;
			if (c != 0) {
				return c;
			}
			
			if ((a>=0) && a<Math.PI) {
				t1 = x.arc.id;
				t2 = y.arc.id;
			} else {
				t2 = x.arc.id;
				t1 = y.arc.id;
			}
			
			
			return t1 - t2;
		}
		
		return (a<b) ? -1 : 1;
	});
	
	var side = [[], [], [], []];
	for (var i=0; i<polarList.length; i++) {
		var data = polarList[i];
		var theta = data.theta;
		
		
		if (theta>rightBottom && theta<=leftBottom) {
			side[BOTTOM].push(data);
		} else if (theta>leftBottom && theta<=leftTop) {
			side[LEFT].push(data);
		} else if (theta>leftTop && theta<=rightTop) {
			side[TOP].push(data);
		} else {
			side[RIGHT].push(data);
		}

	    
	}
	
	var rectangle = new Rectangle().fromBBox(bbox);
	for (var i=0; i<side.length; i++) {
		var sideElements = side[i];
		if (sideElements.length==0) {continue;}
		var normal = NORMAL[i];
		var points = rectangle.side[i].split(sideElements.length);
		for (var j=0; j<points.length; j++) {
			var data = sideElements[j];
			var point = points[j];

			var arc = data.arc;
			var end = arc.otherEnd(data.end);
			end.moveTo(point.x, point.y);
			end.normal = normal;
		}
		
	}
	
	this.routeSelfLinks(vertex, side[TOP], bbox);
	
	
}

Router.prototype.routeSelfLinks = function(vertex, topSide, bbox) {
	if (topSide.length==0) {
		return;
	}
	var presenter = vertex.presenter;
	var fontHeight = presenter.arcLabelFontHeight;
	var pad = presenter.arcLabelPaddingLeft;
	var cx = bbox.x2;
	var cy = bbox.y;
	
	var last = topSide[topSide.length-1];
	var b = last.end;
	var a = last.arc.otherEnd(b);
	
	var d1=0;
	var d4=0;
	for (var i=topSide.length-1; i>=0; i--) {
		
		var data = topSide[i];
		var arc = data.arc;
		if (!arc.isSelfArc()) {
			return;
		}
		var a = data.end;
		var b = arc.otherEnd(a);
		
		var aTextLength = a.textLength;
		var bTextLength = b.textLength;
		if (aTextLength > 0) {
			aTextLength += pad;
		}
		if (bTextLength > 0) {
			bTextLength += pad;
		}
		
		d1 = Math.max(d1 + fontHeight, aTextLength);
		d4 = Math.max(d4 + fontHeight, bTextLength);
		
		var ax = a.point.x;
		var ay = a.point.y;
		var bx = b.point.x;
		var by = b.point.y;
		
		var top = by-d4;
		var right = ax+d1;

		arc.pointList = [
		  new Point(ax, ay),
		  new Point(right, ay),
		  new Point(right, top),
		  new Point(bx, top),
		  new Point(bx, by)
		];
		
		
	}
}



function SetVertexLabelAction(vertex, originalText, finalText) {
	this.vertex = vertex;
	this.originalText = originalText;
	this.finalText = finalText;
}

SetVertexLabelAction.prototype.undo = function() {
	this.vertex.setLabel(this.originalText, true);
}

SetVertexLabelAction.prototype.redo = function() {
	this.vertex.setLabel(this.finalText, true);
}

function ChangeArcEndLabelAction(arcEnd, initialLabel, finalLabel) {
	this.type = "ChangeArcEndLabel";
	this.arcEnd = arcEnd;
	this.initialLabel = initialLabel;
	this.finalLabel = finalLabel;
	this.suppressPublication = true;
}


ChangeArcEndLabelAction.prototype.marshal = function(doc) {
	var array = doc.actions;
	
	array.push({
		"@type" : "ChangeArcEndLabel",
		arcEnd: this.arcEnd.getId(),
		initialLabel: this.initialLabel,
		finalLabel: this.finalLabel
	});
}

ChangeArcEndLabelAction.prototype.undo = function() {
	this.arcEnd.setLabel(this.originalText, true);
}

ChangeArcEndLabelAction.prototype.redo = function() {
	this.arcEnd.setLabel(this.finalText, true);
}

function MoveVertexAction(vertex, initialPoint, finalPoint) {
	this.type="MoveVertex";
	this.vertex = vertex;
	this.initialPoint = initialPoint;
	this.finalPoint= finalPoint;
}

MoveVertexAction.prototype.marshal = function(doc) {
	var array = doc.actions;
	array.push({
		"@type" : "MoveVertex",
		"vertex" : this.vertex.snapElement.attr('id'),
		"initialPoint" : {
			x: this.initialPoint.x,
			y: this.initialPoint.y
		},
		"finalPoint" : {
			"x" : this.finalPoint.x,
			"y" : this.finalPoint.y
		}
	});
}

MoveVertexAction.prototype.undo = function() {
	this.vertex.moveTo(this.initialPoint.x, this.initialPoint.y);
}

MoveVertexAction.prototype.redo = function() {
	this.vertex.moveTo(this.finalPoint.x, this.finalPoint.y);
}

function DeleteElementAction(e) {
	this.type = "DeleteElement";
	this.element = e;
}

DeleteElementAction.prototype.undo = function() {
	this.element.inject();
}

DeleteElementAction.prototype.redo = function() {
	this.element.remove();
}
DeleteElementAction.prototype.marshal = function(doc) {
	this.appendZombie(this.element, doc.actions);
	
}

DeleteElementAction.prototype.appendZombie = function(element, array) {
	if (element instanceof Batch) {
		for (var i=0; i<element.length; i++) {
			this.appendZombie(element[i], array);
		}
	} else if (element instanceof VertexView) {
		var list = element.removedArcList;
		for (var i=0; i<list.length; i++) {
			this.appendZombie( list[i], array );
		}

		array.push({
			'@type': 'DeleteElement',
			'element' : {
				"@type" : "VertexView",
				"@id" : element.snapElement.attr('id')
			}
		});
	} else if (element instanceof ArcView) {

		array.push({
			'@type': 'DeleteElement',
			"element" : {
				"@type" : "ArcView",
				"@id" : element.path.attr('id')
			}
		});
	}
}


/*****************************************************************************/

function AddElementAction(e) {
	this.type = "AddElement";
	this.element = e;
}

AddElementAction.prototype.redo = function() {
	this.element.inject();
}

AddElementAction.prototype.undo = function() {
	this.element.remove();
}

AddElementAction.prototype.serialize = function() {
	return {
		"@type" : this.type,
		object: this.element.serialize()
	};
}

AddElementAction.prototype.marshal = function(doc) {
	if (this.element.marshal) {
		this.element.marshal(doc);
	}
	
}



/***********************************************************************/
/**
 * @classdesc An array-like object that aggregates a collection of elements.
 * This makes it easier to perform bulk operations on all elements
 * within the batch.
 */
function Batch() {
	this.length = 0;
	for (var i=0; i<arguments.length; i++) {
		this.push(arguments[i]);
	}
}

Batch.prototype.splice = function(index, count) {
		
	for (var i=index+count; i<this.length; i++) {
		var j = i - count;
		this[j] = this[i];
	}
	this.length -= count;
}

Batch.prototype.clone = function() {
	var other = new Batch();
	for (var i=0; i<this.length; i++) {
		other.push(this[i]);
	}
	return other;
}

Batch.prototype.contains = function(element) {
	return this.indexOf(element) >= 0;
}

/**
 * Get the index of an element within this Batch.
 * @param {KonigElement} element The element whose index is to be returned.
 * @returns {Number} The index of the specified element, or -1 if it is not contained within this Batch.
 */
Batch.prototype.indexOf = function(element) {
	for (var i=0; i<this.length; i++) {
		if (this[i]===element) {
			return i;
		}
	}
	return -1;
}

/**
 * Append an element to this batch.
 * @param {Element} element The element to be appended.  Must be of type VertexView or ArcView.
 */
Batch.prototype.push = function(element) {
	this[this.length++] = element;
}

/**
 * Select all elements in the batch.
 */
Batch.prototype.select = function() {
	for (var i=0; i<this.length; i++) {
		this[i].select();
	}
}

/**
 * Unselect all elements in the batch.
 */
Batch.prototype.unselect = function() {
	for (var i=0; i<this.length; i++) {
		this[i].unselect();
	}
}

/**
 * Remove from the canvas all elements in the batch
 */
Batch.prototype.remove = function() {
	
	// Remove arcs first, and then vertices
	
	for (var i=0; i<this.length; i++) {
		var elem = this[i];
		if (elem instanceof ArcView) {
			elem.remove();
		}
	}

	for (var i=0; i<this.length; i++) {
		var elem = this[i];
		if (elem instanceof VertexView) {
			elem.remove();
		}
	}
}

/**
 * Inject into the canvas all elements in the batch
 */
Batch.prototype.inject = function() {
	
	for (var i=this.length-1; i>=0; i--) {
		var elem = this[i];
		if (elem instanceof VertexView) {
			elem.inject();
		}
	}

	for (var i=this.length-1; i>=0; i--) {
		var elem = this[i];
		if (elem instanceof ArcView) {
			elem.inject();
		}
	}
}

/**
 * Remove all items from this batch.
 */
Batch.prototype.clear = function() {
	
	for (var i=0; i<this.length; i++) {
		delete this[i];
	}
	this.length = 0;
}


/***********************************************************************/
function SelectionManager() {
	this.selection = new Batch();
	
}


SelectionManager.prototype.batchSelect = function(batch, append) {
	if (!append) {
		this.unselectAll();
	}
	if (batch.length > 0) {

		for (var i=0; i<batch.length; i++) {
			var element = batch[i];
			this.selection.push(element);
			element.select();
		}
	}
}


SelectionManager.prototype.batchUnselect = function(batch) {
	if (batch.length > 0) {

		for (var i=0; i<batch.length; i++) {
			var element = batch[i];
			var index = this.selection.indexOf(element);
			element.unselect();
			if (index) {
				this.selection.splice(index, 1);
			}
		}
	}
}

SelectionManager.prototype.select = function(element) {
	this.unselectAll();
	element.select();
	this.selection.push(element);
	
	
}

SelectionManager.prototype.unselectAll = function(element) {
	this.selection.unselect();
	this.selection.clear();
}

/*************************************************************************/
SproutAnimator = function(vertexView, action) {
	this.vertexView = vertexView;
	this.action = (action instanceof CompositeAction) ? action : new CompositeAction(action);
	this.duration = 500;
}


SproutAnimator.prototype.animate = function() {
	
	this.bbox = this.vertexView.getBBox();
	for (var i=0; i<this.action.length; i++) {
		var action = this.action[i];
		if (action instanceof AddElementAction) {
			if (action.element instanceof VertexView) {
				if (action.element != this.vertexView) {
					this.animateVertex(action.element);
				}
			} else if (action.element instanceof ArcView) {
				this.animateArc(action.element);
			}
		} else if (action instanceof ViewBoxChangeAction) {
			this.animateViewBoxChange(action);
		}
	}
	var self = this;
	
	setTimeout(function(){
		self.cleanup();
	}, this.duration + 50);
	
}

SproutAnimator.prototype.animateViewBoxChange = function(action) {

	var p = this.vertexView.presenter.paper;
	
	Snap.animate(
		action.initialState.split(" "), 
		action.finalState.split(" "), 
		function(values){
			p.attr("viewBox", values.join(" ")); 
		}, this.duration);
	
}

SproutAnimator.prototype.cleanup = function() {

	var node = this.vertexView.presenter.paper.node;
	
	$(node).find('animate').remove();
	for (var i=0; i<this.action.length; i++) {
		var action = this.action[i];
		if (action instanceof AddElementAction) {
			if (action.element instanceof ArcView) {
				var arcView = action.element;
				$(arcView.path.node).find('animate').remove();
				arcView.path.node.setAttribute('d', arcView.animationEnd);
				delete arcView.animationEnd;
			}
		} else if (action instanceof ViewBoxChangeAction) {
			node.setAttribute('viewBox', action.finalState);
		}
	}
}

SproutAnimator.prototype.animateArc = function(arcView) {
	var otherEnd = arcView.otherEnd(this.vertexView);
	if (otherEnd) {
		var end = arcView.renderer.curved.toSvgPath(arcView);
		if (!end) {
			console.log("final path is not defined: " + arcView.toString());
		}
		arcView.animationEnd = end;
		var selfEnd = arcView.otherEnd(otherEnd);
		
		var otherPoint = otherEnd.point;
		otherEnd.point = selfEnd.point;
		
		arcView.path.attr({opacity: 0});
		arcView.path.animate({opacity: 1}, this.duration);
		
		var start = arcView.renderer.curved.toSvgPath(arcView);
		otherEnd.point = otherPoint;
		
		var path = arcView.path.node;
		// Web Animations don't seem to work on path 'd' attributes.
		// I guess this is not yet widely supported.
		// Use SMIL animation for now.
		// TODO: Replace with Web Animation when it is better supported.
		path.setAttribute('d', start);
		
		var a = document.createElementNS("http://www.w3.org/2000/svg", "animate");
		a.setAttribute('attributeName', 'd');
		a.setAttribute('attributeType', 'XML');
		a.setAttribute('from', start);
		a.setAttribute('to', end);
		a.setAttribute('begin', 'indefinite');
		a.setAttribute('dur', this.duration + 'ms');
		a.setAttribute('fill', 'freeze');
		path.appendChild(a);
		a.beginElement();
		
		if (arcView.a.arrowHead) {
			
			arcView.a.arrowHead.node.animate([
			   {opacity: 0, offset: 0},
			   {opacity: 0, offset: 0.9},
			   {opacity: 1, offset: 1}
			], {
				duration: this.duration
			});
			
			
		}

		if (arcView.b.arrowHead) {
			arcView.b.arrowHead.node.animate([
   			   {opacity: 0, offset: 0},
   			   {opacity: 0, offset: 0.9},
   			   {opacity: 1, offset: 1}
   			], {
   				duration: this.duration
   			});
		}
		
		
	}
	
}

SproutAnimator.prototype.vertexCenterStart = function(vertexView) {
	var count=0;
	var x=0;
	var y=0;
	
	var list = vertexView.arcList;
	for (var i=0; i<list.length; i++) {
		var arc = list[i];
		var end = arc.otherEnd(vertexView);
		if (end) {
			count++;
			x += end.point.x;
			y += end.point.y;
		}
	}
	return (count>0) ? new Point(x/count, y/count) : null;
	
}

SproutAnimator.prototype.animateVertex = function(vertexView) {
	
	var start = this.vertexCenterStart(vertexView); 
	if (start) {
		
		var endTransform = vertexView.snapElement.node.getAttribute('transform');
		var bbox = vertexView.getBBox();

		var r = Math.sqrt(bbox.width*bbox.width + bbox.height*bbox.height);
		var factor = 1/r;
		
		var dx = -start.x*(factor-1);
		var dy = -start.y*(factor-1);
		
		var x = start.x - 0.5;
		var y = start.y - 0.5;
		
		var startTransform = 'translate(' + dx + ',' + dy + ') scale(' + factor + ') ' +
			'translate(' + x + ',' + y + ')';
		
		var snap = vertexView.snapElement;
		
		snap.transform(startTransform);
		snap.animate({transform: endTransform}, this.duration);
		
	}
	
	
	
	
}

/****************************************************************************/
function ActionChannel() {
	
}

ActionChannel.prototype.sendMessage = function(json) {
//	var msg = JSON.stringify(json, null, '\t');
//	console.log(msg);
}

/****************************************************************************/
function ActionEnvelope() {
	this['@type'] = "CompositeAction";
	this.actions = [];
}

ActionEnvelope.prototype.assertNamespace = function(prefix, namespaceIRI) {
	var context = this.localContext();
	context[prefix] = namespaceIRI;
}

ActionEnvelope.prototype.localContext = function() {
	var context = this['@context'];
	if (!context) {
		context = this['@context'] = [];
	}
	for (var i=0; i<context.length; i++) {
		var c = context[i];
		if (typeof(c) === 'object') {
			return c;
		}
	}
	var c = {};
	context.push(c);
	return c;
}

/****************************************************************************/
function ActionDAO() {
	this.channel = new ActionChannel();
}

ActionDAO.prototype.beginTransaction = function() {
	this.envelope = new ActionEnvelope();
}

ActionDAO.prototype.commit = function() {
	if (this.envelope && this.envelope.actions.length>0) {
		this.channel.sendMessage(this.envelope);
		this.envelope = null;
	}
}

ActionDAO.prototype.publish = function(action) {
	var envelope = this.envelope;
	if (!envelope) {
		envelope = new ActionEnvelope();
	}
	
	if (action.serialize && !action.suppressPublication) {
		envelope.actions.push(action.serialize());
	} else if (action.marshal && !action.suppressPublication) {
		
		
		action.marshal(envelope);
	} 

	if (!this.envelope && envelope.actions.length>0) {
		this.channel.sendMessage(envelope);
	}
}


/****************************************************************************/
function VertexDialog(presenter){
	this.presenter = presenter;
	var dialog = $("#konig-vertex-dialog");
	this.template = dialog.html();
	dialog.remove();
	
}

VertexDialog.prototype.open = function(event, vertex) {

	var p = this.presenter.mouseToSvg(event.clientX, event.clientY);
	
	var dialogTitle = vertex ? "Rename Concept" : "New Item";
	
	var originalText = vertex ? vertex.getLabelValue() : "";
	
	var data = {
		nameValue: originalText
	};
	
	
	var dialogHTML = Mustache.render(this.template, data);
	
	var placeHTML = Snap.format(
		"<div style='position: absolute; left: {x}px; top: {y}px'></div>",
		{x: event.clientX, y: event.clientY}
	);
	
	var dialogPlace = $(placeHTML);
	
	
	var dialogBody = $(dialogHTML);
	$('body').append(dialogPlace).append(dialogBody);
	
	$("#konig-vertex-dialog-item-type-select").change(function() {
		var selectedType = $(this).val();
		var fieldname = (selectedType === "Concept") ? "Concept Name" : "Value";
		$("#konig-vertex-name-fieldname").text(fieldname);
	});

	var self = this;
	var presenter = this.presenter;
	var submit = function() {
		var value = $('#konig-vertex-name-input').val();
		var itemType = $('#konig-vertex-dialog-item-type-select option:selected').val();
		
		dialogBody.remove();
		dialogPlace.remove();
		if (vertex) {
			vertex.setLabel(value);
			vertex.setRdfType(itemType);
		} else {
			presenter.onCloseVertexDialog(p, value, itemType, self.suggestion);
		}
	};
	
	$('#konig-vertex-name-input').keydown(function(event) {
		var keyCode = event.keyCode;
		if (keyCode == 13) {
			// return key
			submit();
		}
	}).select().autocomplete({
		select: function(event, ui) {
			self.suggestion = ui.item;
			$('#konig-vertex-name-input').val(ui.item.label);
			return false;
		},
		source: function(request, response){
			// TODO: use correct request depending on the item type.
			konig.autocompleteService.suggestResource(request, response);
		}
	});
	
	
	
	
	dialogBody.dialog({
		title: dialogTitle,
		modal: true,
		width: 'auto',
		height: 'auto',
		dialogClass: 'konig-vertex-dialog',
		position: {
			my : 'center',
			at : 'center',
			of : dialogPlace
		},
		close: function() {
			dialogBody.remove();
			dialogPlace.remove();
		}
	});
}

/****************************************************************************/

function Presenter() {
	
	this.paper = Snap('#konig-map');
	this.vertexDialog = new VertexDialog(this);
	this.tool = new UniversalTool();
	this.selectionManager = new SelectionManager(this.paper);
	this.actionHistory = new HistoryManager(this);
	this.actionDAO = new ActionDAO();
	this.dragContext = {};
	this.padding = 5;
	this.eventHandlers = {};
	this.elementDecoratorList = [];
	this.vertexLabelAttr = {
		fontFamily: "Arial"
	}
	this.arrowHeadLength = 8;
	this.arrowHeadConfig = {
		id: 'konig-arrowhead'
	}
	

	var arrowHead = this.paper.path("M 0,-3 L 0,3 L 8,0 z");
	arrowHead.attr(this.arrowHeadConfig);
	arrowHead.toDefs();
	
	this.cornerRadius = 10;
	this.arcLabelConfig = {
		fontFamily: "Arial",
		fill: "black"
			
	}
	this.vertexRectAttr = {
		fill: "#fff",
		stroke: "#000",
		strokeWidth: 2
	}
	this.vertexRectSelectedAttr = {
		stroke: 'red'
	};
	this.arcPathAttr = {
		stroke: "black", 
		fill: "transparent",
		strokeWidth: 2
	};
	this.arcPathSelectedAttr = {stroke: "red"};
	this.arcRenderer = new SmartArcRenderer();
	this.router = new Router();
	
	var testText = this.paper.text(50, 50, "Testing");
	var bbox = testText.getBBox();
	testText.remove();
	this.arcLabelFontHeight = bbox.height;
	this.arcLabelPaddingLeft = 20;
	

	var self = this;
	this.bindPaper();
	
	$(document).keydown(function(event) {
		self.onkeydown(event);
	}).keyup(function(event){
		self.onkeyup(event);
	});
	
	// Register actions / events
	this.AddElement = AddElementAction;
	this.DeleteElement = DeleteElementAction;
	this.MoveVertex = MoveVertexAction;
	
}

Presenter.prototype.bindPaper = function() {

	var self = this;
	this.paper.dblclick(function(event) {
		self.ondblclick(event);
	});
	this.paper.click(function(event) {
		this.presenter.click(event)
	}).drag(this.onmove, this.onstart, this.onend, this, this, this);

	this.paper.presenter = this;
	var txn = this.actionHistory.beginTransaction();
	var paper = $(this.paper.node);
	var vertexList = [];
	paper.find('.konig-vertex').each(function(index, elem){
		var group = Snap(elem);
		var vertexView = new VertexView(self, group);
		group.konigVertex = vertexView;

		self.actionHistory.append(new AddElementAction(vertexView));
		vertexList.push(vertexView);
	});
	paper.find('.konig-arc').each(function(index, elem){
		var path = Snap(elem);
		var arc = self.reconstructArc(path);
	});
	paper.find('[property="konig:arrowHeadOf"]').each(function(index, elem){
		
		var arrowHead = Snap(elem.parentElement);
		var arcEndId = $(elem).text();
		// We are relying on the fact that the arcEndId has the format
		// {arcId}-end-{a|b}
		var arcId = arcEndId.substring(0, arcEndId.length-6);
		var arcNode = document.getElementById(arcId);
		var snapPath = Snap(arcNode);
		var arc = snapPath.konigArc;
		var arcEnd = arc.a.id===arcEndId ? arc.a : arc.b;
		arcEnd.arrowHead = arrowHead;
		arrowHead.removeClass('konig-selected-arrowhead');
	});
	
	// It's not clear why, but we need to explicitly reroute each vertex.
	// If we don't, we'll see rending problems when the user moves one of the vertices.
	// TODO: Figure out why this rerouting is necessary, and see if we can avoid it.
	// It's possible the reconstructArc method gets the ArcEnd point and normal 
	// properties wrong.
	for (var i=0; i<vertexList.length; i++) {
		var vertex = vertexList[i];
		this.router.rerouteVertex(vertex, {});
	}
	
	this.actionHistory.commit(txn);
}

var filterEmptyStrings = function(array) {
	var list = [];
	for (var i=0; i<array.length; i++) {
		var value = array[i];
		if (value.length>0) {
			list.push(value);
		}
	}
	return list;
}

var toNormal = function(x0, y0, x1, y1) {
	if (x0 === x1) {
		return y1>y0 ? normalBottom : normalTop;
	}
	return (x1>x0) ? normalRight : normalLeft;
}

Presenter.prototype.reconstructArc = function(path) {
	
	var arcId = path.attr('id');
	
	var d = filterEmptyStrings( path.attr('d').split(/[, ]/) );
	
	var ax = Number(d[1]);
	var ay = Number(d[2]);
	var acx = Number(d[4]);
	var acy = Number(d[5]);
	var bcx = Number(d[6]);
	var bcy = Number(d[7]);
	var bx = Number(d[8]);
	var by = Number(d[9]);
	
	var p = $(path.node);
	var aVertexId = p.find('[property="konig:vertexA"]').text();
	var bVertexId = p.find('[property="konig:vertexB"]').text();
	
	var aPoint = new Point(ax, ay);
	var bPoint = new Point(bx, by);
	
	var aNormal = toNormal(ax, ay, acx, acy);
	var bNormal = toNormal(bx, by, bcx, bcy);
	
	var aVertex = this.getVertexById(aVertexId);
	var bVertex = this.getVertexById(bVertexId);
	
	var a = new ArcEnd(aVertex, aPoint, aNormal);
	var b = new ArcEnd(bVertex, bPoint, bNormal);
	
	var arc = new ArcView(arcId, this.arcRenderer, a, path, b);
	a.reconstructText();
	b.reconstructText();
	
	this.actionHistory.append(new AddElementAction(arc));

	aVertex.addArc(arc);
	bVertex.addArc(arc);
	
	arc.path.removeClass('konig-selected-arc');
	
	return arc;
}

Presenter.prototype.getVertexById = function(id) {
	var node = document.getElementById(id);
	var snapElement = Snap(node);
	return snapElement.konigVertex;
}

Presenter.prototype.renderSVG = function(svgURL, svgString) {
	var node = $(this.paper.node);
	
	node.parent().append(svgString);
	
	var newNode = $("[resource='" + svgURL + "']");
	if (newNode.length == 1) {
		this.paper.remove();
		this.paper = Snap(newNode[0]);
		this.bindPaper();
	} else {
		console.log("Something went wrong");
	}
	
}


/**
 * Interface for decorating the JSON representation of Konig elements when
 * they are marshaled.
 * 
 * @interface ElementDecorator
 */

/**
 * Handle the creation of a new VertexView.  This method is called when the user
 * creates a new VertexView via the VertexDialog.
 * 
 * @function
 * @name ElementDecorator#createVertex
 * @param {VertexView} vertexView The VertexView that was created.
 */

/**
 * Handle a change to the label for an ArcEnd.
 * @function
 * @name ElementDecorator#changeArcEndLabel
 * @param {ArcEnd} end The ArcEnd whose label has changed.
 */

/**
 * Decorate the JSON representation of a VertexView
 * @function
 * @name ElementDecorator#marshalVertex
 * @param {VertexView} vertexView The VertexView whose JSON representation is to be decorated.
 * @param {JsonObject} json The JSON object representing the given VertexView
 */

/**
 * Decorate the JSON representation of an ArcView
 * @function
 * @name ElementDecorator#marshalArc
 * @param {ArcView} arcView The ArcView whose JSON representation is to be decorated.
 * @param {JsonObject} json The JSON object representing the given ArcView.
 */

/**
 * Add a decorator for an element.
 * @param {ElementDecorator} decorator An ElementDecorator that will be invoked whenever
 * a Konig element is marshaled.
 */
Presenter.prototype.addElementDecorator = function(decorator) {
	this.elementDecoratorList.push(decorator);
}

/**
 * Invoke the 'marshalVertex' method on all ElemenDecorator instances registered
 * with this Presenter.
 * @param {VertexView} vertexView The VertexView to be decorated.
 * @param {JsonObject} json The JSON object representing the given VertexView.
 */
Presenter.prototype.marshalVertex = function(vertexView, json) {
	var list = this.elementDecoratorList;
	for (var i=0; i<list.length; i++) {
		var decorator = list[i];
		if (decorator.marshalVertex) {
			decorator.marshalVertex(vertexView, json);
		}
	}
}


/**
 * Invoke the 'marshalArc' method on all ElemenDecorator instances registered
 * with this Presenter.
 * @param {ArcView} arcView The ArcView to be decorated.
 * @param {JsonObject} json The JSON object representing the given ArcView.
 */
Presenter.prototype.marshalArc = function(arcView, json) {
	var list = this.elementDecoratorList;
	for (var i=0; i<list.length; i++) {
		var decorator = list[i];
		if (decorator.marshalArc) {
			decorator.marshalArc(arcView, json);
		}
	}
}

Presenter.prototype.SproutAnimator = SproutAnimator;


/**
 * Delete an element from the presenter.
 * @param {VertexView|ArcView} element The element to be deleted.
 */
Presenter.prototype.deleteElement = function(element) {
	var action = new DeleteElementAction(element);
	action.redo();
	this.actionHistory.append(action);
}

/**
 * Bind a new event handler to a specified event. When an event of that type fires, the supplied
 * handler will be invoked on the object specified as the 'context'.
 * @param {String} eventName The name of the event type.
 * @param {Function} handler A function that receives an Event as its sole argument.
 * @param {Object} context An object on which the handler will be invoked.
 */
Presenter.prototype.bind = function(eventName, handler, context) {
	var handlerList = this.eventHandlers[eventName];
	if (!handlerList) {
		handlerList = [];
		this.eventHandlers[eventName] = handlerList;
	}
	handlerList.push(new HandlerContext(handler, context));
}

/**
 * Removes a handler for the specified type of event.
 * @param {String} eventName The type of event.
 * @param {Function} handler The function representing the handler that will be removed.
 * @param {Object} context The handler's context.
 */
Presenter.prototype.unbind = function(eventName, handler, context) {
	var handlerList = this.eventHandlers[eventName];
	if (handlerList) {
		
		for (var i=0; i<handlerList.length; i++) {
			var e = handlerList[i];
			if (
				(e.handler===handler) && 
				((!context && !e.context) || (context && (context === e.context))) 
			) {
				handlerList.splice(i, 1);
				return;
			}
		}
	}
}

/**
 * Notify the handlers that an event occurred.
 * @param event The event that occurred.
 */
Presenter.prototype.notifyHandlers = function(event) {
	if (event instanceof CompositeAction) {
		if (this.actionDAO) {
			this.actionDAO.beginTransaction();
		}
		for (var j=0; j<event.length; j++) {
			this.notifyHandlers(event[j]);
		}
	} else {

		var handlerList = this.eventHandlers[event.type];
		if (handlerList) {
			for (var i=0; i<handlerList.length; i++) {
				var e = handlerList[i]
				e.handler.call(e.context, event);
			}
		}
	}
	
	if (this.actionDAO) {
		if (event instanceof CompositeAction) {
			this.actionDAO.commit();
		} else {
			this.actionDAO.publish(event);
		}
	}
	
}

Presenter.prototype.vertexList = function() {
	var list = [];
	$('.konig-vertex').each(function(index, elem){
		var snap = Snap(elem);
		var vertex = snap.konigVertex;
		list.push(vertex);
	});
	return list;
}


/**
 * Transform the view box so that it is centered on a specific VertexView 
 * without changing the scale.
 * @param {VertexView} vertexView The node that is to be centered in the view box.
 */
Presenter.prototype.centerViewBoxAt = function(element) {
	if (element instanceof VertexView) {
		var bbox = element.getBBox();
		var cx = bbox.cx;
		var cy = bbox.cy;
		
		var viewBox = this.viewBox();
		var rect = viewBox.getBox();
	
		var dx = cx - rect.cx;
		var dy = cy - rect.cy;
		var x0 = rect.x;
		var y0 = rect.y;
		var width = rect.width;
		var height = rect.height;
		var begin = x0 + ' ' + y0 + ' ' + width + ' ' + height;
		
		var x1 = x0 + dx;
		var y1 = y0 + dy;
		
		var end = x1 + ' ' + y1 + ' ' + width + ' ' + height;

		this.paper.node.setAttribute('viewBox', begin);
		if (begin != end) {
			var txn = this.actionHistory.beginTransaction();
			var action = new ViewBoxChangeAction(this, begin, end);
			this.actionHistory.append(action);
			this.actionHistory.commit(txn);
			
			return action;
		}
		
	}
	return null;
}

Presenter.prototype.matrix = function(a, b, c, d, e, f) {
	if (arguments.length==0) {
		a = 1;
		b = 0;
		c = 0;
		d = 1;
		e = 0;
		f = 0;
	}
	var m = this.paper.node.createSVGMatrix();
	m.a = a;
	m.b = b;
	m.c = c;
	m.d = d;
	m.e = e;
	m.f = f;
	return m;
}

Presenter.prototype.matrixString = function(m) {
	
	return 'matrix(' + 
		m.a + ',' + m.b + ',' + m.c + ',' + m.d + ',' + m.e + ',' + m.f + ')';
	
}

Presenter.prototype.applyTransform = function(domNode, matrix) {
	domNode.setAttribute('transform', matrixString);
}

Presenter.prototype.mouseToSvg = function(mouseX, mouseY) {

	var offset = $(this.paper.node).offset();
	
	var m = this.paper.transform().globalMatrix.invert();
	
	
	var x0 = mouseX - offset.left;
	var y0 = mouseY - offset.top;
	
	var x = m.x(x0, y0);
	var y = m.y(x0, y0);
	
	return new Point(x, y);
}

Presenter.prototype.nextId = function() {
	return uuid.v1();
}


Presenter.prototype.vertexAncestor = function(e) {
	
	while (e && !e.konigVertex) {
		e = e.parent();
	}
	
	return e ? e.konigVertex : null;
}

Presenter.prototype.openVertexDialog = function(x, y, vertex) {
	
	var dialogTitle = vertex ? "Rename Concept" : "New Concept";
	
	var originalText = vertex ? vertex.getLabelValue() : "";
	
	var dialogHTML = Snap.format(
		'<div>' +
			'<table>' +
			'<tr>'+
				'<td>Name</td>' +
				'<td><input id="konig-vertex-name-input" value="{inputValue}"></input></td>'+
			'</tr>' +
			'</table>' +
		'</div>',
		{
			inputValue: originalText
		}
	);
	
	var placeHTML = Snap.format(
		"<div style='position: absolute; left: {x}px; top: {y}px'></div>",
		{x: x, y: y}
	);
	
	var dialogPlace = $(placeHTML);
	
	
	var dialogBody = $(dialogHTML);
	$('body').append(dialogPlace).append(dialogBody);

	var self = this;
	var submit = function() {
		var value = $('#konig-vertex-name-input').val();
		
		dialogBody.remove();
		dialogPlace.remove();
		if (vertex) {
			vertex.setLabel(value);
		} else {
			self.onCloseVertexDialog(x, y, value);
		}
	};
	
	$('#konig-vertex-name-input').keydown(function(event) {
		var keyCode = event.keyCode;
		if (keyCode == 13) {
			// return key
			submit();
		}
	}).select();
	
	
	dialogBody.dialog({
		title: dialogTitle,
		modal: true,
		width: 350,
		height: 100,
		dialogClass: 'konig-vertex-dialog',
		position: {
			my : 'center',
			at : 'center',
			of : dialogPlace
		},
		close: function() {
			dialogBody.remove();
			dialogPlace.remove();
		}
	});
	
}

Presenter.prototype.onCloseVertexDialog = function(position, conceptName, rdfType, suggestion) {
	var conceptName = conceptName.trim();
	if (conceptName) {
		var txn = this.actionHistory.beginTransaction();
		
		var vertex = this.vertex(position.x, position.y, conceptName, true);
		vertex.setRdfType(rdfType);
		if (suggestion && suggestion.label === conceptName) {
			vertex.setResource(suggestion.value);
		}
		
		var list = this.elementDecoratorList;
		for (var i=0; i<list.length; i++) {
			var e = list[i];
			if (e.createVertex) {
				e.createVertex(vertex);
			}
		}

		this.actionHistory.append(new AddElementAction(vertex));
		this.actionHistory.commit(txn);
	}
}

Presenter.prototype.onkeydown = function(event) {
	var keyCode = event.keyCode;
	if (keyCode == 46) {
		// Delete
		
		var batch = this.selectionManager.selection;
		if (batch.length > 0) {

			batch.unselect();
			batch.remove();
			
			this.selectionManager.selection = new Batch();
			this.actionHistory.append(
				new CompositeAction(
					new UnselectAction(this, batch),
					new DeleteElementAction(batch)
				)
			);
			
		}
	} if (event.ctrlKey && (keyCode == 122) || (keyCode==90)) {
		// ctrl-Z
		
		this.actionHistory.undo();
	} if (event.ctrlKey && (keyCode==121 || keyCode==89)) {
		// ctrl-Y
		this.actionHistory.redo();
	}
	
	if (this.tool.paperKeydown) {
		this.tool.paperKeydown(this, event);
	}
}

Presenter.prototype.onkeyup = function(event) {
	if (this.tool.paperKeyup) {
		this.tool.paperKeyup(this, event);
	}
}

Presenter.prototype.click = function(event) {
	if (this.tool.paperOnClick) {
		this.tool.paperOnClick(this, event);
	}
	
}

Presenter.prototype.arc = function(startVertex, endVertex) {
	var arc = this.arcEnds(
		new ArcEnd(startVertex, new Point(), normalTop), new ArcEnd(endVertex, new Point(), normalTop)
	);
	startVertex.addArc(arc);
	endVertex.addArc(arc);
	return arc;
}

Presenter.prototype.arcList = function() {

	var list = [];
	$(this.paper.node).find('.konig-arc').each(function(index, elem){
		var snap = Snap(elem);
		var arc = snap.konigArc;
		list.push(arc);
	});
	return list;
}

Presenter.prototype.arcEnds = function(start, end, suppressHistory) {
	var id = "arc-" + this.nextId();
	var path = this.paper.path();
	path.attr(this.arcPathAttr);
	var arc = new ArcView(id, this.arcRenderer, start, path, end);
	path.attr({id: id});
	path.attr(this.arcPathAttr);
	
	path.dblclick(arcDoubleClick);
	if (!suppressHistory) {
		this.actionHistory.append(new AddElementAction(arc));
	}
	return arc;
}

/**
 * Convert a Snap Element to a Konig Element
 * @param snapElement The Snap element.
 * @returns The corresponding Konig element, or null if there is no corresponding element.
 */
Presenter.prototype.snap2konig = function(snapElement) {
	if (snapElement.konigVertex) {
		return snapElement.konigVertex;
	}
	if (snapElement.konigArc) {
		return snapElement.konigArc;
	}
	return null;
}

Presenter.prototype.getKonigElementByPoint = function(x, y) {
	var top = Snap.getElementByPoint(x, y);
	while (top) {
		if (top.konigArc) return top.konigArc;
		if (top.konigVertex) return top.konigVertex;
		if (top.konigArcEnd) return top.konigArcEnd;
		top = top.parent();
	}
	return null;
}

Presenter.prototype.onstart = function(x, y, event) {
	if (this.tool.paperOnStart) {
		this.tool.paperOnStart(this, x, y, event);
	}
}
Presenter.prototype.onmove = function(dx, dy, x, y, event) {
	if (this.tool.paperOnMove) {
		this.tool.paperOnMove(this, dx, dy, x, y, event);
	}
	
}

Presenter.prototype.viewBox = function() {
	return new ViewBox(this.paper.node);
}

Presenter.prototype.onend = function(event) {
	if (this.tool.paperOnEnd) {
		this.tool.paperOnEnd(this, event);
	}
}

Presenter.prototype.ondblclick = function(event) {
	var e = this.getKonigElementByPoint(event.clientX, event.clientY);
	if (e instanceof ArcView) {
		e.ondblclick(event);
	} else {
		this.vertexDialog.open(event);
	}
}

Presenter.prototype.globalBBox = function(elem) {
	var bbox = elem.getBBox();
	
	var left = bbox.x;
	var top = bbox.y;
	var right = bbox.x2;
	var bottom = bbox.y2;
	
	var matrix = elem.transform().globalMatrix;
	var result = {};
	
	result.x1 = result.x = matrix.x(left, top);
	result.y1 = result.y = matrix.y(left, top);
	result.x2 = matrix.x(right, top);
	result.y2 = matrix.y(left, bottom);
	
	return result;
}

Presenter.prototype.vertex = function(x, y, label, suppressHistory) {
	var s = this.paper;
	
	var text = s.text(100, 100, label)
		.attr(this.vertexLabelAttr)
		.addClass('konig-vertex-label');
//	
//	var bbox = text.getBBox();
//	var width = bbox.width + this.padding*2;
//	var height = bbox.height + this.padding*2;
	var rect = s.rect(100, 100, 110, 110, this.cornerRadius, this.cornerRadius)
		.attr(this.vertexRectAttr)
		.addClass('konig-vertex-rect');
	
	
	var hotspot = s.path('')
		.addClass('konig-vertex-hotspot')
		.attr({'fill-rule' : 'nonzero'});
	
	
	var group = s.g(rect, text, hotspot);
	
	group.addClass('konig-vertex');
	var vertex = new VertexView(this, group, rect, text, hotspot);


	if (!suppressHistory) {
		this.actionHistory.append(new AddElementAction(vertex));
	}

	var bbox = vertex.pack();
	
	
	var transformString = Snap.format('translate({dx},{dy})', {dx: x-bbox.cx, dy: y-bbox.cy});
	group.transform(transformString);
	return vertex;
	
}


/*********************************************************************************/
UniversalTool = function() {
	this.panTool = new PanTool();
	this.connectTool = new ConnectTool();
	this.multiSelectTool = new MultiSelectTool();
	this.useMultiSelect = false;
	this.showMultiSelect = false;
}


UniversalTool.prototype.paperKeydown = function(presenter, event) {
	if (event.shiftKey) {
		this.showMultiSelect = true;
		presenter.paper.addClass('konig-multiselect');
	}
}

UniversalTool.prototype.paperKeyup = function(presenter, event) {
	if (this.showMultiSelect && !event.shiftKey && !this.useMultiSelect) {
		this.showMultiSelect = false;
		presenter.paper.removeClass('konig-multiselect');
	}
}

UniversalTool.prototype.paperOnStart = function(presenter, mouseX, mouseY, event) {
	
	
	if (event.shiftKey) {
		this.useMultiSelect = true;
		this.multiSelectTool.paperOnStart(presenter, mouseX, mouseY, event);
		
	} else {
		var element = presenter.getKonigElementByPoint(mouseX, mouseY);
		if (element instanceof ArcEnd) {
			element = element.arc;
		}
		if (element instanceof ArcView) {
			this.connectTool.moveTool.selectTool.arcOnClick(element, event);
			return;
		}
		
		presenter.selectionManager.unselectAll();
		this.panTool.paperOnStart(presenter, mouseX, mouseY, event);
	}
	
	
}

UniversalTool.prototype.paperOnMove = function(presenter, dx, dy, mouseX, mouseY, event) {
	if (this.useMultiSelect) {
		this.multiSelectTool.paperOnMove(presenter, dx, dy, mouseX, mouseY, event);
	} else {
		this.panTool.paperOnMove(presenter, dx, dy, mouseX, mouseY, event);
	}
}


UniversalTool.prototype.paperOnEnd = function(presenter, event) {
	
	if (this.useMultiSelect) {
		this.multiSelectTool.paperOnEnd(presenter, event);
		this.useMultiSelect = false;
		if (!event.shiftKey) {
			presenter.paper.removeClass('konig-multiselect');
			this.showMultiSelect = false;
		}
	} else {
		this.panTool.paperOnEnd(presenter, event);
	}
}

UniversalTool.prototype.vertexOnStart = function(vertex, mouseX, mouseY, event) {
	this.connectTool.vertexOnStart(vertex, mouseX, mouseY, event);
}

UniversalTool.prototype.vertexOnMove = function(vertex, dx, dy, mouseX, mouseY, event) {
	this.connectTool.vertexOnMove(vertex, dx, dy, mouseX, mouseY, event);
}

UniversalTool.prototype.vertexOnEnd = function(vertex, event) {
	this.connectTool.vertexOnEnd(vertex, event);
}

/*********************************************************************************/
ConnectTool = function(moveTool) {
	this.moveTool = moveTool || new MoveTool();
}

ConnectTool.prototype.vertexOnStart = function(vertex, mouseX, mouseY, event) {
	event.stopPropagation();

	var point = vertex.presenter.mouseToSvg(mouseX, mouseY);
	x = point.x;
	y = point.y;

	var p = vertex.projectOntoBoundary(x, y);
	var side = p.side;
	
	this.fromVertex = vertex;
	
	if ((p.distance-1) <= vertex.presenter.padding) {
		var start = new ArcEnd(vertex, p, NORMAL[side]);
		this.start = start;
		var end = new ArcEnd(null, new Point(p.x, p.y));
		
		this.connector = vertex.presenter.arcEnds(start, end, true);
		vertex.addArc(this.connector);
		
	} else {
		this.useMoveTool = true;
		this.moveTool.vertexOnStart(vertex, mouseX, mouseY, event);
		
	}
}

ConnectTool.prototype.vertexOnMove = function(vertex, dx, dy, mouseX, mouseY, event) {
	if (this.useMoveTool) {
		this.moveTool.vertexOnMove(vertex, dx, dy, mouseX, mouseY, event);
		return;
	}
	event.stopPropagation();

	var presenter = vertex.presenter;
	
	var point = presenter.mouseToSvg(mouseX, mouseY);
	
	var x = point.x;
	var y = point.y;
	
		
	this.connector.path.remove();
	var mouseElement = Snap.getElementByPoint(mouseX, mouseY);
	
	var otherVertex = presenter.vertexAncestor(mouseElement);
	presenter.paper.append(this.connector.path);
	var otherEnd = this.connector.otherEnd(this.start);
	
	if (otherVertex) {
		var p = otherVertex.projectOntoBoundary(x, y);
        
		otherEnd.vertex = otherVertex;
		otherEnd.normal = NORMAL[p.side];
		x = p.x;
		y = p.y;
		
	} else {
		otherEnd.normal = null;
		otherEnd.vertex = null;
	}

	this.connector.b.moveTo(x, y);
	this.connector.draw();
}

ConnectTool.prototype.vertexOnEnd = function(vertex, event) {
	if (this.useMoveTool) {
		this.moveTool.vertexOnEnd(vertex, event);
		this.useMoveTool = false;
		return;
	}

	event.stopPropagation();
	var presenter = vertex.presenter;
	
	var p = presenter.mouseToSvg(event.clientX, event.clientY);
	var x = p.x;
	var y = p.y;

	var otherVertex = null;
	
	
	var arc = this.connector;
	
	var path = arc.path;
	path.remove();
	
	
	otherVertex = presenter.vertexAncestor(
		Snap.getElementByPoint(event.clientX, event.clientY)
	);
	
	if (otherVertex) {
		arc.b.vertex = otherVertex;
		otherVertex.addArc(arc);
		vertex.presenter.paper.add(path);
		
		if (otherVertex == vertex) {
			vertex.selfArc(arc);
		}
		
	} else {
		vertex.removeArc(arc);
		return;
	}
	
	
	var router = vertex.presenter.router;
	router.reroute(vertex);
	vertex.pack();
	if (otherVertex) {
		otherVertex.pack();
	} else {
		var array = vertex.unsaveNormals();
		for (var i=0; i<array.length; i++) {
			array[i].pack();
		}
	}
	
	vertex.presenter.actionHistory.append(new AddElementAction(arc));
}

/**********************************************************************************/
/**
 *
 * @class
 * @classdesc A tool that allows one to drag out a rectangle and select all elements
 * inside or intersecting the rectangle.
 */
MultiSelectTool = function() {
}

MultiSelectTool.prototype.paperOnStart = function(presenter, mouseX, mouseY, event) {
	this.initialViewBox = presenter.viewBox().getBox();

	var point = this.initialPoint = presenter.mouseToSvg(mouseX, mouseY);
	
	this.selectionRect = presenter.paper.rect(point.x, point.y, 0, 0).addClass('konig-multiselect-rect');
	
}

MultiSelectTool.prototype.paperOnMove = function(presenter, dx, dy, mouseX, mouseY, event) {
	var point = presenter.mouseToSvg(mouseX, mouseY);
	
	
	this.selectionRect.attr({
		x: Math.min(this.initialPoint.x, point.x),
		y: Math.min(this.initialPoint.y, point.y),
		width: Math.abs(this.initialPoint.x - point.x),
		height: Math.abs(this.initialPoint.y - point.y)
	});
}

MultiSelectTool.prototype.paperOnEnd = function(presenter, event) {
	
	var selectionBBox = this.selectionRect.getBBox();

	var batch = new Batch();
	
	var handler = function(elem, i) {
		
		
		if (elem.konigVertex) {

			var elemBBox = elem.getBBox();
			if (Snap.path.isBBoxIntersect(elemBBox, selectionBBox)) {
				batch.push(elem.konigVertex);
			}
		} else if (elem.konigArc) {
			var arc = elem.konigArc;
			if (
					Snap.path.isPointInsideBBox(selectionBBox, arc.a.point.x, arc.a.point.y) ||
					Snap.path.isPointInsideBBox(selectionBBox, arc.b.point.x, arc.b.point.y)
			) {
				batch.push(arc);
			}
			
			var selectionPath = Snap.format("M{x},{y}h{width}v{height}h-{width}z", {
				x: selectionBBox.x,
				y: selectionBBox.y,
				width: selectionBBox.width,
				height: selectionBBox.height
			});
			
			var arcPath = arc.path.attr('d');
			var intersection = Snap.path.intersection(selectionPath, arcPath);
			if (intersection.length > 0) {
				batch.push(arc);
			}
		}
		
		
	};
	presenter.paper.selectAll(".konig-vertex").forEach(handler, presenter);
	presenter.paper.selectAll(".konig-arc").forEach(handler, presenter);

	this.selectionRect.remove();
	var action = new SelectPresenterElementAction(presenter, batch);
	action.commit(true);
	
}


/*********************************************************************************/
/**
 * @classdesc An action that unselects a collection of elements
 */
UnselectAction = function(presenter, element) {
	this.presenter = presenter;
	this.elementList = (element && element.list) ? element : [];
	if (element && !element.list) {
		this.add(element);
	}
}

/**
 * Add an element to this UnselectAction.  The redo method will unselect this element,
 * and the undo method will select it.
 * 
 * @param {KonigElement} element The element that will be added to this UnselectAction.
 */
UnselectAction.prototype.add = function(element) {
	if (this.elementList.indexOf(element) < 0) {
		this.elementList.push(element);
	}
}

/**
 * Unselect all the elements added to this UnselectAction.
 */
UnselectAction.prototype.redo = function() {
	this.presenter.selectionManager.batchUnselect(this.elementList);
}

/**
 * Select all the elements added to this UnselectAction
 */
UnselectAction.prototype.undo = function() {
	this.presenter.selectionManager.batchSelect(this.elementList);
}

/*********************************************************************************/
/**
 * @param {Presenter} presenter The Presenter in which the viewbox is surfaced.
 * @param {string} initialState The string value for the initial state of the viewbox.
 * @param {string} finalState The string value for the final state of the viewbox.
 */
ViewBoxChangeAction = function(presenter, initialState, finalState) {
	this.presenter = presenter;
	this.initialState = initialState;
	this.finalState = finalState;
}

/**
 * Set the viewbox to its final state.
 */
ViewBoxChangeAction.prototype.redo = function() {
	this.presenter.paper.node.setAttribute('viewBox', this.finalState);
}

/**
 * Set the viewbox to its initial state.
 */
ViewBoxChangeAction.prototype.undo = function() {
	this.presenter.paper.node.setAttribute('viewBox', this.initialState);
}


/*********************************************************************************/
/**
 * @classdesc An action that selects a collection of elements
 */
SelectPresenterElementAction = function(presenter, element) {
	this.type = "SelectPresenterElement";
	this.presenter = presenter;
	this.elementList = (element && element.list) ? element : [];
	if (element && !element.list) {
		this.add(element);
	}
}

/**
 * Start a transaction, append this action to the action history,
 * update the SelectionManager, commit the transation.
 * When the SelectionManager is updated, selection handlers will be 
 * notified, and they may add other actions to the transaction as
 * as side effect.
 * 
 */
SelectPresenterElementAction.prototype.commit = function(append) {
	var actionHistory = this.presenter.actionHistory;
	var txn = actionHistory.beginTransaction();
	actionHistory.append(this);
	this.presenter.selectionManager.batchSelect(this.elementList, append);
	actionHistory.commit(txn);
}

/**
 * Add an element to this SelectAction.  The redo method will select this element,
 * and the undo method will unselect it.
 * 
 * @param {KonigElement} element The element that will be added to this SelectAction.
 */
SelectPresenterElementAction.prototype.add = function(element) {
	if (this.elementList.indexOf(element) < 0) {
		this.elementList.push(element);
	}
}

/**
 * Select all the elements added to this SelectAction.
 */
SelectPresenterElementAction.prototype.redo = function() {
	this.presenter.selectionManager.batchSelect(this.elementList);
}

/**
 * Unselect all the elements added to this SelectAction
 */
SelectPresenterElementAction.prototype.undo = function() {
	this.presenter.selectionManager.batchUnselect(this.elementList);
}

/*********************************************************************************/
PanTool = function() {}

PanTool.prototype.paperOnStart = function(presenter, mouseX, mouseY, event) {

	this.initialViewBox = presenter.viewBox().getBox();
	presenter.paper.addClass('grabbing');
}


PanTool.prototype.paperOnMove = function(presenter, dx, dy, mouseX, mouseY, event) {
	
	var box0 = this.initialViewBox;
	
	if (box0) {
		var box1 = {
			x: box0.x - dx,
			y: box0.y - dy,
			width: box0.width,
			height: box0.height
		};
		viewBox = presenter.viewBox();
		viewBox.setBox(box1);
	}
	
}

PanTool.prototype.paperOnEnd = function(presenter, event) {
	presenter.paper.removeClass('grabbing');
	
	if (this.initialViewBox) {
		var initialState = this.initialViewBox.toViewBoxState();
		var finalState = presenter.paper.node.getAttribute('viewBox');
		
		var action = new ViewBoxChangeAction(presenter, initialState, finalState);
		presenter.actionHistory.append(action);
		this.initialViewBox = null;
	}
	
}

/*********************************************************************************/
MoveTool = function(selectTool) {
	this.selectTool = selectTool || new SelectTool();
}

MoveTool.prototype.vertexOnStart = function(vertex, mouseX, mouseY, event) {
	event.stopPropagation();

	var point = vertex.presenter.mouseToSvg(mouseX, mouseY);
	var x = point.x;
	var y = point.y;

	var bbox = vertex.getBBox();
	
	this.priorPoint = point;
	this.initialMousePoint = point;
	this.initialLocation = new Point(bbox.x, bbox.y);

	// TODO: Document purpose of saveNormals
	vertex.saveNormals();
}

MoveTool.prototype.vertexOnMove = function(vertex, dx, dy, mouseX, mouseY, event) {

	event.stopPropagation();

	var presenter = vertex.presenter;
	
	var point = presenter.mouseToSvg(mouseX, mouseY);
	
	var x = point.x;
	var y = point.y;
	
	var dx = x - this.priorPoint.x;
	var dy = y - this.priorPoint.y;
	
	var batch = presenter.selectionManager.selection;
	
	if (batch.length > 1 && batch.contains(vertex)) {
		this.vertexBatchMove(vertex, dx, dy, batch);
	} else {

		var delta = new Snap.Matrix();
		delta.translate(dx, dy);
		var matrix = vertex.snapElement.transform().localMatrix.add(delta);
		vertex.snapElement.transform(matrix);
		
		vertex.presenter.router.reroute(vertex);
	}

	this.priorPoint.moveTo(x, y);
	
	
}

MoveTool.prototype.vertexBatchMove = function(vertex, dx, dy, batch) {
	var map = {};
	
	for (var i=0; i<batch.length; i++) {
		var e = batch[i];
		if (konig.isVertexView(e)) {
			map[e.id] = e;
		}
	}

	var delta = new Snap.Matrix();
	delta.translate(dx, dy);
	for (var i=0; i<batch.length; i++) {
		var e = batch[i];
		if (konig.isVertexView(e)) {
			var matrix = e.snapElement.transform().localMatrix.add(delta);
			e.snapElement.transform(matrix);
			var arcList = e.arcList;
			for (var j=0; j<arcList.length; j++) {
				var arc = arcList[j];
				var otherEnd = arc.otherEnd(e);
				if (otherEnd) {
					v = otherEnd.vertex;
					map[v.id] = v;
				}
			}
		}
	}

	var router = vertex.presenter.router;
	var routerMap = {};
	
	for (var key in map) {
		var v = map[key];
		router.rerouteVertex(v, routerMap);
	}
	
	

	for (var key in routerMap) {
		var arc = routerMap[key];
		arc.draw();
	}
	
}

MoveTool.prototype.vertexOnEnd = function(vertex, event) {
	
	event.stopPropagation();
	var presenter = vertex.presenter;
	
	var p = presenter.mouseToSvg(event.clientX, event.clientY);
	var x = p.x;
	var y = p.y;

	var otherVertex = null;
	
	// MOVE
	var bbox = vertex.getBBox();
	var finalPoint = new Point(bbox.x, bbox.y);
	
	var distance = finalPoint.distanceTo(this.initialLocation)
	if (distance > 0) {
		this.vertexEndMove = true;
		var action = new MoveVertexAction(vertex, this.initialLocation, finalPoint);
		vertex.presenter.actionHistory.append(action);
		this.finalLocation = finalPoint;
		
//		var router = vertex.presenter.router;
//		router.reroute(vertex);
//		vertex.pack();
//		if (otherVertex) {
//			otherVertex.pack();
//		} else {
//			var array = vertex.unsaveNormals();
//			for (var i=0; i<array.length; i++) {
//				array[i].pack();
//			}
//		}
		
	} else {
		this.selectTool.vertexOnClick(vertex, event);
	}
		
	
	
}

/**********************************************************************************/

SelectTool = function() {}

/**
 * Select the arc that was clicked.
 * @param {ArcView} arc The arc that was clicked. 
 * @param {Event} event The DOM click event.
 */
SelectTool.prototype.arcOnClick = function(arc, event) {
	var presenter = arc.a.vertex.presenter;
	var action = new SelectPresenterElementAction(presenter, arc);
	action.commit();
}

SelectTool.prototype.paperOnClick = function(presenter, event) {

	
	var top = presenter.getKonigElementByPoint(event.clientX, event.clientY);
	if (top) {
		var action = new SelectPresenterElementAction(presenter, top);
		action.commit();
		
	} else {
		var batch = presenter.selectionManager.selection.clone();
		presenter.actionHistory.append(new UnselectAction(presenter, batch));
		presenter.selectionManager.unselectAll();
	}
}

SelectTool.prototype.vertexOnClick = function(vertex, event) {
	event.stopPropagation();
	var presenter = vertex.presenter;
	
	var action = new SelectPresenterElementAction(presenter, vertex);
	action.commit();
}

OneTool = function() {
	this.selectTool = new SelectTool();
}

OneTool.prototype.paperOnClick = function(presenter, event) {

	if (this.vertexEndMove || this.vertexEndConnect) {
		this.vertexEndMove = false;
		this.vertexEndConnect = false;
		return;
	}
	
	this.selectTool.paperOnClick(presenter, event);
}

OneTool.prototype.vertexOnMove = function(vertex, dx, dy, mouseX, mouseY, event) {

	event.stopPropagation();

	var presenter = vertex.presenter;
	
	var point = presenter.mouseToSvg(mouseX, mouseY);
	
	var x = point.x;
	var y = point.y;
	
	var ctx = vertex.presenter.dragContext;
	switch (ctx.dragAction) {
	case CONNECT :

		
		ctx.connector.path.remove();
		var mouseElement = Snap.getElementByPoint(mouseX, mouseY);
		
		var otherVertex = presenter.vertexAncestor(mouseElement);
		presenter.paper.append(ctx.connector.path);
		var otherEnd = ctx.connector.otherEnd(ctx.start);
		
		if (otherVertex) {
			var p = otherVertex.projectOntoBoundary(x, y);
            
			otherEnd.vertex = otherVertex;
			otherEnd.normal = NORMAL[p.side];
			x = p.x;
			y = p.y;
			
		} else {
			otherEnd.normal = null;
			otherEnd.vertex = null;
		}

		ctx.connector.b.moveTo(x, y);
		ctx.connector.draw();
		
		break;
		
	case MOVE :
		var dx = x - ctx.priorPoint.x;
		var dy = y - ctx.priorPoint.y;
		
		var delta = new Snap.Matrix();
		delta.translate(dx, dy);
		var matrix = vertex.snapElement.transform().localMatrix.add(delta);
		vertex.snapElement.transform(matrix);
		ctx.priorPoint.moveTo(x, y);
		
		
		vertex.presenter.router.reroute(vertex);
	}
}

OneTool.prototype.paperOnEnd = function(presenter, event) {
	presenter.paper.removeClass('grabbing');
}

OneTool.prototype.paperOnStart = function(presenter, x, y, event) {
	
	presenter.dragContext = {initialViewBox: presenter.viewBox().getBox()};
	presenter.paper.addClass('grabbing');
}

OneTool.prototype.paperOnMove = function(presenter, dx, dy, x, y, event) {
	if (!presenter.dragContext || !presenter.dragContext.initialViewBox) {
		return;
	}
	
	var box0 = presenter.dragContext.initialViewBox;
	
	var box1 = {
		x: box0.x - dx,
		y: box0.y - dy,
		width: box0.width,
		height: box0.height
	};
	var viewBox = presenter.viewBox();
	viewBox.setBox(box1);
}

OneTool.prototype.vertexOnEnd = function(vertex, event) {

	
	event.stopPropagation();
	var presenter = vertex.presenter;
	
	var p = presenter.mouseToSvg(event.clientX, event.clientY);
	var x = p.x;
	var y = p.y;

	var ctx = presenter.dragContext;
	var otherVertex = null;
	
	if (ctx.dragAction == CONNECT) {
		ctx.dragAction = 0;
		this.vertexEndConnect = true;
		var arc = ctx.connector;
		
		var path = arc.path;
		path.remove();
		
		
		otherVertex = presenter.vertexAncestor(
			Snap.getElementByPoint(event.clientX, event.clientY)
		);
		
		if (otherVertex) {
			arc.b.vertex = otherVertex;
			otherVertex.addArc(arc);
			vertex.presenter.paper.add(path);
			
			if (otherVertex == vertex) {
				vertex.selfArc(arc);
			}
			
		} else {
			vertex.removeArc(arc);
			return;
		}
	} else {
		// MOVE
		var bbox = vertex.getBBox();
		var finalPoint = new Point(bbox.x, bbox.y);
		
		var distance = finalPoint.distanceTo(ctx.initialLocation)
		if (distance > 0) {
			this.vertexEndMove = true;
			var action = new MoveVertexAction(vertex, ctx.initialLocation, finalPoint);
			vertex.presenter.actionHistory.append(action);
			ctx.finalLocation = finalPoint;
		}
		
	}
	
	var router = vertex.presenter.router;
	router.reroute(vertex);
	vertex.pack();
	if (otherVertex) {
		otherVertex.pack();
	} else {
		var array = vertex.unsaveNormals();
		for (var i=0; i<array.length; i++) {
			array[i].pack();
		}
	}
}

OneTool.prototype.vertexOnStart = function(vertex, x, y, event) {
	event.stopPropagation();

	var point = vertex.presenter.mouseToSvg(x, y);
	x = point.x;
	y = point.y;

	var p = vertex.projectOntoBoundary(x, y);
	var side = p.side;
	var ctx = vertex.presenter.dragContext = {};
	ctx.fromVertex = vertex;
	
	if ((p.distance-1) <= vertex.presenter.padding) {
		ctx.dragAction = CONNECT;
		var start = new ArcEnd(vertex, p, NORMAL[side]);
		ctx.start = start;
		var end = new ArcEnd(null, new Point(p.x, p.y));
		
		ctx.connector = vertex.presenter.arcEnds(start, end);
		vertex.addArc(ctx.connector);
		
	} else {
		ctx.dragAction = MOVE;
		var bbox = vertex.getBBox();
		
		ctx.priorPoint = new Point(x, y);
		ctx.initialMousePoint = ctx.priorPoint;
		ctx.initialLocation = new Point(bbox.x, bbox.y);
		vertex.saveNormals();
		
	}
}

HandlerContext = function(handler, context) {
	this.handler = handler;
	this.context = (context) ? context : null;
}

if (typeof(konig) === "undefined") {
	konig = {};
}

konig.presenter = new Presenter();
konig.eventManager = konig.presenter;
konig.Point = Point;
konig.Segment = Segment;
konig.Box = Box;
konig.Rectangle = Rectangle;
konig.sideName = sideName;
konig.dist2 = dist2;
konig.distance = distance;

konig.isArcView = function(element) {
	return (element instanceof ArcView);
}

konig.isVertexView = function(element) {
	return (element instanceof VertexView);
}

konig.geometry = geom;



});