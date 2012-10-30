var x = 23;

function A() {
    this.x = 42;
    this.y = /* move to prototype */ (function(){ return this.x; })();
}

var a = new A();
alert(a.y);
