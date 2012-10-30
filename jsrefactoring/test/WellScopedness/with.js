var o = {}

function Rectangle(w, h) {
    this.width = w;
    this.height = h;
    with(o) {
        area = /* not well-scoped */ function() {
            return this.width * this.height;
        };
    }
}

var r = new Rectangle;
