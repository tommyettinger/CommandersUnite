package commanders.unite.utils

/**
 * Created by Tommy Ettinger on 8/2/2014.
 */
case class RepeatedAction(run : () => Unit, var delaySeconds:Float = 0, var intervalSeconds:Float = 0, var repeatCount : Int = 0)
{
  var counter = 0f
  var repeatTotal = 0
}
case class SmoothAction(run : Float => Unit, startTime : Long, endTime : Long)
{
}
//(current time in millis - start time) / (end time - start time)