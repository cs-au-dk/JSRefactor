function Rectangle(w, h) {
    this.width = w;
    this.height = h;
    this.area = /* well-scoped */ function() {
	return this.width * this.height;
    };
}

var r = new Rectangle(23, 42);
alert(r.area());
