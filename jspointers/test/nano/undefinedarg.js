function Foo() {}
function f() {
	var a,b,c;
	a = 1;
	c = Foo;
	g.call(null,a,b,c);
}
function g(x,y,z) {
	z();
}
f();
