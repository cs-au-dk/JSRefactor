function Rectangle(w, h) {
    this.width = w;
    this.height = h;
    this.area = /* not well-scoped */ function() {
        return this.width * this.height;
    };
}

var r1 = new Rectangle(23, 56),
    r2 = new Rectangle(56, 72);
Object.defineProperty(r1, "area", { value: r2.area });

