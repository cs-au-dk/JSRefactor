// rename should fail since property names clash
function Point(x, y) {
    this.x /* -> y */ = x;
    this.y = y;
}

var p = new Point(23, 56);
var x = p.x;
