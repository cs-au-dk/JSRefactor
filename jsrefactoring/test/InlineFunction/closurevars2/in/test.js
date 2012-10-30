function A(v) {
    var x = v;
    this.getX = function() { return x; };
    this.ms = this.ms || [];
    this.ms.push(function() { return this.getX() /* inline */; });
    // this call cannot be inlined, since the containing closure does not
    // always capture the same environment as the called closure; see below
}

var a = new A(23);
A.call(a, 42);
print(a.ms[0].call(a));
