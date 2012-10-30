/* extract module M { */
x = 23;
/* } */
var ps = ['M'];
for(var i=0;i<ps.length;++i)
    alert(ps[i] in this);
