function A() {
    this.x = 42;
}

var a = new A();
var o = { x: 23 };

var p = Math.random() > 0.2 ? a : o;
var x = p.x /* encapsulate */;