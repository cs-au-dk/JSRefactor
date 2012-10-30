function foo() {
	function bar() {return 13;}
	return bar;
}

var x = foo();
var y = foo();

// a new instance of the function is created for every call
assert(x != y);
