/* initialiser */ function Shape() { }

function Rectangle(w, h) {
    Shape.call(this);
    this.width = w;
    this.height = h;
}
Rectangle.prototype = new Shape();

function Circle(r) {
    Shape.call(this);
    this.radius = r;
}
Circle.prototype = new Shape();

var r = new Rectangle(23, 42);
var c = new Circle(56);


