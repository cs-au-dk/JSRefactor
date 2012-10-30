function Rectangle(w, h) {
    this.w /* encapsulate */ = w;
    this.h = h;
}

var r1 = new Rectangle(23, 42);
var r2 = { w: 56, h: 72 };
var r3 = Math.random() > 0.5 ? r1 : r2;
alert(r3.w);