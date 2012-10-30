function Foo() {}

function ThrowFoo() {
	throw Foo;
}
function ThrowThis() {
	throw this;
}

var obj1 = {};
obj1.valueOf = function() {
	ThrowFoo();
}

try {
	var z1 = 23 + obj1;
} catch (err1) {
	err1();
}

var obj2 = {};
obj2.valueOf = function() {
	ThrowThis.call(Foo);
}


try {
	try {
		var z2 = 23 + obj2;
	} catch (err2) {
		err2();
	}
} catch (err3) {
	// err3 should have no call targets
	err3();
}