Array.prototype.sum /* -> sum */ = function() {
    var res = 0;
    for(var p in this)
	res += this[p];
    return res;
};

var a = [1, 2, 3];
var all_in = true;
for(var i=0;i<5;++i)
    all_in &= i in a;