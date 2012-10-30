
function Foo() {
	
}

var x = {};
var n = Math.floor(6);
x[n] = Foo;

x[6]();
