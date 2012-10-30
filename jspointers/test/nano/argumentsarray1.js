function Foo() {
	return arguments[0];
}
function Bar() {}

var x = Foo(Bar);
x();
