function Rectangle(w, h) {
    var width;
    this.getWidth = function() {
        return width;
    };
    this.setWidth = function(newWidth) {
        return width = newWidth;
    };
    width /* encapsulate */ = w;
    this.height = h;
}
Rectangle.prototype.grow = function(dw, dh) {
    this.setWidth(this.getWidth()+dw);
    this.height += dh;
};

var r = new Rectangle(23, 42);
r.grow(19, 14);
