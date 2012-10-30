function Unreachable(obj) {
	obj.x = 5;
	return obj.y;
}

var a = {};
a.x = 3;

function Foo(obj) {
	return obj.x;
}

Foo(a);
