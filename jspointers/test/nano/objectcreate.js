
function Foo() {}
function Bar() {}

var proto = {foo:Foo};

var obj = Object.create(proto);

obj.foo();

obj.bar = Bar;
obj.bar();
proto.bar(); // this call should fail



var obj2 = Object.create.call(null,proto);
obj2.foo();
obj2.bar(); // should fail

var obj3 = Object.create.apply(null,[proto]);
obj3.foo();
obj3.bar(); // should fail

var obj4 = Object.create(obj);
obj4.foo();
obj4.bar();

