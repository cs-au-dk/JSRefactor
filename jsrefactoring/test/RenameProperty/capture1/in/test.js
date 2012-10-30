function A() {
  this.x /* -> y */ = 23;
}
function B() {
  this.y = 42;
}
B.prototype = new A();
var b = new B();
var x = b.x;  // rename should fail, since this access would be captured
