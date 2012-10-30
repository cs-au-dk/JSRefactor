function Point(x, y) {
  this.xCoord /* -> x */ = x;
  this.y = y;
}
function ColourPoint(x, y, c) {
  Point.call(this, x, y);
  this.colour = c;
}
ColourPoint.prototype = new Point;

var p = new ColourPoint(23, 42, 'red');
p.xCoord;