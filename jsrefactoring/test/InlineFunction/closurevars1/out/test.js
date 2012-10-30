function Rectangle(w, h) {
    var width = w, height = h;
    this.getWidth = function() { return width; }
    this.getHeight = function() { return height; }
    this.getArea = function() {
	return (function() {
return width;})() /* inline */
 	     * this.getHeight();
    };
}

var r = new Rectangle(23, 42);
alert(r.getArea());
