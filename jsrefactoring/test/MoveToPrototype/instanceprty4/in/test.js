function A() {
    this.x = /* move to prototype */ { f: 42; };
}

var a1 = new A();
var a2 = new A();
a1.x.f = 23;
print(a2.x.f);