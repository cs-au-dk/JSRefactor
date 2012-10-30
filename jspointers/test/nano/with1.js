function Foo1() {}
function Foo2() {}


function Bar() {
	var obj = {};

	obj.foo = Foo1;
	obj.bar = Foo1;
	var foo = Foo2;
	with (obj) {
		foo();
	}
}

Bar();
