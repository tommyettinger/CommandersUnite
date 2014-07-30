package commanders.unite

import commanders.unite.ActionType.ActionType
import commanders.unite.Direction._
import commanders.unite.MovementType._
import commanders.unite.PieceType._
import commanders.unite.VisualAction._
import commanders.unite.WeaponType._

import scala.collection.mutable.{ArrayBuffer, HashMap}

/**
 * Created by Tommy Ettinger on 7/29/2014.
 */
case class Weapon(
                   kind: WeaponType,
                   damage: Int,
                   multipliers: Array[Float],
                   minRange: Int = 1,
                   maxRange: Int = 1,
                   moveAndAttack: Boolean = true,
                   ring: Boolean = false,
                   alert: Boolean = false,
                   seek: Boolean = false
                   )
{}

object Weapon
{
  def apply(kind: WeaponType, damage: Int, multipliers: Array[Float], specialQualities: String): Weapon =
  {
    var minRange: Int = 1
    var maxRange: Int = 1
    var moveAndAttack: Boolean = true
    var ring: Boolean = false
    var alert: Boolean = false
    var seek: Boolean = false
    if (specialQualities != "") {
      specialQualities.split(" ", 1)(0) match {
        case "Alert" => alert = true
        case "Seek" => seek = true
        case "Ring" => ring = true
        case "Indirect" =>
          moveAndAttack = false
          minRange = Integer.parseInt(specialQualities.split(" ", 2)(1).substring(0, 1))
          maxRange = Integer.parseInt(specialQualities.split(" ", 2)(1).substring(2, 3))
      }
    }
    new Weapon(kind, damage, multipliers, minRange, maxRange, moveAndAttack, ring, alert, seek)
  }

  def apply(kind: WeaponType, damage: Int, multipliers: Array[Float], specialQualities: String, minimumRange: Int, maximumRange: Int): Weapon =
  {
    var minRange: Int = 1
    var maxRange: Int = 1
    var moveAndAttack: Boolean = true
    var ring: Boolean = false
    var alert: Boolean = false
    var seek: Boolean = false
    if (specialQualities != "") {
      specialQualities.split(" ", 1)(0) match {
        case "Alert" => alert = true
        case "Seek" => seek = true
        case "Ring" => ring = true
        case "Indirect" =>
          moveAndAttack = false
      }
    }
    minRange = minimumRange
    maxRange = maximumRange
    Weapon(kind, damage, multipliers, minRange, maxRange, moveAndAttack, ring, alert, seek)
  }
}

object Piece
{
  val CurrentPieces = List(
    "Infantry", "Infantry_P", "Infantry_S", "Infantry_T",
    "Artillery", "Artillery_P", "Artillery_S", "Artillery_T",
    "Tank", "Tank_P", "Tank_S", "Tank_T",
    "Plane", "Plane_P", "Plane_S", "Plane_T",
    "Supply", "Supply_P", "Supply_S", "Supply_T",
    "Copter", "Copter_P", "Copter_S", "Copter_T",
    "City", "Factory", "Airport", "Laboratory", "Castle", "Estate"
  )
  val PieceNames = List(
    "Infantry", "Bazooka", "Bike", "Sniper",
    "Light Artillery", "Defensive Artillery", "AA Artillery", "Stealth Artillery",
    "Light Tank", "Heavy Tank", "AA Tank", "Recon Tank",
    "Prop Plane", "Ground Bomber", "Fighter Jet", "Stealth Bomber",
    "Supply Truck", "Rig", "Amphi Transport", "Jammer",
    "Transport Copter", "Gunship Copter", "Blitz Copter", "Comm Copter",
    "City", "Factory", "Airport", "Laboratory", "Castle", "Estate"
  )
  val PieceTypes = List
  ("Personnel", "Armored", "Vehicle", "Plane", "Helicopter")

  val WeaponDisplays = List(
    (1, -1), (0, 5), (1, -1), (0, 0),
    (-1, 4), (3, -1), (-1, 6), (-1, 6),
    (3, 1), (3, 1), (1, -1), (1, 3),
    (1, -1), (-1, 7), (5, -1), (5, -1),
    (-1, -1), (-1, -1), (-1, -1), (-1, -1),
    (-1, -1), (1, 5), (1, -1), (-1, -1),
    (-1, -1), (-1, -1), (-1, -1), (-1, -1), (-1, -1), (-1, -1)
  )
  val Weapons = List(
    (Weapon(WeaponType.LightGun, 10, Array(1f, 0.5f, 0.5f, 0f, 0.5f), ""),
      Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), "")),
    (Weapon(WeaponType.LightGun, 10, Array(1f, 0.5f, 0.5f, 0f, 0.5f), ""),
      Weapon(WeaponType.Missile, 13, Array(0.5f, 2f, 2f, 0f, 0.5f), "")),
    (Weapon(WeaponType.LightGun, 10, Array(1f, 0.5f, 0.5f, 0f, 0.5f), "Alert"),
      Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), "")),
    (Weapon(WeaponType.LightGun, 10, Array(1f, 0.5f, 0.5f, 0f, 0.5f), ""),
      Weapon(WeaponType.HeavyGun, 13, Array(2f, 0.5f, 1f, 0f, 0.5f), "Indirect", 1, 2)),

    (Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), ""),
      Weapon(WeaponType.Cannon, 13, Array(1f, 2f, 2f, 0f, 0f), "Indirect", 2, 4)),
    (Weapon(WeaponType.Cannon, 16, Array(1f, 2f, 2f, 0f, 0f), "Indirect", 1, 3),
      Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), "")),
    (Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), ""),
      Weapon(WeaponType.Missile, 16, Array(0f, 0.5f, 0.5f, 2f, 2f), "Indirect", 2, 6)),
    (Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), ""),
      Weapon(WeaponType.Missile, 19, Array(1f, 2f, 2f, 0f, 0f), "Indirect", 3, 6)),

    (Weapon(WeaponType.Cannon, 16, Array(0.5f, 2f, 2f, 0f, 0f), ""),
      Weapon(WeaponType.LightGun, 13, Array(1.5f, 1f, 1f, 0f, 1f), "")),
    (Weapon(WeaponType.Cannon, 19, Array(0.5f, 2f, 2f, 0f, 0f), ""),
      Weapon(WeaponType.HeavyGun, 16, Array(0.5f, 1f, 1f, 0f, 1.5f), "")),
    (Weapon(WeaponType.HeavyGun, 16, Array(2f, 0.5f, 1f, 2f, 2f), ""),
      Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), "")),
    (Weapon(WeaponType.LightGun, 13, Array(2f, 1f, 1f, 0f, 1f), "Seek"),
      Weapon(WeaponType.Cannon, 16, Array(0.5f, 2f, 2f, 0f, 0f), "")),

    (Weapon(WeaponType.LightGun, 16, Array(1.5f, 0.5f, 1f, 0.5f, 1.5f), "Alert"),
      Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), "")),
    (Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), ""),
      Weapon(WeaponType.Missile, 25, Array(1f, 2f, 2f, 0f, 0f), "Ring")),
    (Weapon(WeaponType.Missile, 25, Array(0f, 0f, 0f, 2.5f, 2.5f), ""),
      Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), "")),
    (Weapon(WeaponType.Missile, 16, Array(0.5f, 1.5f, 1.5f, 0.5f, 1f), "Seek"),
      Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), "")),

    (Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), ""),
      Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), "")),
    (Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), ""),
      Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), "")),
    (Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), ""),
      Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), "")),
    (Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), ""),
      Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), "")),

    (Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), ""),
      Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), "")),
    (Weapon(WeaponType.HeavyGun, 16, Array(1f, 1f, 1.5f, 0f, 1f), ""),
      Weapon(WeaponType.Missile, 16, Array(0f, 1.5f, 1.5f, 0f, 0.5f), "")),
    (Weapon(WeaponType.LightGun, 13, Array(1.5f, 1f, 1f, 0f, 1.5f), ""),
      Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), "")),
    (Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), ""),
      Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), "")),

    (Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), ""),
      Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), "")),
    (Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), ""),
      Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), "")),
    (Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), ""),
      Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), "")),
    (Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), ""),
      Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), "")),
    (Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), ""),
      Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), "")),
    (Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), ""),
      Weapon(WeaponType.Non, 0, Array(0f, 0f, 0f, 0f, 0f), ""))
  )
  val Actions = List(
    List(ActionType.Capture),    List(ActionType.Capture),
    List(ActionType.Capture),    List(ActionType.Hide, ActionType.CapturePlus),
    List(),    List(),
    List(),    List(ActionType.Hide),
    List(),    List(),
    List(),    List(ActionType.Hide),
    List(),    List(),
    List(),    List(ActionType.HidePlus),
    List(ActionType.Transport),    List(ActionType.Build, ActionType.Transport),
    List(ActionType.Transport),    List(ActionType.DisruptPlus),
    List(ActionType.Transport),    List(),
    List(),    List(ActionType.HidePlus, ActionType.Disrupt),
    List(),    List(),    List(),    List(),    List(),    List()

  )
  var PieceLookup = new HashMap[String, Int]
  var TerrainLookup = new HashMap[String, Int]
  var NameLookup = new HashMap[String, Int]
  var MobilityToPieces = new HashMap[MovementType, ArrayBuffer[Int]]
  var MobilityToTerrains = new HashMap[MovementType, ArrayBuffer[Int]]
  var TerrainToPieces = new Array[ArrayBuffer[Int]](11)
  var TerrainToMobilities = new HashMap[Int, ArrayBuffer[MovementType]]

  val AllSpeeds = Array(
    3, 3, 5, 3,
    4, 3, 6, 4,
    6, 4, 7, 6,
    7, 5, 9, 8,
    5, 5, 6, 6,
    7, 5, 8, 7,
    0, 0, 0, 0, 0, 0
  )
  val AllArmors = Array(
    1, 2, 1, 0,
    2, 3, 2, 1,
    2, 3, 2, 1,
    0, 2, 0, 0,
    2, 4, 3, 2,
    1, 2, 0, 1,
    3, 3, 2, 2, 4, 4
  )
  val AllDodges = Array(
    2, 2, 3, 4,
    2, 1, 3, 3,
    0, 0, 1, 2,
    3, 2, 3, 4,
    1, 0, 1, 3,
    2, 1, 3, 4,
    0, 0, 0, 0, 0, 0
  )
  val AllHealths = Array(
    20, 25, 20, 20,
    25, 40, 25, 25,
    30, 50, 35, 30,
    30, 45, 35, 30,
    25, 40, 35, 30,
    25, 30, 25, 25,
    70, 80, 70, 70, 90, 90
  )
  val AllMobilities = Array(
    MovementType.Foot, MovementType.Foot, MovementType.WheelsTraverse, MovementType.Foot,
    MovementType.Treads, MovementType.Treads, MovementType.Treads, MovementType.WheelsTraverse,
    MovementType.Treads, MovementType.Treads, MovementType.Treads, MovementType.TreadsAmphi,
    MovementType.Flight, MovementType.Flight, MovementType.Flight, MovementType.FlightFlyby,
    MovementType.Wheels, MovementType.Treads, MovementType.TreadsAmphi, MovementType.Wheels,
    MovementType.Flight, MovementType.Flight, MovementType.Flight, MovementType.FlightFlyby,
    MovementType.Immobile, MovementType.Immobile, MovementType.Immobile, MovementType.Immobile, MovementType.Immobile, MovementType.Immobile
  )
  val AllPieceTypes = Array(
    PieceType.Personnel, PieceType.Personnel, PieceType.Personnel, PieceType.Personnel,
    PieceType.Vehicle, PieceType.Armored, PieceType.Vehicle, PieceType.Vehicle,
    PieceType.Armored, PieceType.Armored, PieceType.Armored, PieceType.Armored,
    PieceType.Plane, PieceType.Plane, PieceType.Plane, PieceType.Plane,
    PieceType.Vehicle, PieceType.Vehicle, PieceType.Vehicle, PieceType.Vehicle,
    PieceType.Helicopter, PieceType.Helicopter, PieceType.Helicopter, PieceType.Helicopter,
    PieceType.Armored, PieceType.Armored, PieceType.Armored, PieceType.Armored, PieceType.Armored, PieceType.Armored
  )

  def PieceTypeAsNumber(ut: PieceType): Int =
  {
    ut match {
      case PieceType.Personnel => return 0
      case PieceType.Armored => return 1
      case PieceType.Vehicle => return 2
      case PieceType.Plane => return 3
      case PieceType.Helicopter => return 4
    }
    return 1
  }
  {

    for (v <- MovementType.values) {
      MobilityToPieces += ((v, new ArrayBuffer[Int]()))
    }
    for (t <- 0 until LocalMap.Terrains.length) {
      TerrainLookup += ((LocalMap.Terrains(t), t))
      TerrainToMobilities += ((t, new ArrayBuffer[MovementType]()))
      TerrainToPieces(t) = new ArrayBuffer[Int]()
    }
    for (i <- 0 until CurrentPieces.length) {
      PieceLookup += ((CurrentPieces(i), i))
      NameLookup += ((PieceNames(i), i))
      MobilityToPieces(AllMobilities(i)) += i;
    }
    MobilityToTerrains += ((MovementType.Flight,
      ArrayBuffer(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)))
    MobilityToTerrains += ((MovementType.FlightFlyby,
      ArrayBuffer(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)))
    MobilityToTerrains += ((MovementType.Foot,
      ArrayBuffer(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)))
    MobilityToTerrains += ((MovementType.Treads,
      ArrayBuffer(0, 1, 2, 3, 6, 7, 8)))
    MobilityToTerrains += ((MovementType.TreadsAmphi,
      ArrayBuffer(0, 1, 2, 3, 6, 7, 8, 9)))
    MobilityToTerrains += ((MovementType.Wheels,
      ArrayBuffer(0, 2, 7, 8)))
    MobilityToTerrains += ((MovementType.WheelsTraverse,
      ArrayBuffer(0, 1, 2, 3, 6, 7, 8)))
    MobilityToTerrains += ((MovementType.Immobile,
      ArrayBuffer[Int]()))
    for (kv <- MobilityToTerrains) {
      for (t <- kv._2) {
        TerrainToMobilities(t) += kv._1
      }
    }
    for (kv <- TerrainToMobilities) {
      for (m <- kv._2)
        TerrainToPieces(kv._1) ++= MobilityToPieces(m)
      TerrainToPieces(kv._1) = TerrainToPieces(kv._1).distinct
    }
  }

  def reset(p: Piece): Piece =
  {
    Piece(p.unitIndex, p.color, p.facingNumber, p.x, p.y)(worldX = 20 + p.x * 64 + p.y * 64, worldY = 6 + p.x * 32 - p.y * 32)
  }
}

case class Piece(unitIndex: Int,
                 color: Int,
                 var facingNumber: Int,
                 var x: Int,
                 var y: Int)
                (
                  var facing: Direction = Logic.directions(facingNumber),
                  var name: String = Piece.CurrentPieces(unitIndex),
                  val speed: Int = Piece.AllSpeeds(unitIndex),
                  val mobility: MovementType = Piece.AllMobilities(unitIndex),
                  val kind: PieceType = Piece.AllPieceTypes(unitIndex),
                  val maxHealth: Int = Piece.AllHealths(unitIndex),
                  var currentHealth: Int = Piece.AllHealths(unitIndex),
                  val armor: Int = Piece.AllArmors(unitIndex),
                  val dodge: Int = Piece.AllDodges(unitIndex),
                  val weaponry: (Weapon, Weapon) = Piece.Weapons(unitIndex),
                  val actions: List[ActionType] = Piece.Actions(unitIndex),
                  var visual: VisualAction = VisualAction.Normal,
                  var targeting: ArrayBuffer[DirectedPosition] = new ArrayBuffer[DirectedPosition],
                  var worldX: Float = 20 + x * 64 + y * 64,
                  var worldY: Float = 6 + x * 32 - y * 32)
{
  facingNumber match {
    case 0 => facing = Direction.SE
      facingNumber = 0
    case 1 => facing = Direction.SW
      facingNumber = 1
    case 2 => facing = Direction.NW
      facingNumber = 2
    case 3 => facing = Direction.NE
      facingNumber = 3
    case _ => facing = Direction.SE
      facingNumber = 0
  }

  def isOpposed(u: Piece): Boolean =
  {
    if (color == 0) {
      return u.color != 0
    }
    else {
      return u.color == 0
    }
  }

  def attemptDodge(attacker: Weapon): Boolean =
  {
    if (Logic.r.nextInt(10) + 1 > dodge && attacker.multipliers(Piece.PieceTypeAsNumber(kind)) > 0) {
      return true
    }
    false
  }

  def takeDamage(attacker: Weapon): Boolean = takeDamage(attacker.damage + Logic.r.nextInt(attacker.damage) / 2f - attacker.damage / 4f, attacker.multipliers(Piece.PieceTypeAsNumber(kind)))

  def takeDamage(amount: Float, multiplier: Float): Boolean =
  {
    currentHealth = currentHealth - Math.round(amount * (multiplier - 0.1f * armor))
    if (currentHealth <= 0) {
      currentHealth = 0;
      return true;
    }
    return false;
  }
}
