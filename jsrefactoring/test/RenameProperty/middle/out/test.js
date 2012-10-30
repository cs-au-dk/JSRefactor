function Point(x, y) {
	this.x = x;
	this.y = y;
}

function Circle(center, radius) {
	this.CENTER = center;
	this.radius = radius;
}

var p = new Point(2, 3);
var c = new Circle(p, 5);

var foo = c.CENTER.x;
