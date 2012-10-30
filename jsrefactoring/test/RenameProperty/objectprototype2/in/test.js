
var obj1 = {};
obj1.x /* -> y */ = 5;

var obj2 = {};
obj2.x = 6;

if (Math.random() > 0.5) {
	delete obj1.x;
}

var z = obj1.x || obj2.x;