function A() {
    this.x /* -> y */ = 42;
}

var a = new A();
alert(a.hasOwnProperty('x'));