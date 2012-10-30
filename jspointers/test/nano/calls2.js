function Foo(x,y) {
	this.map = {};
	this.map[x] = y;
}
Foo.prototype.put = function (x,y) {
	y.func();
	this.map[x] = y;
	this.map[x].func();
}
Foo.prototype.baz = function() {
	this.map["x"].func();
	return this.map["x"];
}

var object = {func : function() {}};

var foo = new Foo("a","b");
foo.put("x",object);

foo.map.x = "z";

var baz = foo.baz();
baz.func();
