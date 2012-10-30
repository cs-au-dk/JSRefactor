function Rectangle(w, h) {
    this.width = w;
    this.height = h;
    /* well-scoped */ function area() {
        return this.width * this.height;
    };
    this.area = area;
}

var r = new Rectangle;