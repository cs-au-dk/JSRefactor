/* initialiser */ function Rectangle(w, h) {
    this.width = w;
    this.height = h;
}
Rectangle.prototype.getArea = function() {
    return this.width * this.height;
};

var r = new Rectangle(23, 42);
