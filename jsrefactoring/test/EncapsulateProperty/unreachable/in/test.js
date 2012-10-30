function Rectangle(w, h) {
    this.w = w;
    this.h = h;
    this.area = function() { return this.w /* encapsulate */ * this.h; };
}

var r = new Rectangle(23, 42);
