Object.prototype.inherits = function (shuper) {
  function Inheriter() { }
  Inheriter.prototype = shuper.prototype;
  this.prototype = new Inheriter();
  this.superConstructor = shuper;
}

function AssignStmt(resultVar) {
	this.getResultVar = function() {
		return resultVar;
	}
}

function BinopStmt(resultVar, arg1, op, arg2) {
	AssignStmt.call(this,resultVar);
	
	this.getArg1 = function() {
		return arg1;
	}
	this.getArg2 = function() {
		return arg2;
	}
	this.getOp = function() {
		return op;
	}
	this.getReadVariables = function() {
		return [(function() {
return arg1;})() /* inline */, 
				this.getArg2()];
	}
}
BinopStmt.inherits(AssignStmt);

function UnopStmt(resultVar, op, arg) {
	AssignStmt.call(this,resultVar);
	this.getOp = function() {
		return op;
	}
	this.getArg = function() {
		return arg;
	}
	this.getReadVariables = function() {
		return [this.getArg()];
	}
}
UnopStmt.inherits(AssignStmt);



var stmt1 = new BinopStmt(1, 2, "+", 4);
var stmt2 = new UnopStmt(2, "-", 1);

var vars1 = stmt1.getReadVariables();
var vars2 = stmt2.getReadVariables();

