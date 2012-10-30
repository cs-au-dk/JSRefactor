function A() {
    this.x /* encapsulate */ = 23;
}
A.prototype.getX = function() {
    return this.x;
};

var a = new A();
var x = a.getX();
