var Geometry = (function() {
    function Rectangle(w, h) {
        this.width = w;
        this.height = h;
    }
    function Square(s) {
        Rectangle.call(this, s, s);
    }
    Square.prototype = new Rectangle;
    return {
        Rectangle: Rectangle,
        Square: Square
    };
})();
/* extract module Geometry { */

/* } */
var r = new Geometry.Rectangle(23, 42);
var s = new Geometry.Square(56);
