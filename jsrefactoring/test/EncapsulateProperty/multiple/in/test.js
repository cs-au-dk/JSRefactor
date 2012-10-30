function A() {
    this.x /* encapsulate */ = 23;
}

function B() {
    this.x = 42;
}

var F = Math.random() > 0.5 ? A : B;
var o = new F();
alert(o.x);
