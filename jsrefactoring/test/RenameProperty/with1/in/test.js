function f(o) {
	var x = 5;
	with (o) {
		x = 6;
	}
	return x;
}
var obj = {};
obj.x /* -> X */ = 7;
f(obj);

var obj2 = {};
f(obj2);