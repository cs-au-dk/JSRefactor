/* not initialiser */ function A(v) {
    this.x = v;
}

var a = new A(23);
var aA = A.bind(a);
aA(42);
