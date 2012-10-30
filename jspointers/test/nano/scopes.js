function MyConstructor() {
	this.x = function() {return "hello";};
}

function AssignToGlobal() {
	obj = new MyConstructor();
}

AssignToGlobal();

var z = obj.x();
