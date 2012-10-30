function Bar() {
	return "";
}
function Foo() {
	return 3;
}
function Lookup(x) {
	if (x.next)
		return Lookup(x.next);
	else
		return x;
}
var obj = {};
obj.next = {};
obj.next.next = Bar;
obj.next.next.next = {};
obj.next.next.next.next = Foo;


var z = Lookup(obj);
var answer = z();
