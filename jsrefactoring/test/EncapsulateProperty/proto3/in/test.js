function F() {}

var a = new F();
F.prototype = a;
a.x = "a";

var b = new F();
var z = b.x;
b.x /* encapsulate */ = "b";

alert(z);
