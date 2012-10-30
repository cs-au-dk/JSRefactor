function Bar() {
	return 5;
}

var x = Bar /* -> Bar */ ();
var y = Bar();
