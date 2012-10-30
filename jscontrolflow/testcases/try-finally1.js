
function foo(x) {
	var z = 0;
	for (var i=0; i<10; i++) {
		try {
			with (x) {
				if (i == 5)
					continue;
				else if (i == 6)
					break;
				else if (i == 7)
					return;
				else if (i == 8)
					return i+1;
			}
		} finally {
			z = z + 1;
		}
	}
	return z;
}

foo({});
