function foo(x) {
	return x;
}
var y = bar(5); // nothing called 'bar'
var z = foo(20);

function baz() {
	return new Date(); // valid when modelling natives
}

var w = baz();