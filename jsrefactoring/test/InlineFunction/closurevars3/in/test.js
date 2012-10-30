function f() {
	function a() {
		return 1;
	}
	function b() {
		return a();
	}
	function c() {
		return b() /* inline */;
	}
	return c();
}

f();