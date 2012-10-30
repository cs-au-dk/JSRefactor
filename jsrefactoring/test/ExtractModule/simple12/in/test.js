/* extract module Geometry { */
function Rectangle(w, h) {
    this.width = w;
    this.height = h;
}

function Square(s) {
    Rectangle.call(this, s, s);
}
Square.prototype = new Rectangle;
/* } */
var r = new Rectangle(23, 42);
var s = new Square(56);
