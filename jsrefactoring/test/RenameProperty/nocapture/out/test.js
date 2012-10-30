function A() {
  this.y /* -> y */ = 23;
}
function B() {
  this.y = 42;
}
B.prototype = new A();
var a = new A();
var x = a.y;