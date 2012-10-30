function f(o) {
	var x = 5 + o.x;
	function g(X) {
		return X /* -> X */;
	}
	return g;
}
var x = 5;
var o = {x:6};
o.x = 3;
f(o)(3);
