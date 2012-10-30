
function Foo(a,b,c) {
	this.x();
	a();
	b();
	c();
}

function A() {}
function B() {}
function C() {}
function X() {}

var receiver = {x:X};

var f1 = Foo.bind(receiver,A);
f1(B,C);

