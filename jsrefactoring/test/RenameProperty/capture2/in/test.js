function A() {
  this.x = 23;
}
function B() {
  this.y /* -> x */ = 42;
}
B.prototype = new A();
var b = new B();
var x = b.x;  // rename should fail, since this access would be captured