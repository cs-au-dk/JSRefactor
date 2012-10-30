function Point(xCoord, yCoord) {
  this.x /* -> x */ = xCoord;
  this.y = y;
}
var p = new Point(23, 42);
var found = 'x' in p;
alert(found);