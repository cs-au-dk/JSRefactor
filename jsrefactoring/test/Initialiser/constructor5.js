/* initialiser */ function Shape() { }

function Rectangle(w, h) {
    this.width = w;
    this.height = h;
}
Rectangle.prototype = new Shape();

function Circle(r) {
    this.radius = r;
}
Circle.prototype = new Shape();

var r = new Rectangle(23, 42);
var c = new Circle(56);


