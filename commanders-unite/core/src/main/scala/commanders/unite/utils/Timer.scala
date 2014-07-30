package commanders.unite.utils

import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.{Application, Gdx}

import scala.collection.mutable.ArrayBuffer

/** *****************************************************************************
  * Copyright 2011 Nathan Sweet.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * *****************************************************************************/

class Timer()
{
  private final val tasks = new ArrayBuffer[Timer.Task](8)
  start()

  /** Schedules a task to occur once as soon as possible, but not sooner than the start of the next frame. */
  def postTask(task: Timer.Task)
  {
    scheduleTask(task, 0, 0, 0)
  }

  /** Schedules a task to occur once after the specified delay. */
  def scheduleTask(task: Timer.Task, delaySeconds: Float)
  {
    scheduleTask(task, delaySeconds, 0, 0)
  }

  /** Schedules a task to occur once after the specified delay and then repeatedly at the specified interval until cancelled. */
  def scheduleTask(task: Timer.Task, delaySeconds: Float, intervalSeconds: Float)
  {
    scheduleTask(task, delaySeconds, intervalSeconds, Timer.FOREVER)
  }

  /** Schedules a task to occur once after the specified delay and then a number of additional times at the specified interval. */
  def scheduleTask(task: Timer.Task, delaySeconds: Float, intervalSeconds: Float, repeatCount: Int)
  {
    if (task.repeatCount != Timer.CANCELLED) throw new IllegalArgumentException("The same task may not be scheduled twice.")
    task.executeTimeMillis = System.nanoTime / 1000000 + (delaySeconds * 1000).asInstanceOf[Long]
    task.intervalMillis = (intervalSeconds * 1000).asInstanceOf[Long]
    task.repeatCount = repeatCount
    tasks.synchronized {
      tasks += task
    }
    Timer.wake
  }

  /** Stops the timer, tasks will not be executed and time that passes will not be applied to the task delays. */
  def stop()
  {
    Timer.instances synchronized {
      Timer.instances -= this
    }
  }

  /** Starts the timer if it was stopped. */
  def start()
  {
    Timer.instances synchronized {
      //Timer.checkStatics
      if (Timer.instances.contains(this)) return
      Timer.instances += this
      if (Timer.thread == null) Timer.thread = new Timer.TimerThread
      Timer.wake
    }
  }

  def resume()
  {
    if (Timer.thread == null) {
      return
    }
    Timer.thread.resume()
  }

  def pause()
  {
    if (Timer.thread == null) {
      return
    }
    Timer.thread.pause()
  }

  /** Cancels all tasks. */
  def clear()
  {
    tasks synchronized {
      {
        var i: Int = 0
        val n: Int = tasks.size
        while (i < n) {
          tasks(i).cancel
          i += 1
        }
      }
      tasks.clear
    }
  }

  private[utils] def update(timeMillis: Long, waitMilliseconds: Long): Long =
  {
    var waitMillis = waitMilliseconds
    tasks synchronized {
      {
        var i: Int = 0
        var n: Int = tasks.size
        while (i < n) {
          {
            val task: Timer.Task = tasks(i)
            if (task.executeTimeMillis > timeMillis) {
              waitMillis = Math.min(waitMillis, task.executeTimeMillis - timeMillis)
            }
            else {
              if (task.repeatCount != Timer.CANCELLED) {
                if (task.repeatCount == 0) {
                  task.repeatCount = Timer.CANCELLED
                }
                Gdx.app.postRunnable(task)
              }
              if (task.repeatCount == Timer.CANCELLED) {
                tasks.remove(i)
                i -= 1
                n -= 1
              }
              else {
                task.executeTimeMillis = timeMillis + task.intervalMillis
                waitMillis = Math.min(waitMillis, task.intervalMillis)
                if (task.repeatCount > 0) {
                  task.repeatCount -= 1;
                }
              }
            }
          }
          i += 1
        }
      }
    }
    waitMillis
  }

  /** Adds the specified delay to all tasks. */
  def delay(delayMillis: Long)
  {
    tasks synchronized {
      {
        var i: Int = 0
        val n: Int = tasks.size
        while (i < n) {
          {
            val task: Timer.Task = tasks(i)
            task.executeTimeMillis += delayMillis
          }
          i += 1
        }
      }
    }
  }
}


/** Executes tasks in the future on the main loop thread.
  * @author Nathan Sweet */
object Timer
{

  private[utils] final val instances = new ArrayBuffer[Timer](1)
  private[utils] var thread: Timer.TimerThread = null
  private[utils] final val CANCELLED: Int = -1
  private[utils] final val FOREVER: Int = -2
  /** Timer instance for general application wide usage. Static methods on {@link Timer} make convenient use of this instance. */
  var inst: Timer = null

  def instance: Timer =
  {
    //checkStatics
    if (inst == null) {
      inst = new Timer()
    }
    this.inst
  }

  private[utils] def wake
  {
    instances.synchronized {
      instances.notifyAll
    }
  }

  /** Schedules a task on {@link #instance}.
    * @see #postTask(Task) */
  def post(task: Timer.Task)
  {
    instance.postTask(task)
  }

  /** Schedules a task on {@link #instance}.
    * @see #scheduleTask(Task, float) */
  def schedule(task: Timer.Task, delaySeconds: Float)
  {
    instance.scheduleTask(task, delaySeconds)
  }

  /** Schedules a task on {@link #instance}.
    * @see #scheduleTask(Task, float, float) */
  def schedule(task: Timer.Task, delaySeconds: Float, intervalSeconds: Float)
  {
    instance.scheduleTask(task, delaySeconds, intervalSeconds)
  }

  /** Schedules a task on {@link #instance}.
    * @see #scheduleTask(Task, float, float, int) */
  def schedule(task: Timer.Task, delaySeconds: Float, intervalSeconds: Float, repeatCount: Int)
  {
    instance.scheduleTask(task, delaySeconds, intervalSeconds, repeatCount)
  }

  private def checkStatics
  {
    instances synchronized {
      if (thread != null) {
        if (thread.app ne Gdx.app) {
          thread = null
          instances.clear
          inst = null
        }
      }
    }
  }

  /** Runnable with a cancel method.
    * @see Timer
    * @author Nathan Sweet */
  abstract class Task extends Runnable
  {
    /** If this is the last time the task will be ran or the task is first cancelled, it may be scheduled again in this method. */
    def run()

    /** Cancels the task. It will not be executed until it is scheduled again. This method can be called at any time. */
    def cancel()
    {
      executeTimeMillis = 0
      repeatCount = CANCELLED
    }

    /** Returns true if this task is scheduled to be executed in the future by a timer. */
    def isScheduled: Boolean =
    {
      repeatCount != CANCELLED
    }

    /** Returns the time when this task will be executed in milliseconds */
    def getExecuteTimeMillis: Long =
    {
      executeTimeMillis
    }

    private[utils] var executeTimeMillis: Long = 0L
    private[utils] var intervalMillis: Long = 0L
    private[utils] var repeatCount: Int = CANCELLED
  }

  /** Manages the single timer thread. Stops thread on libgdx application pause and dispose, starts thread on resume.
    * @author Nathan Sweet */
  private[utils] class TimerThread() extends Runnable
  {
    private[utils] var app: Application = null
    private var pauseMillis: Long = System.nanoTime / 1000000
    //Gdx.app.addLifecycleListener(this)
    this.resume()

    def run()
    {
      while (true) {
        instances synchronized {
          if (app ne Gdx.app) return
          val timeMillis: Long = System.nanoTime / 1000000
          var waitMillis: Long = 5000
          var i: Int = 0
          val n: Int = instances.size
          while (i < n) {
            try {
              waitMillis = instances(i).update(timeMillis, waitMillis)
            }
            catch {
              case ex: Throwable =>
                throw new GdxRuntimeException("Task failed: " + instances(i).getClass.getName, ex)
            }
            i += 1
          }
          if (app ne Gdx.app) return
          try {
            if (waitMillis > 0) instances.wait(waitMillis)
          }
          catch {
            case ignored: InterruptedException => {
            }
          }
        }
      }
    }

    def resume()
    {
      val delayMillis: Long = System.nanoTime / 1000000 - pauseMillis
      instances synchronized {
        {
          var i: Int = 0
          val n: Int = instances.size
          while (i < n) {
            instances(i).delay(delayMillis)
            i += 1
          }
        }
      }
      app = Gdx.app
      new Thread(this, "Timer").start()
    }

    def pause()
    {
      pauseMillis = System.nanoTime / 1000000
      instances synchronized {
        app = null
        wake
      }
    }

    def dispose()
    {
      pause()
      //Gdx.app.removeLifecycleListener(this)
      thread = null
      instances.clear()
      inst = null
    }

  }

}
