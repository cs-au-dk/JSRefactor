function A(v) {
    this.x = v;
    this.getX = /* not well-scoped */ function() {
        return this.x;
    };
}

var a = new A(42);
window.addEventListener("load", a.getX, true);