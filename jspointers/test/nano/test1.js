function Foo() {}

var A = {};
A.w = Foo;
A.w();

var B = Object(45);
B.w = Foo;
B.w();

var C = new Object(45);
C.w = Foo;
C.w();

var D = new Object();
D.w = Foo;
D.w();

