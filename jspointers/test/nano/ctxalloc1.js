function Allocate(x) {
	return {bar:x};
}
function Foo() {
	return 5;
}
function Bar() {
	return "";
}
var a = Allocate(Foo);
var b = Allocate(Bar);

var az = a.bar();
var bz = b.bar();