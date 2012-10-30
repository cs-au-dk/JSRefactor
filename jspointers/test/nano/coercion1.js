function Foo() {}

var x = Object(45);
x.foo = Foo;
x.foo();

var y = +x;
y.foo();

function SupposedlyUnused() {
	Foo();
}
var baz = {};
function Unused() {
	baz.xxx = SupposedlyUnused;
}
baz.xxx();

var w = {};
var z = {};
z.toString = function() {
	w.foo = Foo;
	this.foo = Foo;
}

var ww = z + "asd";
w.foo();
z.foo();

function Bz() {
	this.x = {};
}
Bz.prototype.toString = function() {
	this.x.foo = Foo;
}
var bz = new Bz();
var www = bz + "asd";
bz.x.foo();

var sideEffectVar;
var toString = function() {
	sideEffectVar = Foo;
}
function CoerceOnGlobalObj() {
	return this + "43";
}
CoerceOnGlobalObj();
sideEffectVar();
