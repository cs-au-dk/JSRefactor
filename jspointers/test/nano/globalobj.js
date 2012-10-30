var declared = 45;

function Bar() {}

function Foo() {
	this.declared = Bar;
	this.undeclared = Bar;
}

Foo();

declared();
undeclared();
