function Rectangle(w, h) {
    this.width = w;
    this.height = h;
    this.getWidth = /* move to prototype */ function() {
	return this.width;
    };
}
Rectangle.prototype = {
    area: function() {
	return this.width * this.height;
    }
};

var r = new Rectangle(23, 42);
