function Rectangle(w, h) {
    var width;
    this.getWidth = function() {
        return width;
    };
    width /* encapsulate */ = w;
    this.height = h;
    this.area = function() { return width * this.height; };
}

var r = new Rectangle(23, 42);
var a = r.area();
alert(r.getWidth());