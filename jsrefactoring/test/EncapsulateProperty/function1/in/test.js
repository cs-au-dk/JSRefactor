function A() {
    this.init /* encapsulate */ = function() {
	this.x = 23;
    };
    this.init();
}

var a = new A();
