function f(X,o) {
	with (o) {
		X = 6;
	}
	return X;
}
var obj = {};
obj.X /* -> X */ = 7;
f(3,obj);

var obj2 = {X : 3};
f(2,obj2);