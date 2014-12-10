package commanders.unite

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d._
import com.badlogic.gdx.scenes.scene2d.ui._
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent
import game.commanders.unite.CommandersUnite
import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

/**
 * Created by Tommy Ettinger on 7/25/2014.
 */
object UI
{
  var skin = new Skin()
  var stage = new Stage()
  def formatActors()
  { val it = stage.getActors.iterator()
    while(it.hasNext)
    {
      var a = it.next
      a.setScale(Math.round(Gdx.graphics.getDensity * 1.5))
      a.setX(Gdx.graphics.getWidth - a.getWidth * 3 * Gdx.graphics.getDensity * 1.5f)
      a.setY(Gdx.graphics.getHeight / 2 - a.getHeight * Gdx.graphics.getDensity * 1.5f)
    }
  }
  def makeMenu(entries: ArrayBuffer[MenuEntry], color: Int): ScrollPane =
  {
    val vg = new VerticalGroup()
    val sp = new ScrollPane(vg)
    for (ent <- entries) {
      val btn = new TextButton(ent.text, skin, "color" + color)
      btn.addListener(new Changer(new Runnable
      {
        override def run(): Unit =
        {
          btn.setDisabled(true); sp.setVisible(false); ent.action.run()
        }
      }))
      vg.addActor(btn)
    }
    val end = new TextButton("End", skin, "color" + color)
    end.addListener(new Changer(new Runnable
    {
      override def run(): Unit =
      {
        sp.setVisible(false); Logic.advanceTurn(); sp.remove();
      }
    }))
    vg.addActor(end)
    sp
  }

  def postActor(a: Actor)
  {
//    a.setX(700)
//    a.setY(360)
    stage.addActor(a)
    formatActors()

  }

  def postActor(a: Actor, x: Float, y: Float)
  {
    stage.addActor(a)
    formatActors()
  }

  def draw()
  {
    stage.getBatch().begin()
    if (Logic.state == GameState.Paused) {
//      largeFont.setColor(Color.BLACK); //(sp.large ? largeFont : font).
//      font.setColor(Color.BLACK); //(sp.large ? largeFont : font).
      val bigLabel = new Label("PAUSED", skin, "title-text")
      bigLabel.setX(Gdx.graphics.getWidth / 2 + 12)
      bigLabel.setY(Gdx.graphics.getHeight / 2 + 50)
      val smallLabel = new Label("Press Space to continue", skin, "normal-text")
      smallLabel.setX(Gdx.graphics.getWidth / 2 - 46)
      smallLabel.setY(Gdx.graphics.getHeight / 2)

      bigLabel.draw(stage.getBatch(), 1.0f)
      smallLabel.draw(stage.getBatch(), 1.0f)
    }
    for (sp <- Logic.speaking) {
      val speak = new Label(sp.text, skin, (if (sp.large) "title-text" else "normal-text"))
      val vec = CommandersUnite.game.camera.project(new Vector3(sp.worldX, sp.worldY, 0))
      speak.setX(vec.x - (sp.text.length * (if (sp.large) 8 else 4) * Gdx.graphics.getDensity * 1.5f))
      speak.setY( if (sp.large) vec.y else vec.y - 32 * Gdx.graphics.getDensity * 1.5f)
      speak.draw(stage.getBatch(), 1.0f)
    }
    stage.getBatch().end()
    stage.draw()
  }
}

case class MenuEntry(text: String, action: Runnable)
{}

class Changer(n: Runnable) extends ChangeListener
{
  override def changed(evt: ChangeEvent, actor: Actor)
  {
    n.run()
    evt.stop();
  }
}
