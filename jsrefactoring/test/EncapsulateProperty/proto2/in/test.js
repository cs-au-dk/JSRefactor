function A(x) {
	this.v /* encapsulate */ = x;
	this.m = function() {
		return this.v;
	}
}

var a = new A(5);
var b = Object.create(a);
b.v = 9;
var z = b.m();

