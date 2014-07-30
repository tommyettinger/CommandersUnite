package commanders.unite {

/**
 * Created by Tommy Ettinger on 7/22/2014.
 */
object MovementType extends Enumeration
{
  type MovementType = Value
  val Foot, Treads, TreadsAmphi, Wheels, WheelsTraverse, Flight, FlightFlyby, Immobile = Value
}

object Direction extends Enumeration
{
  type Direction = Value
  val SE, SW, NW, NE = Value
}

object Mode extends Enumeration
{
  type Mode = Value
  val Selecting, Moving, Attacking = Value
}

object VisualAction extends Enumeration
{
  type VisualAction = Value
  val Normal, Exploding, Firing = Value
}

object PieceType extends Enumeration
{
  type PieceType = Value
  val Personnel, Armored, Vehicle, Plane, Helicopter
  = Value
}

object WeaponType extends Enumeration
{
  type WeaponType = Value
  val LightGun = Value("Light Gun")
  val HeavyGun = Value("Heavy Gun")
  val Cannon = Value("Cannon")
  val Missile = Value("Explosive")
  val Non = Value
}

object ActionType extends Enumeration
{
  type ActionType = Value
  val Capture = Value("Capture")
  val CapturePlus = Value("Capture+")
  val Hide = Value("Hide")
  val HidePlus = Value("Hide+")
  val Disrupt = Value("Disrupt")
  val DisruptPlus = Value("Disrupt+")
  val Transport = Value("Transport")
  val Build = Value("Build")
  val Non = Value
}

object GameState extends Enumeration
{
  type GameState = Value
  val Paused, NPC_Play, PC_Select_Move, PC_Play_Move, PC_Select_UI, PC_Select_Action, PC_Play_Action = Value
}

object HighlightType extends Enumeration
{
  type HighlightType = Value
  val Plain, Bright, Dim, Spectrum = Value
}

}