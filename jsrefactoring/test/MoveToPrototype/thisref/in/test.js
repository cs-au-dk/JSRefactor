function A() {
    this.x = 23;
    this.y = /* move to prototype */ this.x + 19;
}

var a = new A();
