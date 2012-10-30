// Copyright 2007 Google Inc.
// All rights reserved.

var standalone = false;
try {
  document;
} catch(error) {
  standalone = true;
}
var Main = (function() {
    function runRichards() {
      var count = 100000;
      
      var scheduler = new Sched.Scheduler();
      scheduler.addIdleTask(Sched.ID_IDLE, 0, null, count);
      
      var queue = new P.Packet(null, Sched.ID_WORKER, Sched.KIND_WORK);
      queue = new P.Packet(queue,  Sched.ID_WORKER, Sched.KIND_WORK);
      scheduler.addWorkerTask(Sched.ID_WORKER, 1000, queue);
      
      queue = new P.Packet(null, Sched.ID_DEVICE_A, Sched.KIND_DEVICE);
      queue = new P.Packet(queue,  Sched.ID_DEVICE_A, Sched.KIND_DEVICE);
      queue = new P.Packet(queue,  Sched.ID_DEVICE_A, Sched.KIND_DEVICE);
      scheduler.addHandlerTask(Sched.ID_HANDLER_A, 2000, queue);
      
      queue = new P.Packet(null, Sched.ID_DEVICE_B, Sched.KIND_DEVICE);
      queue = new P.Packet(queue,  Sched.ID_DEVICE_B, Sched.KIND_DEVICE);
      queue = new P.Packet(queue,  Sched.ID_DEVICE_B, Sched.KIND_DEVICE);
      scheduler.addHandlerTask(Sched.ID_HANDLER_B, 3000, queue);
      
      scheduler.addDeviceTask(Sched.ID_DEVICE_A, 4000, null);

      scheduler.addDeviceTask(Sched.ID_DEVICE_B, 5000, null);
      
      var start = getTime();
      scheduler.schedule();
      var end = getTime();
      
      if (scheduler.queueCount == EXPECTED_QUEUE_COUNT
        && scheduler.holdCount == EXPECTED_HOLD_COUNT) {
        if (standalone) {
          print("Time (richards): " + (end - start) + " ms.");
        }
      } else {
        var msg = "Error during execution: queueCount = " + scheduler.queueCount + 
              ", holdCount = " + scheduler.holdCount + ".";
        if (standalone) {
          print(msg);
        } else {
          error(msg);
        }
      }
    }
    var EXPECTED_QUEUE_COUNT = 232625;
    var EXPECTED_HOLD_COUNT = 93050;
    function getTime() {
      //if ('now' in Date) {
      //  return Date.now();
      //} else {
      //  return (new Date).getTime();
      //}
      return new Date();
    }
    return {
        runRichards: runRichards
    };
})();
var Sched = (function() {
    function Scheduler() {
      this.queueCount = 0;
      this.holdCount = 0;
      this.blocks = new Array/*<TaskControlBlock>*/(NUMBER_OF_IDS);
      this.list = null;
      this.currentTcb = null;
      this.currentId = null;
    }
    var ID_IDLE       = 0;
    var ID_WORKER     = 1;
    var ID_HANDLER_A  = 2;
    var ID_HANDLER_B  = 3;
    var ID_DEVICE_A   = 4;
    var ID_DEVICE_B   = 5;
    var NUMBER_OF_IDS = 6;
    var KIND_DEVICE   = 0;
    var KIND_WORK     = 1;
    Scheduler.prototype.addIdleTask = function (id, priority, queue, count) {
      this.addRunningTask(id, priority, queue, new Task.IdleTask(this, 1, count));
    };
    Scheduler.prototype.addWorkerTask = function (id, priority, queue) {
      this.addTask(id, priority, queue, new Task.WorkerTask(this, ID_HANDLER_A, 0));
    };
    Scheduler.prototype.addHandlerTask = function (id, priority, queue) {
      this.addTask(id, priority, queue, new Task.HandlerTask(this));
    };
    Scheduler.prototype.addDeviceTask = function (id, priority, queue) {
      this.addTask(id, priority, queue, new Task.DeviceTask(this))
    };
    Scheduler.prototype.addRunningTask = function (id, priority, queue, task) {
      this.addTask(id, priority, queue, task);
      this.currentTcb.setRunning();
    };
    Scheduler.prototype.addTask = function (id, priority, queue, task) {
      this.currentTcb = new TCB.TaskControlBlock(this.list, id, priority, queue, task);
      this.list = this.currentTcb;
      this.blocks[id] = this.currentTcb;
    };
    Scheduler.prototype.schedule = function () {
      this.currentTcb = this.list;
      while (this.currentTcb != null) {
        if (this.currentTcb.isHeldOrSuspended()) {
          this.currentTcb = this.currentTcb.link;
        } else {
          this.currentId = this.currentTcb.id;
          this.currentTcb = this.currentTcb.run();
        }
      }
    };
    Scheduler.prototype.release = function (id) {
      var tcb = this.blocks[id];
      if (tcb == null) return tcb;
      tcb.markAsNotHeld();
      if (tcb.priority > this.currentTcb.priority) {
        return tcb;
      } else {
        return this.currentTcb;
      }
    };
    Scheduler.prototype.holdCurrent = function () {
      this.holdCount++;
      this.currentTcb.markAsHeld();
      return this.currentTcb.link;
    };
    Scheduler.prototype.suspendCurrent = function () {
      this.currentTcb.markAsSuspended();
      return this.currentTcb;
    };
    Scheduler.prototype.queue = function (packet) {
      var t = this.blocks[packet.id];
      if (t == null) return t;
      this.queueCount++;
      packet.link = null;
      packet.id = this.currentId;
      return t.checkPriorityAdd(this.currentTcb, packet);
    };
    return {
        Scheduler: Scheduler,
        ID_IDLE: ID_IDLE,
        ID_WORKER: ID_WORKER,
        ID_HANDLER_A: ID_HANDLER_A,
        ID_HANDLER_B: ID_HANDLER_B,
        ID_DEVICE_A: ID_DEVICE_A,
        ID_DEVICE_B: ID_DEVICE_B,
        KIND_DEVICE: KIND_DEVICE,
        KIND_WORK: KIND_WORK
    };
})();
var TCB = (function() {
    function TaskControlBlock(link, id, priority, queue, task) {
      this.link = link;
      this.id = id;
      this.priority = priority;
      this.queue = queue;
      this.task = task;
      if (queue == null) {
        this.state = STATE_SUSPENDED;
      } else {
        this.state = STATE_SUSPENDED_RUNNABLE;
      }
    }
    var STATE_RUNNING = 0;
    var STATE_RUNNABLE = 1;
    var STATE_SUSPENDED = 2;
    var STATE_HELD = 4;
    var STATE_SUSPENDED_RUNNABLE = STATE_SUSPENDED | STATE_RUNNABLE;
    var STATE_NOT_HELD = ~STATE_HELD;
    TaskControlBlock.prototype.setRunning = function () {
      this.state = STATE_RUNNING;
    };
    TaskControlBlock.prototype.markAsNotHeld = function () {
      this.state = this.state & STATE_NOT_HELD;
    };
    TaskControlBlock.prototype.markAsHeld = function () {
      this.state = this.state | STATE_HELD;
    };
    TaskControlBlock.prototype.isHeldOrSuspended = function () {
      return (this.state & STATE_HELD) != 0 || (this.state == STATE_SUSPENDED);
    };
    TaskControlBlock.prototype.markAsSuspended = function () {
      this.state = this.state | STATE_SUSPENDED;
    };
    TaskControlBlock.prototype.markAsRunnable = function () {
      this.state = this.state | STATE_RUNNABLE;
    };
    TaskControlBlock.prototype.run = function () {
      var packet;
      if (this.state == STATE_SUSPENDED_RUNNABLE) {
        packet = this.queue;
        this.queue = packet.link;
        if (this.queue == null) {
          this.state = STATE_RUNNING;
        } else {
          this.state = STATE_RUNNABLE;
        }
      } else {
        packet = null;
      }
      return this.task.run(packet);
    };
    TaskControlBlock.prototype.checkPriorityAdd = function (task, packet) {
      if (this.queue == null) {
        this.queue = packet;
        this.markAsRunnable();
        if (this.priority > task.priority) return this;
      } else {
        this.queue = packet.addTo(this.queue);
      }
      return task;
    };
    TaskControlBlock.prototype.toString = function () {
      return "tcb { " + this.task + "@" + this.state + " }";
    };
    return {
        TaskControlBlock: TaskControlBlock
    };
})();
var Task = (function() {
    function IdleTask(scheduler, v1, count) {
      this.scheduler = scheduler;
      this.v1 = v1;
      this.count = count;
    }
    IdleTask.prototype.run = function (packet) {
      this.count--;
      if (this.count == 0) return this.scheduler.holdCurrent();
      if ((this.v1 & 1) == 0) {
        this.v1 = this.v1 >> 1;
        return this.scheduler.release(Sched.ID_DEVICE_A);
      } else {
        this.v1 = (this.v1 >> 1) ^ 0xD008;
        return this.scheduler.release(Sched.ID_DEVICE_B);
      }
    };
    IdleTask.prototype.toString = function () {
      return "IdleTask"
    };
    function DeviceTask(scheduler) {
      this.scheduler = scheduler;
      this.v1 = null;
    }
    DeviceTask.prototype.run = function (packet) {
      if (packet == null) {
        if (this.v1 == null) return this.scheduler.suspendCurrent();
        var v = this.v1;
        this.v1 = null;
        return this.scheduler.queue(v);
      } else {
        this.v1 = packet;
        return this.scheduler.holdCurrent();
      }
    };
    DeviceTask.prototype.toString = function () {
      return "DeviceTask";
    };
    function WorkerTask(scheduler, v1, v2) {
      this.scheduler = scheduler;
      this.v1 = v1;
      this.v2 = v2;
    }
    WorkerTask.prototype.run = function (packet) {
      if (packet == null) {
        return this.scheduler.suspendCurrent();
      } else {
        if (this.v1 == Sched.ID_HANDLER_A) {
          this.v1 = Sched.ID_HANDLER_B;
        } else {
          this.v1 = Sched.ID_HANDLER_A;
        }
        packet.id = this.v1;
        packet.a1 = 0;
        for (var i = 0; i < P.DATA_SIZE; i++) {
          this.v2++;
          if (this.v2 > 26) this.v2 = 1;
          packet.a2[i] = this.v2;
        }
        return this.scheduler.queue(packet);
      }
    };
    WorkerTask.prototype.toString = function () {
      return "WorkerTask";
    };
    function HandlerTask(scheduler) {
      this.scheduler = scheduler;
      this.v1 = null;
      this.v2 = null;
    }
    HandlerTask.prototype.run = function (packet) {
      if (packet != null) {
        if (packet.kind == Sched.KIND_WORK) {
          this.v1 = packet.addTo(this.v1);
        } else {
          this.v2 = packet.addTo(this.v2);
        }
      }
      if (this.v1 != null) {
        var count = this.v1.a1;
        var v;
        if (count < P.DATA_SIZE) {
          if (this.v2 != null) {
            v = this.v2;
            this.v2 = this.v2.link;
            v.a1 = this.v1.a2[count];
            this.v1.a1 = count + 1;
            return this.scheduler.queue(v);
          }
        } else {
          v = this.v1;
          this.v1 = this.v1.link;
          return this.scheduler.queue(v);
        }
      }
      return this.scheduler.suspendCurrent();
    };
    HandlerTask.prototype.toString = function () {
      return "HandlerTask";
    };
    return {
        IdleTask: IdleTask,
        DeviceTask: DeviceTask,
        WorkerTask: WorkerTask,
        HandlerTask: HandlerTask
    };
})();
var P = (function() {
    var DATA_SIZE = 4;
    function Packet(link, id, kind) {
      this.link = link;
      this.id = id;
      this.kind = kind;
      this.a1 = 0;
      this.a2 = new Array(DATA_SIZE);
    }
    Packet.prototype.addTo = function (queue) {
      this.link = null;
      if (queue == null) return this;
      var peek, next = queue;
      while ((peek = next.link) != null)
        next = peek;
      next.link = this;
      return queue;
    };
    Packet.prototype.toString = function () {
      return "Packet";
    };
    return {
        DATA_SIZE: DATA_SIZE,
        Packet: Packet
    };
})();

//(function () {

/**
 * JavaScript implementation of the OO Richards benchmark.
 *
 * The Richards benchmark simulates the task dispatcher of an
 * operating system.
 **/


/**
 * These two constants specify how many times a packet is queued and
 * how many times a task is put on hold in a correct run of richards. 
 * They don't have any meaning a such but are characteristic of a
 * correct run so if the actual queue or hold count is different from
 * the expected there must be a bug in the implementation.
 **/



/**
 * A scheduler can be used to schedule a set of tasks based on their relative
 * priorities.  Scheduling is done by maintaining a list of task control blocks
 * which holds tasks and the data queue they are processing.
 * @constructor
 */




/**
 * Add an idle task to this scheduler.
 * @param {int} id the identity of the task
 * @param {int} priority the task's priority
 * @param {Packet} queue the queue of work to be processed by the task
 * @param {int} count the number of times to schedule the task
 */

/**
 * Add a work task to this scheduler.
 * @param {int} id the identity of the task
 * @param {int} priority the task's priority
 * @param {Packet} queue the queue of work to be processed by the task
 */

/**
 * Add a handler task to this scheduler.
 * @param {int} id the identity of the task
 * @param {int} priority the task's priority
 * @param {Packet} queue the queue of work to be processed by the task
 */

/**
 * Add a handler task to this scheduler.
 * @param {int} id the identity of the task
 * @param {int} priority the task's priority
 * @param {Packet} queue the queue of work to be processed by the task
 */

/**
 * Add the specified task and mark it as running.
 * @param {int} id the identity of the task
 * @param {int} priority the task's priority
 * @param {Packet} queue the queue of work to be processed by the task
 * @param {Task} task the task to add
 */

/**
 * Add the specified task to this scheduler.
 * @param {int} id the identity of the task
 * @param {int} priority the task's priority
 * @param {Packet} queue the queue of work to be processed by the task
 * @param {Task} task the task to add
 */

/**
 * Execute the tasks managed by this scheduler.
 */

/**
 * Release a task that is currently blocked and return the next block to run.
 * @param {int} id the id of the task to suspend
 */

/**
 * Block the currently executing task and return the next task control block
 * to run.  The blocked task will not be made runnable until it is explicitly
 * released, even if new work is added to it.
 */

/**
 * Suspend the currently executing task and return the next task control block
 * to run.  If new work is added to the suspended task it will be made runnable.
 */

/**
 * Add the specified packet to the end of the worklist used by the task
 * associated with the packet and make the task runnable if it is currently
 * suspended.
 * @param {Packet} packet the packet to add
 */


/**
 * A task control block manages a task and the queue of work packages associated
 * with it.
 * @param {TaskControlBlock} link the preceding block in the linked block list
 * @param {int} id the id of this block
 * @param {int} priority the priority of this block
 * @param {Packet} queue the queue of packages to be processed by the task
 * @param {Task} task the task
 * @constructor
 */


/**
 * The task is running and is currently scheduled.
 */

/**
 * The task has packets left to process.
 */

/**
 * The task is not currently running.  The task is not blocked as such and may
* be started by the scheduler.
 */

/**
 * The task is blocked and cannot be run until it is explicitly released.
 */








/**
 * Runs this task, if it is ready to be run, and returns the next task to run.
 */

/**
 * Adds a packet to the worklist of this block's task, marks this as runnable if
 * necessary, and returns the next runnable object to run (the one
 * with the highest priority).
 */



/**
 * An idle task doesn't do any work itself but cycles control between the two
 * device tasks.
 * @param {Scheduler} scheduler the scheduler that manages this task
 * @param {int} v1 a seed value that controls how the device tasks are scheduled
 * @param {int} count the number of times this task should be scheduled
 * @constructor
 */




/**
 * A task that suspends itself after each time it has been run to simulate
 * waiting for data from an external device.
 * @param {Scheduler} scheduler the scheduler that manages this task
 * @constructor
 */



/**
 * A task that manipulates work packets.
 * @param {Scheduler} scheduler the scheduler that manages this task
 * @param {int} v1 a seed used to specify how work packets are manipulated
 * @param {int} v2 another seed used to specify how work packets are manipulated
 * @constructor
 */



/**
 * A task that manipulates work packets and then suspends itself.
 * @param {Scheduler} scheduler the scheduler that manages this task
 * @constructor
 */




/* --- *
 * P a c k e t
 * --- */


/**
 * A simple package of data that is manipulated by the tasks.  The exact layout
 * of the payload data carried by a packet is not importaint, and neither is the
 * nature of the work performed on packets by the tasks.
 *
 * Besides carrying data, packets form linked lists and are hence used both as
 * data and worklists.
 * @param {Packet} link the tail of the linked list of packets
 * @param {int} id an ID for this packet
 * @param {int} kind the type of this packet
 * @constructor
 */

/**
 * Add this packet to the end of a worklist, and return the worklist.
 * @param {Packet} queue the worklist to add this packet to
 */



if (standalone) {
  Main.runRichards();
}

// })();
