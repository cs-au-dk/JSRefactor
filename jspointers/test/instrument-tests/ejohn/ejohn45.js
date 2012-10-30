function highest(basex){ 
  return makeArray(arguments).sort(function(a,b){ 
    return a - b; 
  }).slice(0, basex); 
} 
 
function makeArray(array){ 
  return Array().slice.call( array ); 
} 
 
assert(highest(1, 1, 2, 3).length == 1); 
assert(highest(3, 1, 2, 3, 4, 5)[2] == 3);

dumpValue(highest(1, 1, 2, 3).length); 
dumpValue(highest(3, 1, 2, 3, 4, 5)[2]);

