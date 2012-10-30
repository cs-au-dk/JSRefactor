/* initialiser */ function Rectangle(w, h) {
    this.width = w;
    this.height = h;
}
Rectangle.prototype.getArea = function() {
    return this.width * this.height;
};

function Square(s) {
    Rectangle.apply(this, [s, s]);
}
Square.prototype = new Rectangle;

var r = new Square(3);

