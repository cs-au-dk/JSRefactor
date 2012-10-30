function Point(x, y) {
  this.xCoord /* -> x */ = x;
  this.y = y;
}
Point.prototype.toString = function() {
  return "(" + this.xCoord + ", " + this.y + ")";
};
var p = new Point(23, 42);
var n = p + "";