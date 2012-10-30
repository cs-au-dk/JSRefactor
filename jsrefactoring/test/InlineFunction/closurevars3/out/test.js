function f() {
	function a() {
		return 1;
	}
	function b() {
		return a();
	}
	function c() {
		return (function() {
return a();})() /* inline */;
	}
	return c();
}

f();
