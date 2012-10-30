// must fail because new variable name clashes with existing property
function f(o) {
	var abc = 4;
	with (o) {
		abc /* -> foo */ = 7;
	}
	return abc;
}

var obj = {foo:5};
f(obj);
