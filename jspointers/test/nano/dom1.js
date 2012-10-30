function Foo() {
}
var foo = Foo;
Foo();
foo();

window.addEventListener("load", function() {
	Foo();
});
