/* not initialiser */ function A(v) {
    B.call(this, v);
}

function B(v) {
    A.call(this, v);
}

var a = new A(23);
