/* not initialiser */ function Rectangle(w, h) {
    this.width = w;
    this.height = h;
}
Rectangle.prototype.getArea = function() {
    return this.width * this.height;
};

// we don't support this pattern
function Square(s) {
    this.superCtor = Rectangle;
    this.superCtor(this, s, s);
}
Square.prototype = new Rectangle;

var r = new Square(3);

