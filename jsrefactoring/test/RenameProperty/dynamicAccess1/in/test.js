Array.prototype.foo /* -> sum */ = function() {
    var res = 0;
    for(var i=0;i<this.length;++i)
	res += this[i];
    return res;
};

var s = [1, 2, 3].foo();