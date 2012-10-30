var obj1 = {foo:5, bar:4};
obj1.baz /* -> raz */ = "hello";

var numPrtys = 0;
for (var p in obj1) {
	numPrtys++;
}

var str = g() + "abc";

function g() {
	return this.p; // accesses the last property from the for-in loop
}
