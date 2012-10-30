function A(g) {
	this.x = 6;
	if (g) {
		this.f = g;
	} else {
		this.f = /* not well-scoped */ function() {
			return this.x;
		};
	}
}

var a = new A();
var b = new A(a.f);

var x1 = a.f();
var x2 = b.f();

