function Point(xCoord, yCoord) {
  this.xCoord /* -> x */ = xCoord;
  this.y = y;
}
var p = new Point(23, 42);
var found = 'xCoord' in p;
alert(found);