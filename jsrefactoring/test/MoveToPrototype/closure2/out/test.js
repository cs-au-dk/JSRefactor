function Outer() {
    var x = 23;

    function Inner() {
    }
    Inner.prototype.m = /* move to prototype */ function() {
        return x+19;
    };
    return Inner;
}

var x = new (Outer())();
var v = x.m();
