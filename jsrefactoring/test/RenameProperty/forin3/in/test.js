function f(o) {
	var numPrtys = 0;
	for (var p in o) {
		numPrtys++;
	}
	return numPrtys;
}

var obj1 = {foo:5, bar:4};
obj1.baz /* -> raz */ = "hello";

f(obj1);
