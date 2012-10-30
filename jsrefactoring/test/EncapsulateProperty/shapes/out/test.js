function Rectangle(w, h) {
    var width;
    this.getWidth = function() {
        return width;
    };
    width /* encapsulate */ = w;
    this.height = h;
    this.area = function() { return this.getWidth() * this.height; };
}

function Square(l) {
    Rectangle.call(this, l, l);
}
Square.prototype = new Rectangle;

var s = new Square(3);
s.area();
alert(s.getWidth());
