function f() {
    return 23;
}

/* extract module M { */
x = this.f();
/* } */
alert(x);
