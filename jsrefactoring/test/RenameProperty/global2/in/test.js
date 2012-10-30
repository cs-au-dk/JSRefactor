function f() {
	return this.x /* -> X */;
}

var x = 5;
var y = f();

function g() {
	return x;
}
function h(x) {
	return x;
}

var z = g() + h(5);