/* this refactoring would not change semantics, but is highly
   doubtful; hence it seems justified to reject it, as we do */
function f(x,y /* -> x */) {
	return y;
}
f(1,2);