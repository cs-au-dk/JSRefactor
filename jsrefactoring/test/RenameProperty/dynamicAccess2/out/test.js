Array.prototype.sum /* -> sum */ = function() {
    var res = 0;
    for(var p in this)
	res += this[p];
    return res;
};

var s = [1, 2, 3].sum();