function Point(x, y) {
  this.x /* -> x */ = x;
  this.y = y;
}
Point.prototype.toString = function() {
  return "(" + this.x + ", " + this.y + ")";
};
var p = new Point(23, 42);
var n = p + "";