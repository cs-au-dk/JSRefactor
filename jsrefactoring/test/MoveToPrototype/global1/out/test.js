function Rectangle(w, h) {
    this.width = w;
    this.height = h;
}
Rectangle.prototype.area = /* move to prototype */ function() {
    return this.width * this.height;
};

var r = new Rectangle(23, 42);
var a = r.area();
Rectangle(56, 72);
