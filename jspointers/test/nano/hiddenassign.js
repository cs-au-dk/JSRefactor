var o = {};

function Nothing1() {
}
function Nothing2() {
	return;
}
function Foo() {
}

o.a = Nothing1();
o.b = Nothing2();

with (o) {
	a = Foo;
	b = Foo;
}

// both of these will call Foo
o.a();
o.b();
