function A() {
    this.getX = function() { return 42; };
}

function B() {
    var x;
    x /* encapsulate */ = 23;
}
B.prototype = new A();

var b = new B();
alert(b.getX());
