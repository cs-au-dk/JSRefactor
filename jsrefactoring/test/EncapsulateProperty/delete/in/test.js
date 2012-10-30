function Rectangle(w, h) {
    this.width /* encapsulate */ = w;
    this.height = h;
}

var r = new Rectangle(23, 42);
delete r.width;