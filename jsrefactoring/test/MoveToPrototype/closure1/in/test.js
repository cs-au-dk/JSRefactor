function Rectangle(w, h) {
    this.getArea = /* move to prototype */ function() {
	return w*h;
    };
}

var r = new Rectangle(23, 42);
var a = r.getArea();
