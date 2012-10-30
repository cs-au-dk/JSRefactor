function A() {
    this.x = /* move to prototype */ 23;
}

var a1 = new A();
alert(a1.x);

A.prototype = {};
var a2 = new A();
alert(a2.x);

