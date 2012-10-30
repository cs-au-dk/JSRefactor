var glob = 37;
function A() {
    this.x = /* move to prototype */ glob + 19;
}

glob = 23;
var a = new A();
alert(a.x);
