/* not initialiser */ function A(v) {
    this.x = v;
}

var a = new A(23);
a.f = A;
a.f(42);
