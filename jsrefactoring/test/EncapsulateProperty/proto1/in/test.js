function Rectangle(w, h) {
    this.width /* encapsulate */ = w;
    this.height = h;
}

function Square(s) {
    Rectangle.call(this, s, s);
}
Square.prototype = new Rectangle;

var r = new Square(23);
var w = r.width;
