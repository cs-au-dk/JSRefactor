function F() {}

F.prototype.constructor();

var x = new F();
x.constructor();
