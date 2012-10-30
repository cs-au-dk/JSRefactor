function g() {return 1;}
function f() {
	var y;
	try {
		y = g();
	} catch (x) {
		y = x /* -> z */
	}
	return y;
}
f();