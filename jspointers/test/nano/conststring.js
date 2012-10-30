function A() {}
function B() {}
var foo = {};
foo['a'] = A;
foo['b'] = B;
foo.a();
foo.b();
