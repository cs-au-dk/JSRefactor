function Rectangle(w, h) {
    this.width = w;
    this.height = h;
    var area = /* not well-scoped */ function() {
        return this.width * this.height;
    };
    area();
}

var r = new Rectangle;
