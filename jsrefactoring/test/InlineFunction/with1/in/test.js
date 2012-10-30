
function Foo() {
	return this.x;
}

var z;
var o = {a:Foo, x:17};
with (o) {
	z = a() /* inline */;
}
