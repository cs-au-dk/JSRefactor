function Rectangle(w, h) {
    var width;
    this.getWidth = function() {
        return width;
    };
    this.setWidth = function(newWidth) {
        return width = newWidth;
    };
    width /* encapsulate */ = w;
    this.height = h;
}

var r = new Rectangle(23, 42);
r.setWidth(r.getWidth()+1);
