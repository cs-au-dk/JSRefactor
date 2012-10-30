function A() {
    this.x /* -> y */ = 23;
}

var a = new A();
var x = a['x'];