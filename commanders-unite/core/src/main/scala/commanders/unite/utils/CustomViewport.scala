package commanders.unite.utils

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.Viewport
;

/**
 * Created by Tommy Ettinger on 8/1/2014.
 */
class CustomViewport(var minWorldWidth:Float,
var minWorldHeight:Float,
var cam:Camera = new OrthographicCamera()) extends Viewport
{
  camera = cam
  override def update (screenWidth:Int, screenHeight:Int, centerCamera:Boolean) {
    worldWidth = minWorldWidth
    worldHeight = minWorldHeight
    viewportWidth = (worldWidth * Math.floor(screenWidth / worldWidth)).toInt
    viewportHeight = (worldHeight * Math.floor(screenHeight / worldHeight)).toInt
    viewportX = (screenWidth - viewportWidth) / 2
    viewportY = (screenHeight - viewportHeight) / 2
    super.update(screenWidth, screenHeight, centerCamera)
  }
}