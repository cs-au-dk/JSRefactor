function A() {
    this.x = /* move to prototype */ 23;
}

var a = new A();
delete a.x;
