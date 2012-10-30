function A(v, g) {
    this.x /* encapsulate */ = v;
    if (g) {
        this.f = g;
    } else {
        this.f = function() {
            return this.x;
        };
    }
}

var a = new A(23);
var b = new A(42, a.f);

var x1 = a.f(); // 23
var x2 = b.f(); // 42