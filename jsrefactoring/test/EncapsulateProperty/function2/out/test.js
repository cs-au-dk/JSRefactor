function A() {
    var init;
    init /* encapsulate */ = function() {
	return 23;
    };
    this.x = init();
}

var a = new A();
