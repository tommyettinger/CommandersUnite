package commanders.unite

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d._
import com.badlogic.gdx.scenes.scene2d.ui._
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent
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
      a.setX(Gdx.graphics.getWidth - a.getWidth * 3)
      a.setY(Gdx.graphics.getHeight / 2 - a.getHeight / 2)
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
