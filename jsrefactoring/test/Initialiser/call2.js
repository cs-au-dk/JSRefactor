/* not initialiser */ function A(v) {
    this.x = v;
}

var o = { x: 23 };
Function.prototype.call.call(A, o, 42);
