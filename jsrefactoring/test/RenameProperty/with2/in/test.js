function f(x,o) {
	with (o) {
		x = 6;
	}
	return x;
}
var obj = {};
obj.x /* -> X */ = 7;
f(3,obj);

var obj2 = {x : 3};
f(2,obj2);