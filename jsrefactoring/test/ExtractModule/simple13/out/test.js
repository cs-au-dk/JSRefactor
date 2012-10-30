var M = (function() {
    var x, y;
    x = 23;
    y = 42;
    return {
        x: x,
        y: y
    };
})();
/* extract module M { */
/* } */
alert(M.x+M.y);
