function A(v) {
    this.x /* encapsulate */ = v;
    this.getX = function() { return x; };
    this.setX = function(newX) { return this.x = newX; };
}

function clone(o) {
    var clone = Object.create(o.constructor);
    for(p in o)
	clone[p] = o[p];
    return clone;
}

var a = new A(23);
var a_clone = clone(a);
a_clone.setX(42);
alert(a.getX());
