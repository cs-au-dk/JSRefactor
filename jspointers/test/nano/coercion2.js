String.prototype.foo = function() {
	return this;
}

function Bar() {}

var x = "foo".foo();

x.bar = Bar;
x.bar();

