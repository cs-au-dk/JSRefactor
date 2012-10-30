function Rectangle(w, h) {
    this.width = w;
    this.height = h;
    this.area = /* move to prototype */ function() {
	return this.width * this.height;
    };
}

Rectangle(56, 72);
var a2 = this.area();
