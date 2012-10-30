function f() {
	return this.X /* -> X */;
}

var X = 5;
var y = f();

function g() {
	return X;
}
function h(x) {
	return x;
}

var z = g() + h(5);