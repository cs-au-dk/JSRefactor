function Foo(x) {
	this.x = x;
}
Foo.prototype.getX = function () {
	return this.x;
}
Foo.prototype.getY = function() {
	return this.getX().abc;
}
var w = { abc: function() { return 45; } };
var foo = new Foo(w);
var z = foo.getX();
var zz = foo.getY();
var f = zz();

var abcresult = w.abc();
