function Rectangle(w, h) {
    var width;
    width /* encapsulate */ = w;
    this.height = h;
    this.area = function() { return width * this.height; };
}

var r = new Rectangle(23, 42);
var a = r.area();