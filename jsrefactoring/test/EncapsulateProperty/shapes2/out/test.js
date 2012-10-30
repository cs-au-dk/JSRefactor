function Rectangle(w, h) {
    var height;
    this.getHeight = function() {
        return height;
    };
    this.width = w;
    height /* encapsulate */ = h;
}
Rectangle.prototype.getArea = function() {
    return this.width * this.getHeight();
};
Rectangle.prototype.toString = function() {
    return "Rectangle(width=" + this.width + ", height=" + this.getHeight() + ")"; 
};

function Square(l) {
    Rectangle.call(this, l, l);
    this.length = l;
}
Square.prototype = Object.create(Rectangle.prototype);
Square.prototype.toString = function() {
    return "Square(length=" + this.length + ")";
};

var s = new Square(3);
alert("Area of " + s + ": " + s.getArea());
