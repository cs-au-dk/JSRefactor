function chainTest(n) {
  var first = null;
  
  // Build chain of n equality constraints
  for (var i = 0; i <= n; i++) {
    first = new Variable();
  }

  for (var i = 0; i < 100; i++) {
    first.value = i;
  }
}

