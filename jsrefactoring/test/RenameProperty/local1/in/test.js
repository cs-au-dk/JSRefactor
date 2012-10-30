function f(o) {
	var x /* -> X */ = 5 + o.x;
	return x;
}
var x = 5;
var o = {x:6};
o.x = 3;
f(o);
