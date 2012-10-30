function T1() {}
function T2() {}
function T3() {}
function Choose(x,y) {
	if (Math.random() < 0.5) {
		return x;
	} else {
		return y;
	}
}
function Foo(a,b) {
	a.foo = b;
	b.foo.bar = a;
}
var o1 = new T1();
var o2 = new T2();
var o3 = new T3();
var x = Choose(o1,o2);
var y = Choose(o2,o3);

Foo(x,y);

var xfoo = x.foo;
var yfoo = y.foo;
var xbar = x.bar;
var ybar = y.bar;
