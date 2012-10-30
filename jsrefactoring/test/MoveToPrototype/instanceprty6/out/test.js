function A() {
}
A.prototype.x = /* move to prototype */ 23;

var a1 = new A(),
    a2 = new A();
a1.x = {};
a1.x.f = 42;
