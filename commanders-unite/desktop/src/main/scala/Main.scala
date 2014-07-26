package game.commanders.unite

import com.badlogic.gdx.backends.lwjgl._

object Main extends App {
    val cfg = new LwjglApplicationConfiguration
    cfg.title = "Commanders Unite"
    cfg.height = 720
    cfg.width = 1280
    cfg.forceExit = false
    new LwjglApplication(new CommandersUnite, cfg)
}
