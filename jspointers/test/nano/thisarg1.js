function Foo() {
	return this.x;
}
function Bar() {
}
function Baz() {
}

var barptr = {foo:Foo, x:Bar};
var bar = barptr.foo();
bar();

var bazptr = {foo:Foo, x:Baz};
var baz = bazptr.foo();
baz();
