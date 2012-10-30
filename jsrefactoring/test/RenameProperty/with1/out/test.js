function f(o) {
	var X = 5;
	with (o) {
		X = 6;
	}
	return X;
}
var obj = {};
obj.X /* -> X */ = 7;
f(obj);

var obj2 = {};
f(obj2);