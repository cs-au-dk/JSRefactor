function Rectangle(w, h) {
    this.width = w;
    this.height = h;
    this.area = /* not well-scoped */ function() {
	return this.width * this.height;
    };
}

var r1 = new Rectangle(23, 42),
    r2 = new Rectangle(56, 72);
r1.area = r2.area;
alert(r1.area());
