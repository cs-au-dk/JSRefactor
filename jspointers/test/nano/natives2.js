function Baz() {}

var x = Object(45);
//x.foo = Baz;
//x.foo();

//var y = 45;
//y.foo = Baz;
//y.foo();
//
//var z = new Object(x);
//z.bar = Baz;
//x.bar();
// note: x and z are aliases despite the 'new' keyword

var w = new Object(45);
w.bong = Baz;
x.bong();
// w is a new object, because y was a primitive