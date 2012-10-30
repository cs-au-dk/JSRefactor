function Foo(x) {
	return x;
}
var object = { bar: function() {return 45;} };
var x = Foo(Foo);
var y = object.bar();
var z = Foo(object.bar);
var w = z();
var foo2 = x(Foo);
var foo3 = foo2(foo2);
var foo4 = foo3(object.bar);
var foo4_result = foo4();

function Bing() {}
function Bong() { return Bing; }
function GetBar() { return this.bar; }

var bong = Bong();
bong();

var a = GetBar.apply(object);
a();

var b = GetBar.call(object);
b();

function ReturnNestedIdFunc(x) {
	function Nested() {
		return x;
	}
	return Nested;
}

var nst = ReturnNestedIdFunc(Bing);
var nstx = nst();
nstx();

function SomethingWierd(a, b, c) {
	return this.foo(a(b(c)));
}
var objWithFoo = { 
	foo : function(x) {
		return function() {return x};
	} 
};

var c = SomethingWierd.apply(objWithFoo, [Foo, Foo, Bing]);
var cc = c();
cc();

var d = SomethingWierd.call(objWithFoo, Foo, Foo, Bing);
var dd = d();
dd();
//
//var aaa = 1;
//var bbb = -aaa;
//var ccc = aaa + bbb;

function Zoot(x) {
	return this[x];
}

var obj = {bar:function(x) { return x; }};
// Foo.call(obj,"bar")
var zoo = Function.prototype.call.call(Zoot,obj,"bar");
var woo = zoo(5);