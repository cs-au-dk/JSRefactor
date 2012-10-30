function f(o) {
	var s = "";
	for (var p in o) {
		var x = o[p];
		s += x;
	}
	return s;
}

var obj1 = {foo:5, bar:4};
obj1.raz /* -> raz */ = "hello";

f(obj1);
