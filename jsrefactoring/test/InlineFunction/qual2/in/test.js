function Rectangle(w, h) {
    this.width = w;
    this.height = h;
    this.getWidth = function() {
	return this.width;
    };
    this.getHeight = function() {
	return this.height;
    };
}

var r = new Rectangle(23, 42);
alert(r.getWidth() /* inline */);
