function Foo() {}

Error.prototype.foo = Foo;

var z;
try {
	var x = Foo();
	throw new Error();
} catch (error) {
	z = error;
}

z.foo();


