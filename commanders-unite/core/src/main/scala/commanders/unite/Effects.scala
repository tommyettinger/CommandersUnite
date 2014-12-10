package commanders.unite

import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.TimeUtils
import commanders.unite.utils.{SmoothAction, RepeatedAction}
import game.commanders.unite.CommandersUnite

/**
 * Created by Tommy Ettinger on 7/25/2014.
 */
case class Speech(x: Int, y: Int, large: Boolean, text: String)(var worldX: Float = 64 + x * 64 + y * 64, var worldY: Float = 100 + x * 32 - y * 32)
{}

object Effects
{
  var oldpos = CommandersUnite.game.camera.position
  var newpos = oldpos
  var midpos = oldpos
  var tempV = new Vector3()
  /*val n = new utils.Timer.Task
    {
      def run()
      {
        midpos = midpos.add(((newpos.x - oldpos.x) / CommandersUnite.smoothMove.toFloat).toInt, ((newpos.y - oldpos.y) / CommandersUnite.smoothMove.toFloat).toInt, 0);
        CommandersUnite.game.camera.position.set(midpos)
        CommandersUnite.game.camera.update()
      }
    }*/
  /*
      CommandersUnite.repeatedActions += RepeatedAction(() =>
  {        midpos = midpos.add(((newpos.x - oldpos.x) / CommandersUnite.smoothMove.toFloat).toInt, ((newpos.y - oldpos.y) / CommandersUnite.smoothMove.toFloat).toInt, 0);
    CommandersUnite.game.camera.position.set(midpos)
    CommandersUnite.game.camera.update()
  }, 0, stepPortion * CommandersUnite.updateStep / CommandersUnite.smoothMove.toFloat, CommandersUnite.smoothMove)
   */

  def CenterCamera(gridX: Float, gridY: Float, stepPortion: Float)
  {
    oldpos = new Vector3(CommandersUnite.game.camera.position);
    newpos = new Vector3(20 + 64 * (gridX + gridY), 6 + 32 * (gridX - gridY) + 32, 0);

    CommandersUnite.smoothActions += SmoothAction((t : Float) =>
    {
      tempV.x = oldpos.x
      tempV.y = oldpos.y
      tempV.z = 0
      midpos = tempV.lerp(newpos, t)
      CommandersUnite.game.camera.position.set(midpos)
      CommandersUnite.game.camera.update()
    }, TimeUtils.millis(), TimeUtils.millis() + (CommandersUnite.updateMillis * stepPortion).toLong - 20)
    //utils.Timer.instance.scheduleTask(n, 0, stepPortion * CommandersUnite.updateStep / CommandersUnite.smoothMove.toFloat, CommandersUnite.smoothMove - 1);
    //utils.Timer.instance.start()
  }

  def CenterCamera(pos: Position, stepPortion: Float)
  {
    CenterCamera(pos.x, pos.y, stepPortion)
  }
}
