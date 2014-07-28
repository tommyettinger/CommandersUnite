package game.commanders.unite

import java.lang.Exception

import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.graphics._
import com.badlogic.gdx._
import com.badlogic.gdx.graphics.g2d._
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.{GdxRuntimeException}
import com.badlogic.gdx.utils.viewport._
import commanders.unite._
import commanders.unite.utils.Timer

import scala.util.control.Exception

class CommandersUnite extends Game
{
  var camera:OrthographicCamera = new OrthographicCamera()
  override def create()
  {
    camera = new OrthographicCamera(Gdx.graphics.getWidth, Gdx.graphics.getHeight)
    CommandersUnite.game = this
    this.setScreen(new GameScreen)
  }
  override def dispose()
  {
    this.getScreen().dispose()
    CommandersUnite.game = null
    this.setScreen(null)
    Gdx.app.exit()
    System.exit(0)
  }
  override def pause()
  {
    this.getScreen().pause()
  }
  override def resume()
  {
    this.getScreen().resume()
  }
  class GameScreen extends Screen
  {
    val width=24
    val height=24
    val parts = Array(
      new Texture(Gdx.files.internal("pack.png"), Pixmap.Format.RGBA8888, false),
      new Texture(Gdx.files.internal("pack2.png"), Pixmap.Format.RGBA8888, false))
    var currentFrame:TextureAtlas.AtlasRegion = null
    val atlas = new TextureAtlas(Gdx.files.internal("pack.atlas"))
    val palette = new Texture(Gdx.files.internal("PaletteDark.png"), Pixmap.Format.RGBA8888, false);
    var shader:ShaderProgram = createChannelShader()
    val font = new BitmapFont(Gdx.files.internal("Monology.fnt"));
    val largeFont = new BitmapFont(Gdx.files.internal("MonologyLarge.fnt"));
    Logic.PlacePieces();
    palette.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

    var terrains = Array.ofDim[TextureRegion](11, 2)
    for (i <- 0 until 11)
    {
      terrains(i)(0) = atlas.findRegion(LocalMap.Terrains(i)); //"Terrain/" +
      terrains(i)(1) = atlas.findRegion(LocalMap.Terrains(i) + "_bold")
    }
    var pieces = new Array[Array[Array[Array[TextureAtlas.AtlasRegion]]]](Piece.CurrentPieces.length)
    var animations = new Array[Array[Array[Animation]]](Piece.CurrentPieces.length)
    val clear = atlas.findRegion("clear")
    val clearLarge = atlas.findRegion("clear_large")
    for(name <- Piece.CurrentPieces)
    {
      pieces(Piece.PieceLookup(name)) = new Array[Array[Array[TextureAtlas.AtlasRegion]]](4)
      animations(Piece.PieceLookup(name)) = new Array[Array[Animation]](4)

      for (dir <- 0 until 4)
      {
        pieces(Piece.PieceLookup(name))(dir) = new Array[Array[TextureAtlas.AtlasRegion]](if(Piece.AllMobilities(Piece.PieceLookup(name)) == MovementType.Immobile) 2 else 6)
        for (j <- 0 until (if(Piece.AllMobilities(Piece.PieceLookup(name)) == MovementType.Immobile) 2 else 6))
        {
          j match {
            case 0=>
            pieces(Piece.PieceLookup(name))(dir)(j) = new Array[TextureAtlas.AtlasRegion](if(Piece.AllMobilities(Piece.PieceLookup(name)) == MovementType.Immobile) 2 else 4)
            for (k <- 0 until (if(Piece.AllMobilities(Piece.PieceLookup(name)) == MovementType.Immobile) 2 else 4))
            {
              pieces(Piece.PieceLookup(name))(dir)(j)(k) = atlas.findRegion(name + "_face" + dir, k)
            }
            case 1=>
              pieces(Piece.PieceLookup(name))(dir)(j) = new Array[TextureAtlas.AtlasRegion](9)
              for(k <- 0 until 8)
              {
                pieces(Piece.PieceLookup(name))(dir)(j)(k) = atlas.findRegion(name + "_Explode_face" + dir, k)
              }
              pieces(Piece.PieceLookup(name))(dir)(j)(8) = clearLarge
            case 2=>
              pieces(Piece.PieceLookup(name))(dir)(j) = new Array[TextureAtlas.AtlasRegion](if(Piece.AllMobilities(Piece.PieceLookup(name)) == MovementType.Immobile) 2 else 16)
            for (k <- 0 until (if(Piece.AllMobilities(Piece.PieceLookup(name)) == MovementType.Immobile) 2 else 16))
            {
              if (Piece.WeaponDisplays(Piece.PieceLookup(name))._1 > - 1)
              {
                pieces(Piece.PieceLookup(name))(dir)(j)(k) = atlas.findRegion(name + "_Attack_0_face" + dir, k)
              }
              else
              {
                pieces(Piece.PieceLookup(name))(dir)(j)(k) = atlas.findRegion(name + "_face" + dir, k % 4)
              }
            }
            case 3=>
              pieces(Piece.PieceLookup(name))(dir)(j) = new Array[TextureAtlas.AtlasRegion](if(Piece.AllMobilities(Piece.PieceLookup(name)) == MovementType.Immobile) 2 else 16)
              for (k <- 0 until (if(Piece.AllMobilities(Piece.PieceLookup(name)) == MovementType.Immobile) 2 else 16))
            {
              if (Piece.WeaponDisplays(Piece.PieceLookup(name))._2 > - 1)
              {
                pieces(Piece.PieceLookup(name))(dir)(j)(k) = atlas.findRegion(name + "_Attack_1_face" + dir, k)
              }
              else
              {
                pieces(Piece.PieceLookup(name))(dir)(j)(k) = atlas.findRegion(name + "_face" + dir, k % 4)
              }
            }
            case 4=>
          pieces(Piece.PieceLookup(name))(dir)(j) = new Array[TextureAtlas.AtlasRegion](if(Piece.AllMobilities(Piece.PieceLookup(name)) == MovementType.Immobile) 2 else 16)
              for (k <- 0 until (if(Piece.AllMobilities(Piece.PieceLookup(name)) == MovementType.Immobile) 2 else 16))
            {
              if (Piece.WeaponDisplays(Piece.PieceLookup(name))._1 > - 1)
              {
                pieces(Piece.PieceLookup(name))(dir)(j)(k) = atlas.findRegion(name + "_Receive_0_face" + dir, k)
              }
              else
              {
                pieces(Piece.PieceLookup(name))(dir)(j)(k) = clearLarge;
              }
            }
            case 5=>
              pieces(Piece.PieceLookup(name))(dir)(j) = new Array[TextureAtlas.AtlasRegion](if(Piece.AllMobilities(Piece.PieceLookup(name)) == MovementType.Immobile) 2 else 16)
              for (k <- 0 until (if(Piece.AllMobilities(Piece.PieceLookup(name)) == MovementType.Immobile) 2 else 16))
            {
              if (Piece.WeaponDisplays(Piece.PieceLookup(name))._2 > - 1)
              {
                pieces(Piece.PieceLookup(name))(dir)(j)(k) = atlas.findRegion(name + "_Receive_1_face" + dir, k);
              }
              else
              {
                pieces(Piece.PieceLookup(name))(dir)(j)(k) = clearLarge;
              }
            }
          }
        }
        animations(Piece.PieceLookup(name))(dir) = new Array[Animation](if(Piece.AllMobilities(Piece.PieceLookup(name)) == MovementType.Immobile) 2 else 6)
        for (j <- 0 until (if(Piece.AllMobilities(Piece.PieceLookup(name)) == MovementType.Immobile) 2 else 6))
        {
          j match {
            case 0=> animations(Piece.PieceLookup(name))(dir)(j) = new Animation((if(Piece.AllMobilities(Piece.PieceLookup(name)) == MovementType.Immobile) 0.4F else 0.15F),
            pieces(Piece.PieceLookup(name))(dir)(j): _*)
            case 1=> animations(Piece.PieceLookup(name))(dir)(j) = new Animation(0.11F,
              pieces(Piece.PieceLookup(name))(dir)(j): _*)
            //                                units[Piece.PieceLookup(name)][i][j].Concat(new TextureRegion[] {units[Piece.PieceLookup(name)][i][j][7],  }).ToArray());
            case 2=> if (Piece.WeaponDisplays(Piece.PieceLookup(name))._1 > - 1)
            {
              animations(Piece.PieceLookup(name))(dir)(j) = new Animation(0.11F,
              pieces(Piece.PieceLookup(name))(dir)(j): _*)
            }
            case 3=> if (Piece.WeaponDisplays(Piece.PieceLookup(name))._2 > - 1)
                     {
                       animations(Piece.PieceLookup(name))(dir)(j) = new Animation(0.11F,
                         pieces(Piece.PieceLookup(name))(dir)(j): _*)
                     }
            case 4=>if (Piece.WeaponDisplays(Piece.PieceLookup(name))._1 > - 1)
                    {
                      animations(Piece.PieceLookup(name))(dir)(j) = new Animation(0.11F,
                        pieces(Piece.PieceLookup(name))(dir)(j): _*)
                    }
            case 5=> if (Piece.WeaponDisplays(Piece.PieceLookup(name))._2 > - 1)
          {
          animations(Piece.PieceLookup(name))(dir)(j) = new Animation(0.11F,
          pieces(Piece.PieceLookup(name))(dir)(j): _*)
          }
          }
        }
      }

    }

    CommandersUnite.game.camera.position.set(Gdx.graphics.getWidth / 2, Gdx.graphics.getHeight / 2, 0)
    CommandersUnite.game.camera.setToOrtho(false, 1280, 720)
    CommandersUnite.game.camera.update()
    val batch = new SpriteBatch()

    CommandersUnite.cursor = Position(Logic.ActivePiece.x, Logic.ActivePiece.y)
    UI.skin = new Skin(Gdx.files.internal("ui.json"), new TextureAtlas(Gdx.files.internal("ui.atlas")))
    UI.stage = new Stage(new ScreenViewport())
    var inp:InputProc = new InputProc()
    var multi = new InputMultiplexer(UI.stage, inp)
    Gdx.input.setInputProcessor(multi)
    Timer.instance.scheduleTask(new Timer.Task{ def run() {Logic.ProcessStep}}, 0F, CommandersUnite.updateStep)
  override def render (delta : Float) {
    CommandersUnite.initialized = true
    Timer.instance.start()
    Gdx.gl.glClearColor(0.45F, 0.7F, 1f, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    camera.update()
    if (Logic.state == GameState.Paused) {}
    else if (Logic.state == GameState.NPC_Play || Logic.state == GameState.PC_Play_Move || Logic.state == GameState.PC_Play_Action) {
      CommandersUnite.stateTime += Gdx.graphics.getDeltaTime()
      CommandersUnite.attackTime += Gdx.graphics.getDeltaTime()
      CommandersUnite.explodeTime += Gdx.graphics.getDeltaTime()
      CommandersUnite.receiveTime += Gdx.graphics.getDeltaTime()
      UI.stage.act(Gdx.graphics.getDeltaTime())
    }
    else if (Logic.state == GameState.PC_Select_Move || Logic.state == GameState.PC_Select_UI || Logic.state == GameState.PC_Select_Action) {
      CommandersUnite.stateTime += Gdx.graphics.getDeltaTime();
    }
    batch.setProjectionMatrix(camera.combined);

    //shader.begin();

    var faction = new Color()
    faction.a = 1F
    faction.b = 0.5F
    faction.g = 0.9F
    faction.r = 9 / 32F

    batch.setShader(shader)
    Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
    batch.begin()

    palette.bind(3)

    shader.setUniformi("u_texPalette", 3)
    parts(0).bind(2)
    shader.setUniformi("u_texture", 2)

    faction.r = 10 / 32.0F
    batch.setColor(faction)
    //            for (int h = 0; h < height; h++)
    for (row <- 0 until width + height)
    {
      for (col <- 0 to (if(row < width) row else (width + height - 1) - row))
      {
        var w = (if(row < width) width - 1 - row + col else col) //height + (width - 1 - row) +
        var h = (if(row < width) col else row - width + col)
        var boldness = 0
        var highlighter:Float = 10
        Logic.FieldMap.Highlight(w)(h) match {
          case HighlightType.Bright=>
            boldness = 1
          case HighlightType.Dim=>
            boldness = 0
            highlighter = 19 //(((int)((stateTime) % 2)) == 0) ? 19 : 10;
          case HighlightType.Plain=>
            boldness = 0
          highlighter = 10
          case HighlightType.Spectrum=>
            boldness = 1
            highlighter = 13 + ((CommandersUnite.stateTime * 9) % 6).toInt
        }
        if (Logic.state == GameState.PC_Select_Move && Logic.FieldMap.Highlight(w)(h) != HighlightType.Dim && CommandersUnite.cursor.x == w && CommandersUnite.cursor.y == h) {
          boldness = 1;
          highlighter = 13 + ((CommandersUnite.stateTime * 9) % 6).toInt
        }
        faction.r = highlighter / 32.0F
        batch.setColor(faction)
        batch.draw(terrains(Logic.FieldMap.Land(w)(h))(boldness), w * 64 + h * 64, w * 32 - h * 32)
      }
    }
    for (row <- 0 until width + height)
    {
      for (col <- 0 to (if(row < width) row else (width + height - 1) - row))
      {
        var w = (if(row < width) width - 1 - row + col else col)
        var h = (if(row < width) col else row - width + col)
        if (Logic.PieceGrid(w)(h) != null) {
          faction.r = (Logic.PieceGrid(w)(h).color + 1) / 32.0F;
          batch.setColor(faction);
          Logic.PieceGrid(w)(h).visual match {
            case VisualAction.Normal=>
            currentFrame = animations(Logic.PieceGrid(w)(h).unitIndex)(Logic.PieceGrid(w)(h).facingNumber)(0).getKeyFrame(CommandersUnite.stateTime, true).asInstanceOf[TextureAtlas.AtlasRegion]
            batch.draw(currentFrame, (Logic.PieceGrid(w)(h).worldX).toInt + currentFrame.offsetX, (Logic.PieceGrid(w)(h).worldY).toInt + currentFrame.offsetY + LocalMap.Depths(Logic.FieldMap.Land(w)(h)) * 3)
            case VisualAction.Exploding=>
              currentFrame = animations(Logic.PieceGrid(w)(h).unitIndex)(Logic.PieceGrid(w)(h).facingNumber)(1).getKeyFrame(CommandersUnite.explodeTime, false).asInstanceOf[TextureAtlas.AtlasRegion]
              batch.draw(currentFrame, (Logic.PieceGrid(w)(h).worldX - 80).toInt + currentFrame.offsetX, (Logic.PieceGrid(w)(h).worldY - 40).toInt + currentFrame.offsetY + LocalMap.Depths(Logic.FieldMap.Land(w)(h)) * 3)
            case VisualAction.Firing=>
            if (Piece.WeaponDisplays(Logic.PieceGrid(w)(h).unitIndex)._1 > - 1)
            {
              currentFrame = animations(Logic.PieceGrid(w)(h).unitIndex)(Logic.PieceGrid(w)(h).facingNumber)(2).getKeyFrame(CommandersUnite.attackTime, false).asInstanceOf[TextureAtlas.AtlasRegion]
              batch.draw(currentFrame, (Logic.PieceGrid(w)(h).worldX - 80).toInt + currentFrame.offsetX, (Logic.PieceGrid(w)(h).worldY - 40).toInt + currentFrame.offsetY + LocalMap.Depths(Logic.FieldMap.Land(w)(h)) * 3)
            }
            else
            {
              currentFrame = animations(Logic.PieceGrid(w)(h).unitIndex)(Logic.PieceGrid(w)(h).facingNumber)(0).getKeyFrame(CommandersUnite.stateTime, true).asInstanceOf[TextureAtlas.AtlasRegion]
              batch.draw(currentFrame, (Logic.PieceGrid(w)(h).worldX).toInt + currentFrame.offsetX, (Logic.PieceGrid(w)(h).worldY).toInt + currentFrame.offsetY + LocalMap.Depths(Logic.FieldMap.Land(w)(h)) * 3)
            }
          }

          for(dp <- Logic.ActivePiece.targeting)
          {
            if (Logic.currentlyFiring > -1 && dp.p.x == w && dp.p.y == h) {
              val tx = 20 - 80 + dp.p.x * 64 + dp.p.y * 64
              val ty = 6 - 40 + dp.p.x * 32 - dp.p.y * 32
              currentFrame = animations(Logic.ActivePiece.unitIndex)(Logic.ConvertDirection(dp.dir))(4+Logic.currentlyFiring).getKeyFrame(CommandersUnite.receiveTime, false).asInstanceOf[TextureAtlas.AtlasRegion]
              batch.draw(currentFrame, tx + currentFrame.offsetX, ty + currentFrame.offsetY + LocalMap.Depths(Logic.FieldMap.Land(dp.p.x)(dp.p.y)) * 3)
            }
          }
        }
        if (Logic.ActivePiece.x == w && Logic.ActivePiece.y == h) {
          faction.r = (Logic.ActivePiece.color + 1) / 32.0F;
          batch.setColor(faction);
          //[Logic.ReverseColors[Logic.ActivePiece.color]]

          Logic.ActivePiece.visual match {
            case VisualAction.Normal=>
              currentFrame = animations(Logic.ActivePiece.unitIndex)(Logic.ActivePiece.facingNumber)(0).getKeyFrame(CommandersUnite.stateTime, true).asInstanceOf[TextureAtlas.AtlasRegion]
              batch.draw(currentFrame, (Logic.ActivePiece.worldX).toInt + currentFrame.offsetX, (Logic.ActivePiece.worldY).toInt + currentFrame.offsetY + LocalMap.Depths(Logic.FieldMap.Land(w)(h)) * 3)
            case VisualAction.Exploding=>
              currentFrame = animations(Logic.ActivePiece.unitIndex)(Logic.ActivePiece.facingNumber)(1).getKeyFrame(CommandersUnite.explodeTime, false).asInstanceOf[TextureAtlas.AtlasRegion]
              batch.draw(currentFrame, (Logic.ActivePiece.worldX - 80).toInt + currentFrame.offsetX, (Logic.ActivePiece.worldY - 40).toInt + currentFrame.offsetY + LocalMap.Depths(Logic.FieldMap.Land(w)(h)) * 3)
            case VisualAction.Firing=>
            if (Logic.currentlyFiring > -1) {
              currentFrame = animations(Logic.ActivePiece.unitIndex)(Logic.ActivePiece.facingNumber)(2+Logic.currentlyFiring).getKeyFrame(CommandersUnite.attackTime, false).asInstanceOf[TextureAtlas.AtlasRegion]
              batch.draw(currentFrame, (Logic.ActivePiece.worldX - 80).toInt + currentFrame.offsetX, (Logic.ActivePiece.worldY - 40).toInt + currentFrame.offsetY + LocalMap.Depths(Logic.FieldMap.Land(w)(h)) * 3)
            }
            else {
              currentFrame = animations(Logic.ActivePiece.unitIndex)(Logic.ActivePiece.facingNumber)(0).getKeyFrame(CommandersUnite.stateTime, true).asInstanceOf[TextureAtlas.AtlasRegion]
              batch.draw(currentFrame, (Logic.ActivePiece.worldX).toInt + currentFrame.offsetX, (Logic.ActivePiece.worldY).toInt + currentFrame.offsetY + LocalMap.Depths(Logic.FieldMap.Land(w)(h)) * 3)
            }
          }
        }
      }
    }
    if (Logic.state == GameState.Paused) {
      batch.setColor(new Color(20 / 32f, 0, 0, 1))
      largeFont.setColor(Color.BLACK); //(sp.large ? largeFont : font).
      font.setColor(Color.BLACK); //(sp.large ? largeFont : font).
      largeFont.draw(batch, "PAUSED", camera.position.x + 12, camera.position.y + 50)
      font.draw(batch, "Press Space to continue", camera.position.x - 23 * 2, camera.position.y + 0)
    }
    for(sp <- Logic.speaking)
    {
      batch.setColor(Color.BLACK)
      (if(sp.large) largeFont else font).setColor(Color.BLACK)
      (if(sp.large) largeFont else font).draw(batch, sp.text, sp.worldX - (sp.text.length * (if(sp.large) 8 else 4) ), (if(sp.large) sp.worldY else sp.worldY - 32))
    }
    //            worldX = 20 + x * 64 + y * 64;
    //            worldY = 8 + x * 32 - y * 32;

    batch.end();

    UI.draw();
    //shader.end();
  }

    def show() =
    {
      resumeGame()
    }

    def hide() =
    {
      pauseGame()
    }

    override def pause() =
    {
      pauseGame();

    }

    override def resume() =
    {
      resumeGame()
    }

    def resumeGame () {
      if (Logic.state == GameState.Paused) {
        Timer.instance.resume()
        Logic.state = Logic.previousState
      }
    }
    def pauseGame () {

      if (Logic.state != GameState.Paused) {
        Timer.instance.pause()
        Logic.previousState = Logic.state;
        Logic.state = GameState.Paused;
      }
    }
    def dispose () {
    try {
      Logic.dispose()
      Timer.instance.stop();
    }
    catch{
      case e:Exception =>
    }
    batch.dispose()
    atlas.dispose()
      println("Wrapping up!")
  }
    def resize (wide:Int, high:Int) {
    Effects.CenterCamera(new Position(Logic.ActivePiece.x, Logic.ActivePiece.y), 1)
    UI.stage.getViewport().update(wide, high, true)
    camera.setToOrtho(false, wide, high)
    camera.update()
  }

    def createChannelShader () : ShaderProgram = {
      val vertex = "attribute vec4 a_position;    \n" +
        "attribute vec4 a_color;\n" +
        "attribute vec2 a_texCoord0;\n" +
        "uniform mat4 u_projTrans;\n" +
        "varying vec4 v_color;" +
        "varying vec2 v_texCoords;" +
        "void main()                  \n" +
        "{                            \n" +
        "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" +
        "   v_color.a = v_color.a * (256.0/255.0);\n" +
        "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0; \n" +
        "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" +
        "}                            \n";
      val fragment =
"""
#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif
varying LOWP vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D u_texPalette;

void main()
{
        vec4 color = texture2D(u_texture, v_texCoords);
        vec2 index = vec2(color.r, v_color.r);
        gl_FragColor = vec4(texture2D(u_texPalette, index).rgb, color.a);
}
"""
      val shader = new ShaderProgram(vertex, fragment);
      if (shader.isCompiled() == false) throw new GdxRuntimeException("Error compiling shader: " + shader.getLog());
      return shader;
    }
  }

  class InputProc extends InputProcessor
  {
    def keyDown(keycode: Int): Boolean =
    {
      if(CommandersUnite.initialized == false)
        return false
      if (keycode == Input.Keys.SPACE) {
        if (Logic.state != GameState.Paused) {
          CommandersUnite.game.getScreen.pause();
        }
        else {
          CommandersUnite.game.getScreen.resume();
        }
      }
      false
    }

    def keyUp(keycode: Int): Boolean =
    {
      false
    }

    def keyTyped(character: Char): Boolean =
    {
      false
    }

    def touchDown(x: Int, y: Int, pointer: Int, button: Int): Boolean =
    {
      if(CommandersUnite.initialized == false)
        return false
      if (Logic.state == GameState.PC_Select_Move || Logic.state == GameState.PC_Select_Action) {
        var v3 = CommandersUnite.game.camera.unproject(new Vector3(x, y, 0))

        var worldX = v3.x
        var worldY = v3.y

        //Console.WriteLine("screenX: " + screenX + ", screenY: " + screenY);
        worldY = worldY - 32;
        //screenX /= 64;
        //screenY /= 32;

        var gridX = (worldX / 128 + worldY / 64).toInt
        var gridY = (worldX / 128 - worldY / 64).toInt

        CommandersUnite.cursor = Position(gridX, gridY)
        if (Logic.PieceGrid(CommandersUnite.cursor.x)(CommandersUnite.cursor.y) == null && Logic.state == GameState.PC_Select_Move) {
          Logic.state = GameState.PC_Play_Move;
        }
        else if (Logic.ActivePiece.x == CommandersUnite.cursor.x && Logic.ActivePiece.y == CommandersUnite.cursor.y && Logic.state == GameState.PC_Select_Action) {
          Logic.advanceTurn();
          Logic.state = GameState.NPC_Play;
        }
        else if (Logic.PieceGrid(CommandersUnite.cursor.x)(CommandersUnite.cursor.y) != null
          && Logic.FieldMap.Highlight(CommandersUnite.cursor.x)(CommandersUnite.cursor.y) == HighlightType.Bright
          && Logic.state == GameState.PC_Select_Action) {
          Logic.speaking.clear();
          Logic.state = GameState.PC_Play_Action;
        }
      }
      return false;
    }

    def touchUp(x: Int, y: Int, pointer: Int, button: Int): Boolean = {
      return false;
    }

  def touchDragged(x: Int, y: Int, pointer: Int): Boolean = {
      return false;
    }

    def mouseMoved(x: Int, y: Int) :Boolean =
    {
      if(CommandersUnite.initialized == false)
        return false
      if (Logic.state == GameState.PC_Select_Move) {
        var v3 = CommandersUnite.game.camera.unproject(new Vector3(x, y, 0));

        var worldX = v3.x;
        var worldY = v3.y;

        //Console.WriteLine("screenX: " + screenX + ", screenY: " + screenY);
        worldY = worldY - 32;
        //screenX /= 64;
        //screenY /= 32;

        var gridX = (worldX / 128 + worldY / 64).toInt
        var gridY = (worldX / 128 - worldY / 64).toInt

        CommandersUnite.cursor = Position(gridX, gridY)
      }
      false
    }

    def scrolled(amount:Int):Boolean = {
       false
    }
  }
}
object CommandersUnite
{
  var initialized = false
  var game:CommandersUnite = null
  val updateStep = 0.33F
  var stateTime, attackTime, explodeTime, receiveTime = 0.0f
  var cursor:Position = null;
}