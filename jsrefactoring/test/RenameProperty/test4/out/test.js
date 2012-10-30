function Ellipse(f1, f2, a) {
    this.f1 = f1;
    this.f2 = f2;
    this.a = a;
}

function Circle(c, r) {
    Ellipse.call(this, c, c, r);
    this.c = c;
    this.r = r;
    this.area /* -> area */ = function() {
	return Math.PI*this.r*this.r;
    };
}
Circle.prototype = new Ellipse;

function Square(o, l) {
    this.o = o;
    this.l = l;
    this.area = function() {
	return this.l*this.l;
    };
}

function totalArea(shapes) {
    return shapes.reduce(function (area, shape) {
	return area + shape.area();
    }, 0);
}

var c = new Circle(1, 1, 23);
var s = new Square(2, 2, 42);
alert(totalArea([c, s]));
var a = s.a;