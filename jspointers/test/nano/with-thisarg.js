function X() {}

function Foo() {return X;}

function CallFoo() {
	// this can call Foo, because this=obj
	return this.foo();
}

function Bar(x) {
	with (x) {
		// send 'x' as this argument
		return func();
	}
}

var obj = {func:CallFoo, foo:Foo};
var result = Bar(obj);

result(); // can call X


