function Foo() {}
var w;
var obj = {
	get foo() {
		w = Foo;
		return Foo;
	}
};

var z = obj.foo;
z();


w();
