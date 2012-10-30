function Super() {
    this.x = 42;
}

function A() {
    this.x = /* move to prototype */ 23;
}
A.prototype = new Super();

var a1 = new A();
var a2 = Object.create(A.prototype);
var x = a2.x;