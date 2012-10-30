function FooTarget() {return 5;}
function Foo() {
	this.target = FooTarget;
}
Foo.prototype.work = function() {
	return this.other();
}
Foo.prototype.other = function() {
	return this.target();
}

function BarTarget() {return "f";}
function Bar() {
	this.target = BarTarget;
}
Bar.prototype.work = function() {
	return this.other();
}
Bar.prototype.other = function() {
	return this.target();
}

var obj = new Foo();
if (blah) {
	obj = new Bar();
}
obj.work();
