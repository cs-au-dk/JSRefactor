function Foo() {
	var foo = Baz;
	function Bar() {
		foo();
	}
	Bar();
}
function Baz() {
	this.ptr = TestTarget;
}
function TestTarget() {}

Foo();

// ptr() will call TestTarget
ptr();
