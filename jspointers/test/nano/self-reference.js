var x = function f(y) {
	if (y)
		return f(false);
	else
		return 5;
}

var z = x(true);
