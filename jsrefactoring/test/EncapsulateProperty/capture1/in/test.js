function A() {
    this.x /* encapsulate */ = 23;
}

function B() {
    this.getX = function() { return 42; };
}
B.prototype = new A();

var b = new B();
alert(b.x);
