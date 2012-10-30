var x = 19;
function A() {
    this.x /* encapsulate */ = 23;
    this.m = function() {
	return this.x + x;
    };
}

var a = new A();
a.m();
