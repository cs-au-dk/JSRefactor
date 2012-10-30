function f() {
    alert("Hi!");
    return this;
}

/* extract module M { */
x = 42;
var y = f().x;
/* } */
