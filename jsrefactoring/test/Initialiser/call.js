/* not initialiser */ function A(v) {
    this.x = v;
}

var o = { x: 23 };
A.call(o, 42);
