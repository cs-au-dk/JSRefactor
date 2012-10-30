function Baz() {
	return "foo";
}
function Foo(x) {
	var baz;
	with (x) {
		(function() {
			baz = Baz;
		})();
	}
}
var obj = {baz:4};
Foo(obj);
var z = obj.baz();
