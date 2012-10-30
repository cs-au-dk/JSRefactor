function A() {
    this.init /* encapsulate */ = function() {
	return 23;
    };
    this.x = this.init();
}

var a = new A();
