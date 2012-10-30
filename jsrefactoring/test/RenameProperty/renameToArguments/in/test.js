function f(x) {
	return function g() {
		return x /* -> arguments */;
	}
}