function A() {
    this.f = /* move to prototype */ function() { };
}

var a1 = new A();
var a2 = new A();
var f1 = new a1.f();
var f2 = new a2.f();

f1.constructor.x = 23;

print(f2.constructor.x);