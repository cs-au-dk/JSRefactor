function Point(x, y) {
  this.x = x;
  this.y = y;
}
Point.prototype.valueOf /* -> dist */ = function() {
  return Math.sqrt(this.x*this.x+this.y*this.y);
};
var p1 = new Point(23, 42);
var p2 = new Point(56, 72);
p1 < p2;