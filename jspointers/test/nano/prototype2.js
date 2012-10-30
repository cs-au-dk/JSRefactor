function Foo(x) {
	this.x = x;
}
Foo.prototype.bar = function () {
	return 45;
}
var foo = new Foo();
var z = foo.bar();
