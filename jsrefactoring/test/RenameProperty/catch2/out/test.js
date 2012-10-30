function f(x) {
    try {
	throw 42;
    } catch(y) {
	return function() {
	    return y /* -> y */;
	}
    }
}

f(23)();
