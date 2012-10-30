function Bar() {
	
}
function Foo() {
	this.foo = Bar;
	this.foo;
}

var obj = {foo:Foo};

obj.foo();
obj.foo();

delete obj.foo;

