function setF(o, x) {
    o.f = x;
}

function A(v, o) {
    this.x = v;
    this.f = /* not well-scoped */ function() {
        return this.x;
    };
    if(o)
        setF(o, this.f);
}

var a1 = new A(23);
var a2 = new A(42, a1);
