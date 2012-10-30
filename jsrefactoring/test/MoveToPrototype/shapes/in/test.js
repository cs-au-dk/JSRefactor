function Circle(x, y, radius, color) {
  this.x = x;
  this.y = y;
  this.radius = radius; 
  this.color = color;

  this.drawShape = function (gr) {
    gr.fillCircle(new jsColor(this.color),
                  new jsPoint(this.x,this.y),
                  this.radius);
  }
}

function Rectangle(x, y, width, height, color) {
  this.x = x;
  this.y = y;
  this.width = width;
  this.height = height;
  this.color = color;

  this.drawShape = /* move to prototype */ function (gr) {
    gr.fillRectangle(new jsColor(this.color),
                     new jsPoint(this.x,this.y),
                     this.width, this.height);
  }
}

function Square(x, y, size, color) {
  Rectangle.call(this, x, y, size, size, color);
  this.size = size;
};
Square.prototype = new Rectangle;

function drawAll (shapes) {
  var gr = new jsGraphics(document.getElementById("canvas"));
  shapes.map( function(s) { s.drawShape(gr); });
}
function r (n) { return Math.round(Math.random() * n); }

sh = [];
for (var i = 0; i < 500; i++){
  var o = new jsColor().rgbToHex(r(255), r(255), r(255));
  switch(r(3)){
    case 0: sh[i] = double(new Circle(r(500),r(500),r(50), o)); break;
    case 1: sh[i] = new Rectangle(r(500),r(500),r(50), r(50), o); break;
    case 2: sh[i] = new Square(r(500),r(500),r(50), o); break; 
  }
};
drawAll(sh);
