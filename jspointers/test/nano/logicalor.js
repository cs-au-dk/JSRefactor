function Bar() {}
function Baz() {}
function Foo(x) {
	return x || Bar;
}

var x = Foo(Baz);
var y = Foo();

x();
y();

