function One() {}
function Two() {}
function Three() {}

function foo(x) {
	var obj = new Object();
	var z;
	var w;
	w = x;
	function bar() {
		z = One;
	}
	function baz() {
		obj.b = z;
	}
	bar();
	baz();
	obj.a = w;
	return obj;
}
var x = foo(Two);
var y = foo(Three);

x.a();
x.b();

y.a();
y.b();

