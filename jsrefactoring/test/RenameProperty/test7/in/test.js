function Point(x, y) {
  this.x = x;
  this.y = y;
}
Point.prototype.toString /* -> prettyPrint */ = function() {
  return "(" + this.x + ", " + this.y + ")";
};
var p = new Point(23, 42);
var n = p + "";