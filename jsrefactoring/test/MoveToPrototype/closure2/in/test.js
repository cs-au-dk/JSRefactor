function Outer() {
    var x = 23;

    function Inner() {
        this.m = /* move to prototype */ function() {
            return x+19;
        };
    }
    return Inner;
}

var x = new (Outer())();
var v = x.m();
