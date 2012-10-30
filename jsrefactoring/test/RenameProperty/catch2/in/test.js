function f(x) {
    try {
	throw 42;
    } catch(x) {
	return function() {
	    return x /* -> y */;
	}
    }
}

f(23)();
