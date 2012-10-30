function A() {
    this.x /* encapsulate */ = 23;
}

var a = new A();
alert('x' in a);
