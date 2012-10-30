function Rectangle(w, h) {
    this.width = w;
    this.height = h;
    this.area = /* not well-scoped */ function() {
        return this.width * this.height;
    };
}

var r = new Rectangle(23, 56);
r.area.call(new Rectangle(56, 72));

