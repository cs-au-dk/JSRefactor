function A() {
    this.x = 23;
    this.x = /* move to prototype */ 42;
    alert(this.x);
}

var a = new A();
