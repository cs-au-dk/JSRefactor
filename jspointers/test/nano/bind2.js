
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

var f1 = Function.prototype.bind.apply(Foo, [receiver,A,B]);
f1(C);



