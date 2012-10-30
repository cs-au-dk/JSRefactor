
var obj1 = {};
obj1.y /* -> y */ = 5;

var obj2 = {};
obj2.x = 6;

if (Math.random() > 0.5) {
	delete obj1.y;
}

var z = obj1.y || obj2.x;