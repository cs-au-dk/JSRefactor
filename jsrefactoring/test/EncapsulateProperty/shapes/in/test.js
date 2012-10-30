function Rectangle(w, h) {
    this.width /* encapsulate */ = w;
    this.height = h;
    this.area = function() { return this.width * this.height; };
}

function Square(l) {
    Rectangle.call(this, l, l);
}
Square.prototype = new Rectangle;

var s = new Square(3);
s.area();
alert(s.width);
