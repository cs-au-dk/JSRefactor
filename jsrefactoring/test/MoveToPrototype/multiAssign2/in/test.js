function A() {
    var a = [];
    for(var i=0;i<2;++i) {
	this.x = /* move to prototype */ 42;
	a[i] = this.x;
	this.x = 23;
    }
    alert(a.join(', '));
}

var a = new A();
