
var array = new Array();

function Foo() { return 45; }
function Bar() { return Foo; }

var arrayInit = new Array(Bar, 34);
var barPtr = arrayInit[0];
var fooPtr = barPtr();
var fortyFive = fooPtr();

var NUM = 45;
function Baz() {
	return new Array(NUM);
}
var bz = new Baz();

function rr() {
	new Scheduler();
}
rr();

function Scheduler() {
  this.queueCount = 0;
  this.holdCount = 0;
  this.blocks = new Array/*<TaskControlBlock>*/(NUMBER_OF_IDS);
  this.list = null;
  this.currentTcb = null;
  this.currentId = null;
}