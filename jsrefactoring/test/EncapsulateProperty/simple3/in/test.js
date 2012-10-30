function Rectangle(w, h) {
    this.width /* encapsulate */ = w;
    this.height = h;
    this.getWidth = function() {
	return this.width;
    };
}

var r = new Rectangle(23, 42);
var w = r.getWidth();