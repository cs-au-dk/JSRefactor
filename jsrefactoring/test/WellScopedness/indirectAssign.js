function Rectangle(w, h) {
    this.width = w;
    this.height = h;
    this.area = /* not well-scoped */ function() {
	return this.width * this.height;
    };
}

function foo(r, m) {
    r.area = m;
}

function bar(f, m) {
    f(new Rectangle(23, 42), m);
}

bar(foo, new Rectangle(56, 72).area);
