function Square(o, l) {
    this.origin = o;
    this.length = l;
}

Function.prototype.method = function(name, func) {
    this.prototype[name] = func;
    return this;
};

Square.method('area', 
	      function() {
		  return this.length * this.length;
	      });

var x = new Square(0, 10);
var a = x.area /* -> a */ ();
alert(a);
