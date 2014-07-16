/* This file is licensed under
 * The MIT License (MIT)
 * 
 * Copyright (c) 2014 Thomas Ettinger and Nathan Sweet, modifying code originally
 * by Nathan Sweet for LibGDX
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using com.badlogic.gdx;
using jl = java.lang;
namespace CU
{
/** Executes tasks in the future on the main loop thread.
 * @author Nathan Sweet */
public class Timer {
	static TimerThread thread;
 private const int CANCELLED = -1;
 private const int FOREVER = -2;

    private List<Task> tasks = new List<Task>();
    public static Timer inst = new Timer();
	static public Timer instance () {
		if (inst == null) {
			inst = new Timer();
            inst.start();
        }
		return inst;
	}

	private Timer () {
		
	}

	/** Schedules a task to occur once as soon as possible, but not sooner than the start of the next frame. */
	public void postTask (Task task) {
		scheduleTask(task, 0, 0, 0);
	}

	/** Schedules a task to occur once after the specified delay. */
	public void scheduleTask (Task task, float delaySeconds) {
		scheduleTask(task, delaySeconds, 0, 0);
	}

	/** Schedules a task to occur once after the specified delay and then repeatedly at the specified interval until cancelled. */
	public void scheduleTask (Task task, float delaySeconds, float intervalSeconds) {
		scheduleTask(task, delaySeconds, intervalSeconds, FOREVER);
	}

	/** Schedules a task to occur once after the specified delay and then a number of additional times at the specified interval. */
	public void scheduleTask (Task task, float delaySeconds, float intervalSeconds, int repeatCount) {
		if (task.repeatCount != CANCELLED) throw new ArgumentException("The same task may not be scheduled twice.");
        task.executeTimeMillis = jl.System.currentTimeMillis() + (long)(delaySeconds * 1000); // System.currentTimeMillis() / 1000000 + (long)(delaySeconds * 1000);
		task.intervalMillis = (long)(intervalSeconds * 1000);
		task.repeatCount = repeatCount;
		//lock (tasks) {
			tasks.Add(task);
		//}
		wake();
	}

	/** Stops the timer, tasks will not be executed and time that passes will not be applied to the task delays. */
	public void kill () {
		//lock (inst) {
            inst = null;
		//}
	}

	/** Starts the timer if it was stopped. */
	public void start () {
		//lock (inst) {
			//checkStatics();
			if (thread == null) thread = new TimerThread();
			wake();
		//}
	}

	/** Cancels all tasks. */
	public void clear () {
		//lock (tasks) {
        for (int i = 0, n = tasks.Count; i < n; i++)
        {
            tasks[i].cancel();
        }
        tasks.Clear();
		//}
	}

	long update (long timeMillis, long waitMillis) {
        lock (tasks)
        {
            for (int i = 0, n = tasks.Count; i < n; i++)
            {
                Task task = tasks[i];
                if (task.executeTimeMillis > timeMillis)
                {
                    waitMillis = Math.Min(waitMillis, task.executeTimeMillis - timeMillis);
                    continue;
                }
                if (task.repeatCount != CANCELLED)
                {
                    if (task.repeatCount == 0)
                    {
                        // Set cancelled before run so it may be rescheduled in run.
                        task.repeatCount = CANCELLED;
                    }
                    Gdx.app.postRunnable(task);

                }
                if (task.repeatCount == CANCELLED)
                {
                    tasks.RemoveAt(i);

                    i--;
                    n--;
                }
                else
                {
                    task.executeTimeMillis = timeMillis + task.intervalMillis;
                    waitMillis = Math.Min(waitMillis, task.intervalMillis);
                    if (task.repeatCount > 0)
                        task.repeatCount--;

                }
            }
        }
		return waitMillis;
	}

	/** Adds the specified delay to all tasks. */
	public void delay (long delayMillis) {
		//lock (tasks) {
			for (int i = 0, n = tasks.Count; i < n; i++)
            {
                tasks[i].executeTimeMillis += delayMillis;
			}
		//}
	}

	static void wake () {
		lock (inst) {
            Monitor.PulseAll(inst);
		}
	}


	/** Runnable with a cancel method.
	 * @see Timer
	 * @author Nathan Sweet */
	abstract public class Task : jl.Runnable{
		public long executeTimeMillis;
		public long intervalMillis;
		public int repeatCount = CANCELLED;

		/** If this is the last time the task will be ran or the task is first cancelled, it may be scheduled again in this method. */
		abstract public void run ();

		/** Cancels the task. It will not be executed until it is scheduled again. This method can be called at any time. */
		public void cancel () {
			executeTimeMillis = 0;
			repeatCount = CANCELLED;
		}

		/** Returns true if this task is scheduled to be executed in the future by a timer. */
		public bool isScheduled () {
			return repeatCount != CANCELLED;
		}

	}

	/** Manages the single timer thread. Stops thread on libgdx application pause and dispose, starts thread on resume.
	 * @author Nathan Sweet */
	class TimerThread
    {
        private Thread t;
		public Application app;
		private long pauseMillis;

		public TimerThread () {
			//Gdx.app.addLifecycleListener(this);
			resume();
		}

		public void run () {
			while (true) {
                if (inst == null) return;
                lock (inst)
                {
                    if (app != Gdx.app) return;

                    long timeMillis = jl.System.currentTimeMillis();
                    int waitMillis = 5000;
                    try
                    {
                        waitMillis = (int)inst.update(timeMillis, waitMillis);
                    }
                    catch (Exception ex)
                    {
                        throw new TimingException("Task failed: Timer" , ex);
                    }


                    if (app != Gdx.app) return;

                    try
                    {
                        if (waitMillis > 0) Monitor.Wait(inst, waitMillis);
                    }
                    catch (ThreadInterruptedException)
                    {
                    }
                }
			}
		}

		public void resume () {
			//long delayMillis = jl.System.currentTimeMillis() - pauseMillis;
			//lock (inst) {
					//inst.delay(delayMillis);
			//}
			app = Gdx.app;
			t = new Thread(run);
            t.Start();
		}

		public void pause () {
			pauseMillis = jl.System.currentTimeMillis();
			//lock (inst) {
				app = null;
				wake();
			//}
		}

		public void dispose () {
			pause();
//			Gdx.app.removeLifecycleListener(this);
			thread = null;
			inst = null;
		}
	}

	private static void checkStatics () {
		// fix for https://github.com/libgdx/libgdx/issues/1548, in case the statics are left overs...
		//lock (inst) {
			if (thread != null) {
				if (thread.app != Gdx.app) {
					thread = null;
					inst = null;
				}
			}
		//}
	}
}
[Serializable]
public class TimingException : Exception
{
    public TimingException() { }
    public TimingException(string message) : base(message) { }
    public TimingException(string message, Exception inner) : base(message, inner) { }
    protected TimingException(
      System.Runtime.Serialization.SerializationInfo info,
      System.Runtime.Serialization.StreamingContext context)
        : base(info, context) { }
}
}
