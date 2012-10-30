function g() {return 1;}
function f() {
	var y;
	try {
		y = g();
	} catch (z) {
		y = z /* -> z */
	}
	return y;
}
f();