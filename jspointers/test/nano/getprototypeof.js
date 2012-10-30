function Foo() {
}
Foo.prototype.member = function() {
}

var obj = new Foo();
obj.member = function() {
}

obj.member();

var proto = Object.getPrototypeOf(obj);
proto.member();

