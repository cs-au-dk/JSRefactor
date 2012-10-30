function F() {}
var array1 = [F];
var array2 = [F,F];
var f = array2[array1.length];
f();

var g = array1[F.length];
g();

