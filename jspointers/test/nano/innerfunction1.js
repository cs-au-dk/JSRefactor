function Foo(x) {
	return function(y) {
		x.prty = y;
	}
}
function F1() {}
function F2() {}

var obj1 = {};
var obj2 = {};

var obj1setter = Foo(obj1);
var obj2setter = Foo(obj2);

obj1setter(F1);
obj2setter(F2);

var a = obj1.prty;
var b = obj2.prty;

a();
b();