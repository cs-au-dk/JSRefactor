function Rectangle(w, h) {
    this.width = w;
    this.height = h;
}
Rectangle.prototype = {
    area: function() {
	return this.width * this.height;
    }
};
Rectangle.prototype.getWidth = /* move to prototype */ function() {
    return this.width;
}

var r = new Rectangle(23, 42);
