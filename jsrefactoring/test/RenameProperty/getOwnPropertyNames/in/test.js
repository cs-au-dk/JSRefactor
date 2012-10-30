function A() {
    this.x /* -> y */ = 42;
}

var a = new A();
alert(Object.getOwnPropertyNames(a));