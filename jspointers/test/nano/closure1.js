function f() {
	function a() {
		return 1;
	}
	function b() {
		return a();
	}
	function c() {
		return b();
	}
	return c();
}

f();