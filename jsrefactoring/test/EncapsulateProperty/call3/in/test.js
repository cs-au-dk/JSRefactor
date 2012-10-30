function A() {
    this.x /* encapsulate */ = 23;
    this.getX = function() {
	return this.x;
    };
}

var a = new A();
var b = Object.create(a);
b.x = 42;
b.getX();
