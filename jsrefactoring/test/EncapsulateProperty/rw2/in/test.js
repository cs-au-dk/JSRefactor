function Rectangle(w, h) {
    this.width /* encapsulate */ = w;
    this.height = h;
}
Rectangle.prototype.grow = function(dw, dh) {
    this.width += dw;
    this.height += dh;
};

var r = new Rectangle(23, 42);
r.grow(19, 14);
