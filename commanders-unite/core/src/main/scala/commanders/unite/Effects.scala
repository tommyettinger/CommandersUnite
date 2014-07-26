package commanders.unite

import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Timer
import game.commanders.unite.CommandersUnite

/**
 * Created by Tommy Ettinger on 7/25/2014.
 */
case class Speech(x:Int, y:Int, large:Boolean, text:String)(var worldX:Float = 64 + x * 64 + y * 64, var worldY:Float = 146 + x * 32 - y * 32){}
object Effects
{
  var oldpos = CommandersUnite.game.camera.position
  var newpos= oldpos
  var midpos = oldpos
  def CenterCamera(gridX:Float, gridY:Float, stepPortion:Float)
  {
    oldpos = new Vector3(CommandersUnite.game.camera.position);
    newpos = new Vector3(64 * (gridX + gridY), 32 * (gridX - gridY) + 32, 0);
    val n = new Timer.Task{ def run() {
      midpos = midpos.add(((newpos.x - oldpos.x) / 16F).toInt, ((newpos.y - oldpos.y) / 16F).toInt, 0);
      CommandersUnite.game.camera.position.set(midpos)
      CommandersUnite.game.camera.update()
    }}
    Timer.instance().scheduleTask(n, 0, stepPortion * CommandersUnite.updateStep / 16F, 15);
    Timer.instance().start()
  }
  def CenterCamera(pos:Position, stepPortion:Float)
  {
    CenterCamera(pos.x, pos.y, stepPortion);
  }
}
