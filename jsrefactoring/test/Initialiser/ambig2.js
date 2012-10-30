/* initialiser */ function A(v) {
    this.x = v;
}

function B(w) {
    this.y = w+19;
}

function mk(ctor, arg) {
    return new ctor(arg);
}

var a = mk(A, 23);
