function Point(w, h) {
    this.width = /* move to prototype */ w;
    this.height = h;
    this.area = function() {
	return this.width * this.height;
    };
}

var p = new Point(23, 42);
