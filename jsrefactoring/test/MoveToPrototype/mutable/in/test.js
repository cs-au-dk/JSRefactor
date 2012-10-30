function A() {
    this.x = /* move to prototype */ [0, 1];
}

var a1 = new A();
var a2 = new A();
a1.x[0]++;
alert(a2.x[0]);
