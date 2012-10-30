function f(x) {
	function a() {
		return x;
	}
	function b() {
		return a();
	}
	return b;
}
function X() {}
var x = f(X);
var y = x();
y();
