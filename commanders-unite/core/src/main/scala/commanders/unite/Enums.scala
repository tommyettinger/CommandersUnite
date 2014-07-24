package commanders.unite {

/**
 * Created by Tommy Ettinger on 7/22/2014.
 */
object MovementType extends Enumeration {
  type MovementType = Value
  val Foot, Treads, TreadsAmphi, Wheels, WheelsTraverse, Flight, FlightFlyby, Immobile = Value
}

object Direction extends Enumeration {
  type Direction = Value
  val SE, SW, NW, NE = Value
}

object Mode extends Enumeration {
  type Mode = Value
  val Selecting, Moving, Attacking = Value
}

object VisualAction extends Enumeration {
  type VisualAction = Value
  val Normal, Exploding, Firing = Value
}

object PieceType extends Enumeration {
  type PieceType = Value
  val Personnel, Armored, Vehicle, Plane, Helicopter
  = Value
}
object WeaponType extends Enumeration {
  type WeaponType = Value
  val LightGun, HeavyGun, Cannon, Missile, Non = Value
}

}