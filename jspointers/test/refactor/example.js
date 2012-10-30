
// rename foo to x
function Point(foo,y) {
	this.foo = foo;
	this.y = y;
}

var p = new Point(4,6);

var f = p.foo;

Point.prototype.foo = 3;

var p2 = new Point(1,1);
var z = p2;
z.foo = p.foo;

delete p2.foo;

var obj = {foo: 34};
if (Math.random() < 0.5) {
	obj = p2;
}
obj.foo = 6;


