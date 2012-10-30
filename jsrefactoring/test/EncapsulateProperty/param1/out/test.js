function A(v, g) {
    var x;
    this.getX = function() {
        return x;
    };
    x /* encapsulate */ = v;
    if (g) {
        this.f = g;
    } else {
        this.f = function() {
            return this.getX();
        };
    }
}

var a = new A(23);
var b = new A(42, a.f);

var x1 = a.f(); // 23
var x2 = b.f(); // 42