function f() {
	
	for (var i=0; i<10; i++) {
		try {
			if (i == 5)
				throw new Error();
		} catch (e) {
			break;
		}
	}
	
}