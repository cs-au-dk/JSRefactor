function A() {
    this.x = /* move to prototype */ 23;
}
A.prototype = {
    getX: function() { return this.x; }
};

var a1 = new A();
var x1 = a1.getX();

A.prototype = {};

var a2 = new A();
var x2 = a2.x;
