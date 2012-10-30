function Foo() {}
var x = Object.call(null,45);
x.w = Foo;
x.w();
