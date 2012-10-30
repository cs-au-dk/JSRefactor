function Bar() {}

function Foo() {
	try {
		throw Bar;
	} catch (ex) {
		ex();
	}
	if (ex) ex(); // not in scope here
}

try {
	throw Foo;
} catch (ex2) {
	ex2();
}
if (ex2) ex2(); // not in scope here

var obj = {f:Foo};
try {
	throw obj;
} catch (ex3) {
	ex3.f();
}
if (ex3) ex3.f(); // not in scope here

function Foo2() {}
function Crash1() {
	throw Foo2;
}
function Crash2() {
	return Crash1();
}
try {
	Crash2();
} catch (ex4) {
	ex4();
}
if (ex4) ex4(); // not in scope here
