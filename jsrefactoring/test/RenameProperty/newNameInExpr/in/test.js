
var obj = {};
obj.x /* -> y */ = 5;

var obj2 = {};
obj.y = 6;

function f(o) {
	return 'y' in o;
}

f(obj);  // rename should fail, otherwise this expression would change from "false" to "true"
f(obj2);
