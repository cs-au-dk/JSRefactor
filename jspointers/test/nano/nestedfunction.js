function Outer(arg) {
	function Inner() {}
	Inner.prototype = arg;
	var x = new Inner();
	return x;
}

var z = {};
var x = Outer(z);

z.baz = function() {}
x.baz(); // should succeed

x.bong = function() {}
z.bong(); // should fail

