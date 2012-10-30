function f(o) {
	var X /* -> X */ = 5 + o.x;
	return X;
}
var x = 5;
var o = {x:6};
o.x = 3;
f(o);
