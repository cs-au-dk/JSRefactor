function Rectangle(w, h) {
    this.width /* encapsulate */ = w;
    this.height = h;
    this.area = function() { return this.width * this.height; };
}

var r = new Rectangle(23, 42);
var a = r.area();