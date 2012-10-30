function Rectangle(w, h) {
    var width;
    width /* encapsulate */ = w;
    this.height = h;
    this.getWidth = function() {
	return width;
    };
}

var r = new Rectangle(23, 42);
var w = r.getWidth();