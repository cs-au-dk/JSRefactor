// Benchpress: A collection of micro-benchmarks.

var allResults = [ ];
var Framework = (function() {
    function Benchmark(string, run) {
      this.string = string;
      this.run = run;
    }
    function time(benchmark) {
      var elapsed = 0;
      var start = new Date();
      for (var n = 0; elapsed < 2000; n++) {
        benchmark.run();
        elapsed = new Date() - start;
      }
      var usec = (elapsed * 1000) / n;
      allResults.push(usec);
      print('Time (' + benchmark.string + '): ' + Math.floor(usec) + ' us.');
    }
    function error(string) {
      print(string);
    }
    return {
        Benchmark: Benchmark,
        time: time,
        error: error
    };
})();
var FibonacciBench = (function() {
    function doFib(n) {
      if (n <= 1) return 1;
      return doFib(n - 1) + doFib(n - 2);
    }
    function fib() {
      var result = doFib(20);
      if (result != 10946) Framework.error("Wrong result: " + result + " should be: 10946");
    }
    var Fibonacci = new Framework.Benchmark("Fibonacci", fib); 
    return {
        Fibonacci: Fibonacci
    };
})();
var LoopBench = (function() {
    function loop() {
      var sum = 0;
      for (var i = 0; i < 200; i++) {
        for (var j = 0; j < 100; j++) {
          sum++;
        }
      }
      if (sum != 20000) Framework.error("Wrong result: " + sum + " should be: 20000");
    }
    var Loop = new Framework.Benchmark("Loop", loop);
    return {
        Loop: Loop
    };
})();
var TowersBench = (function() {
    var towersPiles, towersMovesDone;
    function TowersDisk(size) {
      this.size = size;
      this.next = null;
    }
    function towersPush(pile, disk) {
      var top = towersPiles[pile];
      if ((top != null) && (disk.size >= top.size))
        Framework.error("Cannot put a big disk on a smaller disk");
      disk.next = top;
      towersPiles[pile] = disk;
    }
    function towersPop(pile) {
      var top = towersPiles[pile];
      if (top == null) Framework.error("Attempting to remove a disk from an empty pile");
      towersPiles[pile] = top.next;
      top.next = null;
      return top;
    }
    function towersMoveTop(from, to) {
      towersPush(to, towersPop(from));
      towersMovesDone++;
    }
    function towersMove(from, to, disks) {
      if (disks == 1) {
        towersMoveTop(from, to);
      } else {
        var other = 3 - from - to;
        towersMove(from, other, disks - 1);
        towersMoveTop(from, to);
        towersMove(other, to, disks - 1);
      }
    }
    function towersBuild(pile, disks) {
      for (var i = disks - 1; i >= 0; i--) {
        towersPush(pile, new TowersDisk(i));
      }
    }
    function towers() {
      towersPiles = [ null, null, null ];
      towersBuild(0, 13);
      towersMovesDone = 0;
      towersMove(0, 1, 13);
      if (towersMovesDone != 8191) 
        Framework.error("Error in result: " + towersMovesDone + " should be: 8191");
    }
    var Towers = new Framework.Benchmark("Towers", towers);
    return {
        Towers: Towers
    };
})();
var SieveBench = (function() {
    function doSieve(flags, size) {
      var primeCount = 0, i;
      for (i = 1; i < size; i++) flags[i] = true;
      for (i = 2; i < size; i++) {
        if (flags[i]) { // amoeller: this line deliberately uses 'undefined' as 'false'
          primeCount++;
          for (var k = i + 1; k <= size; k+=i) flags[k - 1] = false;
        }
      } 
      return primeCount;
    }
    function sieve() {
      var flags = new Array(1001);
      var result = doSieve(flags, 1000);
      if (result != 168) 
        Framework.error("Wrong result: " + result + " should be: 168");
    }
    var Sieve = new Framework.Benchmark("Sieve", sieve);
    return {
        Sieve: Sieve
    };
})();
var PermuteBench = (function() {
    var permuteCount;
    function swap(n, k, array) {
      var tmp = array[n];
      array[n] = array[k];
      array[k] = tmp;
    }
    function doPermute(n, array) {
      permuteCount++;
      if (n != 1) {
        doPermute(n - 1, array);
        for (var k = n - 1; k >= 1; k--) {
          swap(n, k, array);
          doPermute(n - 1, array);
          swap(n, k, array);
        }
      }
    }
    function permute() {
      var array = new Array(8);
      for (var i = 1; i <= 7; i++) array[i] = i - 1;
      permuteCount = 0;
      doPermute(7, array);
      if (permuteCount != 8660) Framework.error("Wrong result: " + permuteCount + " should be: 8660");
    }
    var Permute = new Framework.Benchmark("Permute", permute);
    return {
        Permute: Permute
    };
})();
var QueensBench = (function() {
    function tryQueens(i, a, b, c, x) {
      var j = 0, q = false;
      while ((!q) && (j != 8)) {
        j++;
        q = false;
        if (b[j] && a[i + j] && c[i - j + 7]) {
          x[i] = j;
          b[j] = false;
          a[i + j] = false;
          c[i - j + 7] = false;
          if (i < 8) {
            q = tryQueens(i + 1, a, b, c, x);
            if (!q) {
              b[j] = true;
              a[i + j] = true;
              c[i - j + 7] = true;
            }
          } else {
            q = true;
          }
        }
      }
      return q;
    }
    function queens() {
      var a = new Array(9);
      var b = new Array(17);
      var c = new Array(15);
      var x = new Array(9);
      for (var i = -7; i <= 16; i++) {
        if ((i >= 1) && (i <= 8)) a[i] = true;
        if (i >= 2) b[i] = true;
        if (i <= 7) c[i + 7] = true;
      }

      if (!tryQueens(1, b, a, c, x)) 
        Framework.error("Error in queens");
    }
    var Queens = new Framework.Benchmark("Queens", queens);
    return {
        Queens: Queens
    };
})();
var RecurseBench = (function() {
    function recurse(n) {
      if (n <= 0) return 1;
      recurse(n - 1);
      return recurse(n - 1);
    }
    var Recurse = new Framework.Benchmark("Recurse", function () { recurse(13); });
    return {
        Recurse: Recurse
    };
})();
var SumBench = (function() {
    function doSum(start, end) {
      var sum = 0;
      for (var i = start; i <= end; i++) sum += i;
      return sum;
    }
    function sum() {
      var result = doSum(1, 10000);
      if (result != 50005000) Framework.error("Wrong result: " + result + " should be: 50005000");
    }
    var Sum = new Framework.Benchmark("Sum", sum);
    return {
        Sum: Sum
    };
})();
var SortUtil = (function() {
    var randomInitialSeed = 74755; 
    var randomSeed;
    function random() {
      randomSeed = ((randomSeed * 1309) + 13849) % 65536;
      return randomSeed;
    }
    function SortData(length) {
      randomSeed = randomInitialSeed;
      var array = new Array(length);
      for (var i = 0; i < length; i++) array[i] = random();

      var min, max; 
      min = max = array[0];
      for (var i = 0; i < length; i++) {
        var e = array[i];
        if (e > max) max = e;
        if (e < min) min = e;
      }

      this.min = min;
      this.max = max;
      this.array = array;
      this.length = length;
    }
    function check(data) {
      var a = data.array;
      var len = data.length;
      if ((a[0] != data.min) || (a[len - 1] != data.max))
        Framework.error("Array is not sorted");
      for (var i = 1; i < len; i++) {
        if (a[i - 1] > a[i]) Framework.error("Array is not sorted");
      }
    }
    return {
        SortData: SortData,
        check: check
    };
})();
var BubbleBench = (function() {
    function doBubblesort(a, len) {
      for (var i = len - 2; i >= 0; i--) {
        for (var j = 0; j <= i; j++) {
          var c = a[j], n = a[j + 1];
          if (c > n) {
            a[j] = n;
            a[j + 1] = c;
          }
        }
      }
    }
    function bubblesort() {
      var data = new SortUtil.SortData(130);
      doBubblesort(data.array, data.length);
      SortUtil.check(data);
    }
    var BubbleSort = new Framework.Benchmark("BubbleSort", bubblesort);
    return {
        BubbleSort: BubbleSort
    };
})();
var QuickBench = (function() {
    function doQuicksort(a, low, high) {
      var pivot = a[(low + high) >> 1];
      var i = low, j = high;
      while (i <= j) {
        while (a[i] < pivot) i++;
        while (pivot < a[j]) j--;
        if (i <= j) {
          var tmp = a[i];
          a[i] = a[j];
          a[j] = tmp;
          i++;
          j--;
        }
      }

      if (low < j) doQuicksort(a, low, j);
      if (i < high) doQuicksort(a, i, high);
    }
    function quicksort() {
      var data = new SortUtil.SortData(800);
      doQuicksort(data.array, 0, data.length - 1);
      SortUtil.check(data);
    }
    var QuickSort = new Framework.Benchmark("QuickSort", quicksort);
    return {
        QuickSort: QuickSort
    };
})();
var TreeBench = (function() {
    function TreeNode(value) {
      this.value = value;
      this.left = null;
      this.right = null;
    }
    TreeNode.prototype.insert = function (n) {
      if (n < this.value) {
        if (this.left == null) this.left = new TreeNode(n);
        else this.left.insert(n);
      } else {
        if (this.right == null) this.right = new TreeNode(n);
        else this.right.insert(n);
      }
    };
    TreeNode.prototype.check = function () {
      var left = this.left, right = this.right, value = this.value;
      return ((left == null)  || ((left.value  <  value) && left.check())) &&
             ((right == null) || ((right.value >= value) && right.check()));
    };
    function treesort() {
      var data = new SortUtil.SortData(1000);
      var a = data.array;
      var len = data.length;
      var tree = new TreeNode(a[0]);
      for (var i = 1; i < len; i++) tree.insert(a[i]);
      if (!tree.check()) Framework.error("Invalid result, tree not sorted");
    }
    var TreeSort = new Framework.Benchmark("TreeSort", treesort);
    return {
        TreeSort: TreeSort
    };
})();
var TakBench = (function() {
    function tak(x, y, z) {
     if (y >= x) return z;    
     return tak(tak(x-1, y, z), tak(y-1, z, x), tak(z-1, x, y));   
    }
    var Tak = new Framework.Benchmark("Tak", function () { tak(18,12,6); });
    return {
        Tak: Tak
    };
})();
var TaklBench = (function() {
    function ListElement(length, next) {
      this.length = length;
      this.next = next;
    }
    function makeList(length) {
      if (length == 0) return null;
      return new ListElement(length, makeList(length - 1));
    }
    function isShorter(x, y) {
      var xTail = x, yTail = y;
      while (yTail != null) {
        if (xTail == null) return true;
        xTail = xTail.next;
        yTail = yTail.next;
      }
      return false;
    }
    function doTakl(x, y, z) {
      if (isShorter(y, x)) {
        return doTakl(doTakl(x.next, y, z), 
                      doTakl(y.next, z, x), 
                      doTakl(z.next, x, y));
      } else {
        return z;
      }
    }
    function takl() {
      var result = doTakl(makeList(15), makeList(10), makeList(6));
      if (result.length != 10) 
        Framework.error("Wrong result: " + result.length + " should be: 10");
    }
    var Takl = new Framework.Benchmark("Takl", takl);
    return {
        Takl: Takl
    };
})();


// -----------------------------------------------------------------------------
// F r a m e w o r k
// -----------------------------------------------------------------------------


// Run each benchmark for two seconds and count number of iterations.




// -----------------------------------------------------------------------------
// F i b o n a c c i
// -----------------------------------------------------------------------------






// -----------------------------------------------------------------------------
// L o o p
// -----------------------------------------------------------------------------





// -----------------------------------------------------------------------------
// T o w e r s
// -----------------------------------------------------------------------------











// -----------------------------------------------------------------------------
// S i e v e 
// -----------------------------------------------------------------------------





// -----------------------------------------------------------------------------
// P e r m u t e
// -----------------------------------------------------------------------------

 





// -----------------------------------------------------------------------------
// Q u e e n s
// -----------------------------------------------------------------------------





// -----------------------------------------------------------------------------
// R e c u r s e
// -----------------------------------------------------------------------------





// -----------------------------------------------------------------------------
// S u m
// -----------------------------------------------------------------------------






// -----------------------------------------------------------------------------
// H e l p e r   f u n c t i o n s   f o r   s o r t s
// -----------------------------------------------------------------------------






// -----------------------------------------------------------------------------
// B u b b l e S o r t
// -----------------------------------------------------------------------------






// -----------------------------------------------------------------------------
// Q u i c k S o r t
// -----------------------------------------------------------------------------






// -----------------------------------------------------------------------------
// T r e e S o r t
// -----------------------------------------------------------------------------








// -----------------------------------------------------------------------------
//  T a k
// -----------------------------------------------------------------------------





// -----------------------------------------------------------------------------
//  T a k l
// -----------------------------------------------------------------------------









// -----------------------------------------------------------------------------
// M a i n 
// -----------------------------------------------------------------------------
Framework.time(FibonacciBench.Fibonacci);
Framework.time(LoopBench.Loop);
Framework.time(TowersBench.Towers);
Framework.time(SieveBench.Sieve);
Framework.time(PermuteBench.Permute);
Framework.time(QueensBench.Queens);
Framework.time(RecurseBench.Recurse);
Framework.time(SumBench.Sum);
Framework.time(BubbleBench.BubbleSort);
Framework.time(QuickBench.QuickSort);
Framework.time(TreeBench.TreeSort);
Framework.time(TakBench.Tak);
Framework.time(TaklBench.Takl);

var logMean = 0;
for (var i = 0; i < allResults.length; i++)
  logMean += Math.log(allResults[i]);
logMean /= allResults.length;

print("Geometric mean: " + Math.round(Math.pow(Math.E, logMean)) + " us.");
