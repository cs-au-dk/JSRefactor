// try to force non-termination in case object-sensitive lattice has infinite height

function Foo() {
	return {foo:Foo};
}
var x = Foo();
while (true) {
	x = x.foo();
}
