function A() {
    this.x /* encapsulate */ = 23;
    this.m = function(x) {
	return this.x + x;
    };
}

var a = new A();
a.m();
