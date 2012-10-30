function A() {
    var init;
    init /* encapsulate */ = function() {
	this.x = 23;
    };
    init.call(this);
}

var a = new A();
