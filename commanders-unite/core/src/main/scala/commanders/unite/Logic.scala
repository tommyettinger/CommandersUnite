/**
 * Created by Tommy Ettinger on 7/22/2014.
 */
package commanders.unite {

import commanders.unite.Direction.Direction
import commanders.unite.MovementType.MovementType
import commanders.unite.PieceType.PieceType
import commanders.unite.VisualAction.VisualAction
import commanders.unite.WeaponType.WeaponType

import collection.mutable._
import scala.collection.mutable
import scala.util.Random

/*
LINQ conversion table

xs.Aggregate(accumFunc) -> xs.reduceLeft(accumFunc)
xs.Aggregate(seed, accumFunc) -> xs.foldLeft(seed)(accumFunc)
xs.Aggregate(seed, accumFunc, trans) -> trans(xs.foldLeft(seed)(accumFunc))
xs.All(pred) -> xs.forall(pred)
xs.Any() -> xs.nonEmpty
xs.Any(pred) -> xs.exists(pred)
xs.AsEnumerable() -> xs.asTraversable // roughly
xs.Average() -> xs.sum / xs.length
xs.Average(trans) -> trans(xs.sum / xs.length)
xs.Cast<A>() -> xs.map(_.asInstanceOf[A])
xs.Concat(ys) -> xs ++ ys
xs.Contains(x) -> xs.contains(x) //////
xs.Contains(x, eq) -> xs.exists(eq(x, _))
xs.Count() -> xs.size
xs.Count(pred) -> xs.count(pred)
xs.DefaultIfEmpty() -> if(xs.isEmpty) List(0) else xs // Use `mzero` (from Scalaz) instead of 0 for more genericity
xs.DefaultIfEmpty(v) -> if(xs.isEmpty) List(v) else xs
xs.Distinct() -> xs.distinct
xs.ElementAt(i) -> xs(i)
xs.ElementAtOrDefault(i) -> xs.lift(i).orZero // `orZero` is from Scalaz
xs.Except(ys) -> xs.diff(ys)
xs.First() -> xs.head
xs.First(pred) -> xs.find(pred) // returns an `Option`
xs.FirstOrDefault() -> xs.headOption.orZero
xs.FirstOrDefault(pred) -> xs.find(pred).orZero
xs.GroupBy(f) -> xs.groupBy(f)
xs.GroupBy(f, g) -> xs.groupBy(f).mapValues(_.map(g))
xs.Intersect(ys) -> xs.intersect(ys)
xs.Last() -> xs.last
xs.Last(pred) -> xs.reverseIterator.find(pred) // returns an `Option`
xs.LastOrDefault() -> xs.lastOption.orZero
xs.LastOrDefault(pred) -> xs.reverseIterator.find(pred).orZero
xs.Max() -> xs.max
xs.Max(f) -> xs.maxBy(f)
xs.Min() -> xs.min
xs.Min(f) -> xs.minBy(f)
xs.OfType<A>() -> xs.collect { case x: A => x }
xs.OrderBy(f) -> xs.sortBy(f)
xs.OrderBy(f, comp) -> xs.sortBy(f)(comp) // `comp` is an `Ordering`.
xs.OrderByDescending(f) -> xs.sortBy(f)(implicitly[Ordering[A]].reverse)
xs.OrderByDescending(f, comp) -> xs.sortBy(f)(comp.reverse)
Enumerable.Range(start, count) -> start until start + count
Enumerable.Repeat(x, times) -> Iterator.continually(x).take(times)
xs.Reverse() -> xs.reverse
xs.Select(trans) -> xs.map(trans) // For indexed overload, first `zipWithIndex` and then `map`.
xs.SelectMany(trans) -> xs.flatMap(trans)
xs.SequenceEqual(ys) -> xs.sameElements(ys)
xs.Skip(n) -> xs.drop(n)
xs.SkipWhile(pred) -> xs.dropWhile(pred)
xs.Sum() -> xs.sum
xs.Sum(f) -> xs.map(f).sum // or `xs.foldMap(f)`. Requires Scalaz.
xs.Take(n) -> xs.take(n)
xs.TakeWhile(pred) -> xs.takeWhile(pred)
xs.OrderBy(f).ThenBy(g) -> xs.sortBy(x => (f(x), g(x))) // Or: xs.sortBy(f &&& g). `&&&` is from Scalaz.
xs.ToArray() -> xs.toArray // Use `xs.toIndexedSeq` for immutable indexed sequence.
xs.ToDictionary(f) -> xs.map(f.first).toMap // `first` is from Scalaz. When f = identity, you can just write `xs.toMap`.
xs.ToList() -> xs.toList // This returns an immutable list. Use `xs.toBuffer` if you want a mutable list.
xs.Union(ys) -> xs.union(ys)
xs.Where(pred) -> xs.filter(pred)
xs.Zip(ys, f) -> (xs, ys).zipped.map(f) // When f = identity, use `xs.zip(ys)`.
 */
  case class Weapon (
    kind:WeaponType,
    damage:Int,
    multipliers:Array[Float],
    minRange:Int = 1,
    maxRange:Int = 1,
    moveAndAttack:Boolean = true,
    ring:Boolean = false,
    alert:Boolean = false,
    seek:Boolean = false
  )
  {}
  object Weapon
  {
    def apply (kind:WeaponType,damage:Int,multipliers:Array[Float], specialQualities:String) :Weapon =
      {
        var minRange:Int = 1
        var maxRange:Int = 1
        var moveAndAttack:Boolean = true
        var ring:Boolean = false
        var alert:Boolean = false
        var seek:Boolean = false
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
      Weapon(kind, damage, multipliers, minRange, maxRange, moveAndAttack, ring, alert, seek)
    }
    def apply (kind:WeaponType,damage:Int,multipliers:Array[Float], specialQualities:String, minimumRange:Int, maximumRange:Int) :Weapon =
    {
      var minRange:Int = 1
      var maxRange:Int = 1
      var moveAndAttack:Boolean = true
      var ring:Boolean = false
      var alert:Boolean = false
      var seek:Boolean = false
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
  object Piece {
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
        Weapon(WeaponType.Missile, 13, Array(0f, 0.5f, 0.5f, 2f, 2f), "Indirect", 2, 6)),
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
    var PieceLookup = new HashMap[String, Int]
    var TerrainLookup = new HashMap[String, Int]
    var NameLookup = new HashMap[String, Int]
    var MobilityToPieces = new HashMap[MovementType, ArrayBuffer[Int]]
    var MobilityToTerrains = new HashMap[MovementType, ArrayBuffer[Int]]
    var TerrainToPieces = new Array[ArrayBuffer[Int]](10)
    var TerrainToMobilities = new HashMap[Int, ArrayBuffer[MovementType]];
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

    def PieceTypeAsNumber(ut: PieceType): Int = {
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
        MobilityToPieces(v) = new ArrayBuffer[Int](15)
      }
      for (t <- 0 to LocalMap.Terrains.length) {
        TerrainLookup(LocalMap.Terrains(t)) = t;
        TerrainToMobilities(t) = new ArrayBuffer[MovementType]();
        TerrainToPieces(t) = new ArrayBuffer[Int]();
      }
      for (i <- 0 to CurrentPieces.length) {
        PieceLookup(CurrentPieces(i)) = i;
        NameLookup(PieceNames(i)) = i;
        MobilityToPieces(AllMobilities(i)) += i;
      }
      MobilityToTerrains(MovementType.Flight) =
        ArrayBuffer(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
      MobilityToTerrains(MovementType.FlightFlyby) =
        ArrayBuffer(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
      MobilityToTerrains(MovementType.Foot) =
        ArrayBuffer(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
      MobilityToTerrains(MovementType.Treads) =
        ArrayBuffer(0, 1, 2, 3, 6, 7, 8)
      MobilityToTerrains(MovementType.TreadsAmphi) =
        ArrayBuffer(0, 1, 2, 3, 6, 7, 8, 9)
      MobilityToTerrains(MovementType.Wheels) =
        ArrayBuffer(0, 2, 7, 8)
      MobilityToTerrains(MovementType.WheelsTraverse) =
        ArrayBuffer(0, 1, 2, 3, 6, 7, 8)
      MobilityToTerrains(MovementType.Immobile) =
        ArrayBuffer[Int]()
      for (kv <- MobilityToTerrains) {
        for (t <- kv._2) {
          TerrainToMobilities(t) += (kv._1);
        }
      }
      for (kv <- TerrainToMobilities) {
        for (m <- kv._2)
          TerrainToPieces(kv._1) ++= (MobilityToPieces(m));
        TerrainToPieces(kv._1) = TerrainToPieces(kv._1).distinct
      }
    }
  }
    class Piece(unitIndex:Int,
               color:Int,
               var facingNumber:Int,
               x:Int,
               y:Int,

               var name:String = Piece.CurrentPieces(unitIndex),
               var facing:Direction = Direction.SE,
  val speed:Int = Piece.AllSpeeds(unitIndex),
  val mobility:MovementType = Piece.AllMobilities(unitIndex),
  val kind:PieceType = Piece.AllPieceTypes(unitIndex),
  val maxHealth:Int = Piece.AllHealths(unitIndex),
  var currentHealth:Int = maxHealth,
  val armor:Int = Piece.AllArmors(unitIndex),
  val dodge:Int = Piece.AllDodges(unitIndex),
  val weaponry:(Weapon, Weapon) = Piece.Weapons(unitIndex),
  var visual:VisualAction = VisualAction.Normal,
  var targeting:ArrayBuffer[DirectedPosition] = new ArrayBuffer[DirectedPosition],
  var worldX:Float = 20 + x * 64 + y * 6,
  var worldY:Float = 6 + x * 32 - y * 32
  ) {
      facingNumber match {
        case 0=>facing = Direction.SE
          facingNumber = 0
        case 1=>facing = Direction.SW
          facingNumber = 1
        case 2=>facing = Direction.NW
          facingNumber = 2
        case 3=>facing = Direction.NE
          facingNumber = 3
        case _=>facing = Direction.SE
          facingNumber = 0
      }
    
    def isOpposed(u:Piece) :Boolean = {
      if (color == 0)
        return u.color != 0
      else
        return u.color == 0
    }
    def attemptDodge(attacker : Weapon) : Boolean = {
      if (Logic.r.nextInt(10) + 1 > dodge && attacker.multipliers(Piece.PieceTypeAsNumber(kind)) > 0)
      {
        return true
      }
      false
    }
    def takeDamage(attacker : Weapon) : Boolean = takeDamage(attacker.damage + Logic.r.nextInt(attacker.damage) / 2f - attacker.damage / 4f, attacker.multipliers(Piece.PieceTypeAsNumber(kind)))
    def takeDamage(amount:Float, multiplier:Float) :Boolean = {
      currentHealth = currentHealth - Math.round(amount * (multiplier - 0.1f * armor))
      if (currentHealth <= 0) {
        currentHealth = 0;
        return true;
      }
      return false;
    }
  }
  object Logic {
    def log = new StringBuilder()
    def CurrentMode = Mode.Selecting
    def width = 24
    def height = 24
    def FieldMap : LocalMap(width, height)
    def PieceGrid = Array.ofDim[Int](width, height)
    def ActivePiece:Piece = null
    def Colors = new Array[Int](4)
    def ReverseColors = new Array[Int](8)
    def ActingFaction = 1
    def TaskSteps = 0
    def speaking = new ArrayBuffer[Speech](16)
    def targetX = Array(width/4, width/2, width/4, width/2)
    def targetY = Array(height / 2, height / 4, height / 2, height / 4)
    def outward = Array.ofDim[Float](width+2,height+2)
    def DirectionNames = Array("SE", "SW", "NW", "NE")

    //foot 0-0, treads 1-5, wheels 6-8, flight 9-10


    /*{ "Infantry", //foot 0 0
                           "Tank", "Artillery", "Artillery_P", "Artillery_S", "Supply_P", //treads 1 5
                           "Artillery_T", "Supply", "Supply_T", //wheels 6 8
                           "Helicopter", "Plane", //flight 9 10
                           "City", "Factory", "Castle", "Capital" //facility
                         };*/


    def r = new Random()

    def ConvertDirection (dir : Direction) : Int = {
      dir match {
        case Direction.SE=> return 0
        case Direction.SW=> return 1
        case Direction.NW=> return 2
        case Direction.NE=> return 3
        case _ => return 0
      }
    }
    float[, ] DijkstraAttackPositions(Weapon weapon, MovementType mobility, int selfColor, int[] targetColors, int[, ] grid, Piece[, ] placing, float[, ] d)
    {
      int width = d.GetLength(0);
      int height = d.GetLength(1);
      int wall = 2222;
      int goal = 0;

      Dictionary < Position, float > open = new Dictionary < Position, float >(),
      fringe = new Dictionary < Position, float >(),
      closed = new Dictionary < Position, float >();

      int[] ability =
        new int[] {
          0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        };
      //plains forest desert jungle hills mountains ruins tundra road river basement
      Dictionary < MovementType, bool > pass = new Dictionary < MovementType, bool > {
      {
        MovementType.Foot, true
      },
      {
        MovementType.Treads, true
      },
      {
        MovementType.Wheels, true
      },
      {
        MovementType.TreadsAmphi, true
      },
      {
        MovementType.WheelsTraverse, true
      },
      {
        MovementType.Flight, true
      },
      {
        MovementType.FlightFlyby, true
      },
      {
        MovementType.Immobile, false
      },
    };
      switch(mobility) {
        case MovementType.Foot:
        ability =
          new int[] {
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0
          };
        pass = new Dictionary < MovementType, bool > {
          {
            MovementType.Foot, false
          },
          {
            MovementType.Treads, true
          },
          {
            MovementType.Wheels, false
          },
          {
            MovementType.TreadsAmphi, true
          },
          {
            MovementType.WheelsTraverse, false
          },
          {
            MovementType.Flight, true
          },
          {
            MovementType.FlightFlyby, true
          },
          {
            MovementType.Immobile, false
          },
        };
        break;
        case MovementType.Treads:
          ability =
        new int[] {
          1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0
        };
        pass = new Dictionary < MovementType, bool > {
          {
            MovementType.Foot, false
          },
          {
            MovementType.Treads, false
          },
          {
            MovementType.Wheels, false
          },
          {
            MovementType.TreadsAmphi, false
          },
          {
            MovementType.WheelsTraverse, false
          },
          {
            MovementType.Flight, true
          },
          {
            MovementType.FlightFlyby, true
          },
          {
            MovementType.Immobile, false
          },
        };
        break;
        case MovementType.Wheels:
          ability =
        new int[] {
          1, 0, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0
        };
        pass = new Dictionary < MovementType, bool > {
          {
            MovementType.Foot, false
          },
          {
            MovementType.Treads, false
          },
          {
            MovementType.Wheels, false
          },
          {
            MovementType.TreadsAmphi, false
          },
          {
            MovementType.WheelsTraverse, false
          },
          {
            MovementType.Flight, true
          },
          {
            MovementType.FlightFlyby, true
          },
          {
            MovementType.Immobile, false
          },
        };
        break;
        case MovementType.TreadsAmphi:
          ability =
        new int[] {
          1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0
        };
        pass = new Dictionary < MovementType, bool > {
          {
            MovementType.Foot, false
          },
          {
            MovementType.Treads, false
          },
          {
            MovementType.Wheels, false
          },
          {
            MovementType.TreadsAmphi, false
          },
          {
            MovementType.WheelsTraverse, false
          },
          {
            MovementType.Flight, true
          },
          {
            MovementType.FlightFlyby, true
          },
          {
            MovementType.Immobile, false
          },
        };
        break;
        case MovementType.WheelsTraverse:
          ability =
        new int[] {
          1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0
        };
        pass = new Dictionary < MovementType, bool > {
          {
            MovementType.Foot, false
          },
          {
            MovementType.Treads, false
          },
          {
            MovementType.Wheels, false
          },
          {
            MovementType.TreadsAmphi, false
          },
          {
            MovementType.WheelsTraverse, false
          },
          {
            MovementType.Flight, true
          },
          {
            MovementType.FlightFlyby, true
          },
          {
            MovementType.Immobile, false
          },
        };
        break;
        case MovementType.Flight:
          ability =
        new int[] {
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0
        };
        pass = new Dictionary < MovementType, bool > {
          {
            MovementType.Foot, true
          },
          {
            MovementType.Treads, true
          },
          {
            MovementType.Wheels, true
          },
          {
            MovementType.TreadsAmphi, true
          },
          {
            MovementType.WheelsTraverse, true
          },
          {
            MovementType.Flight, false
          },
          {
            MovementType.FlightFlyby, false
          },
          {
            MovementType.Immobile, false
          },
        };
        break;
        case MovementType.FlightFlyby:
          ability =
        new int[] {
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0
        };
        pass = new Dictionary < MovementType, bool > {
          {
            MovementType.Foot, true
          },
          {
            MovementType.Treads, true
          },
          {
            MovementType.Wheels, true
          },
          {
            MovementType.TreadsAmphi, true
          },
          {
            MovementType.WheelsTraverse, true
          },
          {
            MovementType.Flight, true
          },
          {
            MovementType.FlightFlyby, true
          },
          {
            MovementType.Immobile, false
          },
        };
        break;
      }
      movesToTargets.Clear();
      for (int i = 1;
      i < width - 1;
      i ++)
      {
        for (int j = 1;
        j < height - 1;
        j ++)
        {
          if (targetColors.Any(c => placing[i - 1, j - 1] != null && c == placing[i - 1, j - 1].color) )
          {
            if (weapon.multipliers[Piece.PieceTypeAsNumber(placing[i - 1, j - 1].kind)] > 0)
            {
              Position tgt = new Position(i - 1, j - 1);
              foreach(Position p in Position.WithinRange(i, j, 1, 1, width - 1, height - 1, weapon.minRange, weapon.maxRange))
              {
                if (ability[grid[p.x - 1, p.y - 1]] == 1 && placing[p.x - 1, p.y - 1] == null)
                {
                  d[p.x, p.y] = goal;
                  open[p] = goal;
                  movesToTargets[ new Position(p.x - 1, p.y - 1)] = tgt;
                }
              }
            }
            /*
            if (weapon.minRange > 1)
            {
                //d[i, j] = wall;
                //closed[new Position(i, j)] = wall;
                int avoidance = weapon.minRange -1;
                for (int a = 1; a < avoidance; a++)
                {
                    foreach (Position p in Position.Nearby(i, j, width, height, a))
                    {
                        //d[p.x, p.y] = (d[p.x, p.y] == wall) ? wall : 2 * (avoidance - a);
                        //closed[p] = d[p.x, p.y];
                    }
                }
            }*/
          }
          else if (d[i, j] >= wall) {
          closed[ new Position(i, j)] = wall;
        }
        }
      }


      while (open.Count > 0) {
        foreach( var idx_dijk in open)
        {
          List < Position > moves = idx_dijk.Key.Adjacent(width, height);
          foreach(Position mov in moves)
          if (open.ContainsKey(mov) ||
            closed.ContainsKey(mov) ||
            d[mov.x, mov.y] >= wall ||
            d[mov.x, mov.y] <= idx_dijk.Value + 1) {

          }
          else if (
            ability[grid[mov.x - 1, mov.y - 1]] == 1
          && placing[mov.x - 1, mov.y - 1] == null)
          {
            fringe[mov] = (idx_dijk.Value + 1);
            d[mov.x, mov.y] = idx_dijk.Value + 1;
          }
          else if (
          ability[grid[mov.x - 1, mov.y - 1]] == 1 &&
          (placing[mov.x - 1, mov.y - 1] != null &&
          (pass[placing[mov.x - 1, mov.y - 1].mobility] ||
          (placing[mov.x - 1, mov.y - 1].color == selfColor &&
          placing[mov.x - 1, mov.y - 1].mobility != MovementType.Immobile)
          ) ) )
          {
            fringe[mov] = (idx_dijk.Value + 1);
            d[mov.x, mov.y] = idx_dijk.Value + 1;
          }
        }
        foreach( var kv in open)
        {
          closed[kv.Key] = (kv.Value);
        }
        open.Clear();
        foreach( var kv in fringe)
        {
          open[kv.Key] = (kv.Value);
        }
        fringe.Clear();

      }
      /*
      for (int i = 1; i < width - 1; i++)
      {
          for (int j = 1; j < height - 1; j++)
          {
              if (d[i, j] == goal && placing[i - 1, j - 1] != null)
              {
                  d[i, j] = 3333;// ((pass[placing[i - 1, j - 1].mobility]) ? 0 : wall);

              }
              else if (placing[i - 1, j - 1] != null)
              {
                  //d[i, j] += 0.5F;
              }
          }
      }*/
      return d;
    }
    public static void writeShowLog (string text) {
      #if DEBUG
      //log.Append (text + "\n");
      //Console.WriteLine(text);
      #endif
    }
    List < Position > bestMoves = new List < Position >();
    Dictionary < Position, Position > movesToTargets = new Dictionary < Position, Position >();
    Position best = null;
    float[, ] ViableMoves(Piece self, int currentWeapon, int[, ] grid, Piece[, ] placing)
    {
      writeShowLog("\n" + "* * " + self.name + " * *");
      writeShowLog("Piece is at: " + self.x + ", " + self.y);
      gradient = SmartDijkstra(self, currentWeapon, grid, placing, ((self.color == 0) ? new int[] {
        1, 2, 3, 4, 5, 6, 7
      }: new int[] {
      0
    }) );
      /*for (int i = 0; i < 25; i++)
      {
          StringBuilder lg = new StringBuilder();
          for (int j = 0; j < 25; j++)
          {
              if (i == self.x && j == self.y)
                  lg.Append("@@@");
              else
                  lg.Append(" " + ((placing[i, j] == null) ? ".." : placing[i, j].name.Substring(0, 2)));
          }
          Gdx.app.log("CU", lg.ToString());
      }*/
      int width = gradient.GetLength(0);
      int height = gradient.GetLength(1);
      int wall = 2222;
      int goal = 0;
      float unexplored = 1111;

      bestMoves = new List < Position > {
        new Position(self.x, self.y)
      };
      Dictionary < Position, float >
      open = new Dictionary < Position, float > {
      {
        new Position(self.x + 1, self.y + 1), goal
      }
    },
      fringe = new Dictionary < Position, float >(),
      closed = new Dictionary < Position, float >();


      float[, ] radiate = new float[width, height];

      for (int i = 1;
      i < width - 1;
      i ++)
      {
        for (int j = 1;
        j < height - 1;
        j ++)
        radiate[i, j] = unexplored;
      }

      for (int i = 0;
      i < width;
      i ++)
      {
        radiate[i, 0] = wall;
        radiate[i, (height - 1)] = wall;

      }
      for (int j = 1;
      j < height - 1;
      j ++)
      {
        radiate[ 0, j] = wall;
        radiate[(width - 1), j] = wall;
      }

      for (int i = 0;
      i < width;
      i ++)
      {
        for (int j = 0;
        j < height;
        j ++)
        {
          if (radiate[i, j] >= wall) // || gradient[i,j] >= wall) {
            closed[ new Position(i, j)] = wall;
          }
        }
      }
      radiate[self.x + 1, self.y + 1] = goal;
      int[] ability =
        new int[] {
          0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        };
      //plains forest desert jungle hills mountains ruins tundra road river basement
      Dictionary < MovementType, bool > pass = new Dictionary < MovementType, bool > {
      {
        MovementType.Foot, true
      },
      {
        MovementType.Treads, true
      },
      {
        MovementType.Wheels, true
      },
      {
        MovementType.TreadsAmphi, true
      },
      {
        MovementType.WheelsTraverse, true
      },
      {
        MovementType.Flight, true
      },
      {
        MovementType.FlightFlyby, true
      },
      {
        MovementType.Immobile, false
      },
    };
      switch(self.mobility) {
        case MovementType.Foot:
        ability =
          new int[] {
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0
          };
        pass = new Dictionary < MovementType, bool > {
          {
            MovementType.Foot, false
          },
          {
            MovementType.Treads, true
          },
          {
            MovementType.Wheels, false
          },
          {
            MovementType.TreadsAmphi, true
          },
          {
            MovementType.WheelsTraverse, false
          },
          {
            MovementType.Flight, true
          },
          {
            MovementType.FlightFlyby, true
          },
          {
            MovementType.Immobile, false
          },
        };
        break;
        case MovementType.Treads:
          ability =
        new int[] {
          1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0
        };
        pass = new Dictionary < MovementType, bool > {
          {
            MovementType.Foot, false
          },
          {
            MovementType.Treads, false
          },
          {
            MovementType.Wheels, false
          },
          {
            MovementType.TreadsAmphi, false
          },
          {
            MovementType.WheelsTraverse, false
          },
          {
            MovementType.Flight, true
          },
          {
            MovementType.FlightFlyby, true
          },
          {
            MovementType.Immobile, false
          },
        };
        break;
        case MovementType.Wheels:
          ability =
        new int[] {
          1, 0, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0
        };
        pass = new Dictionary < MovementType, bool > {
          {
            MovementType.Foot, false
          },
          {
            MovementType.Treads, false
          },
          {
            MovementType.Wheels, false
          },
          {
            MovementType.TreadsAmphi, false
          },
          {
            MovementType.WheelsTraverse, false
          },
          {
            MovementType.Flight, true
          },
          {
            MovementType.FlightFlyby, true
          },
          {
            MovementType.Immobile, false
          },
        };
        break;
        case MovementType.TreadsAmphi:
          ability =
        new int[] {
          1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0
        };
        pass = new Dictionary < MovementType, bool > {
          {
            MovementType.Foot, false
          },
          {
            MovementType.Treads, false
          },
          {
            MovementType.Wheels, false
          },
          {
            MovementType.TreadsAmphi, false
          },
          {
            MovementType.WheelsTraverse, false
          },
          {
            MovementType.Flight, true
          },
          {
            MovementType.FlightFlyby, true
          },
          {
            MovementType.Immobile, false
          },
        };
        break;
        case MovementType.WheelsTraverse:
          ability =
        new int[] {
          1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0
        };
        pass = new Dictionary < MovementType, bool > {
          {
            MovementType.Foot, false
          },
          {
            MovementType.Treads, false
          },
          {
            MovementType.Wheels, false
          },
          {
            MovementType.TreadsAmphi, false
          },
          {
            MovementType.WheelsTraverse, false
          },
          {
            MovementType.Flight, true
          },
          {
            MovementType.FlightFlyby, true
          },
          {
            MovementType.Immobile, false
          },
        };
        break;
        case MovementType.Flight:
          ability =
        new int[] {
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0
        };
        pass = new Dictionary < MovementType, bool > {
          {
            MovementType.Foot, true
          },
          {
            MovementType.Treads, true
          },
          {
            MovementType.Wheels, true
          },
          {
            MovementType.TreadsAmphi, true
          },
          {
            MovementType.WheelsTraverse, true
          },
          {
            MovementType.Flight, false
          },
          {
            MovementType.FlightFlyby, false
          },
          {
            MovementType.Immobile, false
          },
        };
        break;
        case MovementType.FlightFlyby:
          ability =
        new int[] {
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0
        };
        pass = new Dictionary < MovementType, bool > {
          {
            MovementType.Foot, true
          },
          {
            MovementType.Treads, true
          },
          {
            MovementType.Wheels, true
          },
          {
            MovementType.TreadsAmphi, true
          },
          {
            MovementType.WheelsTraverse, true
          },
          {
            MovementType.Flight, true
          },
          {
            MovementType.FlightFlyby, true
          },
          {
            MovementType.Immobile, false
          },
        };
        break;
      }
      float furthest = 0;
      float lowest = 1000;
      if (gradient[self.x + 1, self.y + 1] <= goal)
      {
        radiate.Fill(wall);
        radiate[self.x + 1, self.y + 1] = goal;
      }
      else
      {
        while (open.Count > 0 && furthest < self.speed) {
          foreach( var idx_dijk in open)
          {
            List < Position > moves = idx_dijk.Key.Adjacent(width, height);
            foreach(Position mov in moves)
            if (open.ContainsKey(mov) ||
              closed.ContainsKey(mov) ||
              radiate[mov.x, mov.y] >= wall ||
              radiate[mov.x, mov.y] <= idx_dijk.Value + 1) {

            }
            else if (
              ability[grid[mov.x - 1, mov.y - 1]] == 1 &&
            placing[mov.x - 1, mov.y - 1] == null)
            {
              fringe[mov] = (idx_dijk.Value + 1);
              radiate[mov.x, mov.y] = idx_dijk.Value + 1;
              if (gradient[mov.x, mov.y] < lowest) {
                //bestMoves.Clear();
                bestMoves.Add(new Position {
                  x = mov.x - 1, y = mov.y - 1
                });
                lowest = gradient[mov.x, mov.y];
              }
              else if (gradient[mov.x, mov.y] == lowest) {
                bestMoves.Add(new Position {
                  x = mov.x - 1, y = mov.y - 1
                });
              }
            }
            else if (
            ability[grid[mov.x - 1, mov.y - 1]] == 1 &&
            (placing[mov.x - 1, mov.y - 1] != null &&
            (//Math.Abs(self.x - (mov.x - 1)) + Math.Abs(self.y - (mov.y - 1)) < self.speed &&
              (pass[placing[mov.x - 1, mov.y - 1].mobility] ||
            (placing[mov.x - 1, mov.y - 1].color == self.color &&
            placing[mov.x - 1, mov.y - 1].mobility != MovementType.Immobile)
            ) ) ) )
            {
              radiate[mov.x, mov.y] = idx_dijk.Value + 1;
              fringe[mov] = (idx_dijk.Value + 1);
              //furthest = Math.Max(idx_dijk.Value + 1, furthest);
            }
          }
          foreach( var kv in open)
          {
            closed[kv.Key] = (kv.Value);
          }
          open.Clear();
          foreach( var kv in fringe)
          {
            open[kv.Key] = (kv.Value);
          }
          fringe.Clear();
          furthest ++;
        }
      }
      /*for (int i = 0; i < width; i++)
      {
          for (int j = 0; j < height; j++)
          {
              if (gradient[i, j] <= goal)
                  radiate[i, j] = wall;
          }
      }*/

      return radiate;
    }

    float[, ] dijkstraInner(Piece self, int[, ] grid, Piece[, ] placing, float[, ] d)
    {
      int width = d.GetLength(0);
      int height = d.GetLength(1);
      int wall = 2222;
      int goal = 0;

      Dictionary < Position, float > open = new Dictionary < Position, float >(),
      fringe = new Dictionary < Position, float >(),
      closed = new Dictionary < Position, float >();

      for (int i = 0;
      i < width;
      i ++)
      {
        for (int j = 0;
        j < height;
        j ++)
        {
          if (d[i, j] == goal) {
            open[ new Position(i, j)] = goal;
          }
          /*else if (d[i, j] == ultimate)
          {
              open[new Position(i, j)] = goal;
          }*/
          else if (d[i, j] >= wall) {
            closed[ new Position(i, j)] = wall;
          }
        }
      }

      int[] ability =
        new int[] {
          0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        };
      //plains forest desert jungle hills mountains ruins tundra road river basement
      Dictionary < MovementType, bool > pass = new Dictionary < MovementType, bool > {
      {
        MovementType.Foot, true
      },
      {
        MovementType.Treads, true
      },
      {
        MovementType.Wheels, true
      },
      {
        MovementType.TreadsAmphi, true
      },
      {
        MovementType.WheelsTraverse, true
      },
      {
        MovementType.Flight, true
      },
      {
        MovementType.FlightFlyby, true
      },
      {
        MovementType.Immobile, false
      },
    };
      switch(self.mobility) {
        case MovementType.Foot:
        ability =
          new int[] {
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0
          };
        pass = new Dictionary < MovementType, bool > {
          {
            MovementType.Foot, false
          },
          {
            MovementType.Treads, true
          },
          {
            MovementType.Wheels, false
          },
          {
            MovementType.TreadsAmphi, true
          },
          {
            MovementType.WheelsTraverse, false
          },
          {
            MovementType.Flight, true
          },
          {
            MovementType.FlightFlyby, true
          },
          {
            MovementType.Immobile, false
          },
        };
        break;
        case MovementType.Treads:
          ability =
        new int[] {
          1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0
        };
        pass = new Dictionary < MovementType, bool > {
          {
            MovementType.Foot, false
          },
          {
            MovementType.Treads, false
          },
          {
            MovementType.Wheels, false
          },
          {
            MovementType.TreadsAmphi, false
          },
          {
            MovementType.WheelsTraverse, false
          },
          {
            MovementType.Flight, true
          },
          {
            MovementType.FlightFlyby, true
          },
          {
            MovementType.Immobile, false
          },
        };
        break;
        case MovementType.Wheels:
          ability =
        new int[] {
          1, 0, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0
        };
        pass = new Dictionary < MovementType, bool > {
          {
            MovementType.Foot, false
          },
          {
            MovementType.Treads, false
          },
          {
            MovementType.Wheels, false
          },
          {
            MovementType.TreadsAmphi, false
          },
          {
            MovementType.WheelsTraverse, false
          },
          {
            MovementType.Flight, true
          },
          {
            MovementType.FlightFlyby, true
          },
          {
            MovementType.Immobile, false
          },
        };
        break;
        case MovementType.TreadsAmphi:
          ability =
        new int[] {
          1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0
        };
        pass = new Dictionary < MovementType, bool > {
          {
            MovementType.Foot, false
          },
          {
            MovementType.Treads, false
          },
          {
            MovementType.Wheels, false
          },
          {
            MovementType.TreadsAmphi, false
          },
          {
            MovementType.WheelsTraverse, false
          },
          {
            MovementType.Flight, true
          },
          {
            MovementType.FlightFlyby, true
          },
          {
            MovementType.Immobile, false
          },
        };
        break;
        case MovementType.WheelsTraverse:
          ability =
        new int[] {
          1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0
        };
        pass = new Dictionary < MovementType, bool > {
          {
            MovementType.Foot, false
          },
          {
            MovementType.Treads, false
          },
          {
            MovementType.Wheels, false
          },
          {
            MovementType.TreadsAmphi, false
          },
          {
            MovementType.WheelsTraverse, false
          },
          {
            MovementType.Flight, true
          },
          {
            MovementType.FlightFlyby, true
          },
          {
            MovementType.Immobile, false
          },
        };
        break;
        case MovementType.Flight:
          ability =
        new int[] {
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0
        };
        pass = new Dictionary < MovementType, bool > {
          {
            MovementType.Foot, true
          },
          {
            MovementType.Treads, true
          },
          {
            MovementType.Wheels, true
          },
          {
            MovementType.TreadsAmphi, true
          },
          {
            MovementType.WheelsTraverse, true
          },
          {
            MovementType.Flight, false
          },
          {
            MovementType.FlightFlyby, false
          },
          {
            MovementType.Immobile, false
          },
        };
        break;
        case MovementType.FlightFlyby:
          ability =
        new int[] {
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0
        };
        pass = new Dictionary < MovementType, bool > {
          {
            MovementType.Foot, true
          },
          {
            MovementType.Treads, true
          },
          {
            MovementType.Wheels, true
          },
          {
            MovementType.TreadsAmphi, true
          },
          {
            MovementType.WheelsTraverse, true
          },
          {
            MovementType.Flight, true
          },
          {
            MovementType.FlightFlyby, true
          },
          {
            MovementType.Immobile, false
          },
        };
        break;
      }
      while (open.Count > 0) {
        foreach( var idx_dijk in open)
        {
          List < Position > moves = idx_dijk.Key.Adjacent(width, height);
          foreach(Position mov in moves)
          if (open.ContainsKey(mov) ||
            closed.ContainsKey(mov) ||
            d[mov.x, mov.y] >= wall ||
            d[mov.x, mov.y] <= idx_dijk.Value + 1) {

          }
          else if (
            ability[grid[mov.x - 1, mov.y - 1]] == 1 &&
          placing[mov.x - 1, mov.y - 1] == null)
          {
            fringe[mov] = (idx_dijk.Value + 1);
            d[mov.x, mov.y] = idx_dijk.Value + 1;
          }
          else if (
          ability[grid[mov.x - 1, mov.y - 1]] == 1 &&
          (placing[mov.x - 1, mov.y - 1] != null &&
          (Math.Abs(self.x - (mov.x - 1)) + Math.Abs(self.y - (mov.y - 1)) < self.speed &&
            (pass[placing[mov.x - 1, mov.y - 1].mobility] ||
          (placing[mov.x - 1, mov.y - 1].color == self.color &&
          placing[mov.x - 1, mov.y - 1].mobility != MovementType.Immobile)
          ) ) ) )
          {
            fringe[mov] = (idx_dijk.Value + 1);
            d[mov.x, mov.y] = idx_dijk.Value + 1;
          }
        }
        foreach( var kv in open)
        {
          closed[kv.Key] = (kv.Value);
        }
        open.Clear();
        foreach( var kv in fringe)
        {
          open[kv.Key] = (kv.Value);
        }
        fringe.Clear();

      }

      for (int i = 1;
      i < width - 1;
      i ++)
      {
        for (int j = 1;
        j < height - 1;
        j ++)
        {
          if (d[i, j] == goal && placing[i - 1, j - 1] != null)
          {
            d[i, j] = 3333; // ((pass[placing[i - 1, j - 1].mobility]) ? 0 : wall);

          }
          else if (placing[i - 1, j - 1] != null)
          {
            //d[i, j] += 0.5F;
          }
        }
      }
      return d;
    }

    float[, ] dijkstra(Piece self, int[, ] grid, Piece[, ] placing, int targetX, int targetY)
    {

      int width = grid.GetLength(0) + 2;
      int height = grid.GetLength(1) + 2;
      float unexplored = 1111;
      float goal = 0;
      int wall = 2222;

      float[, ] d = new float[width, height];

      for (int i = 1;
      i < width - 1;
      i ++)
      {
        for (int j = 1;
        j < height - 1;
        j ++)
        d[i, j] = unexplored;
      }

      for (int i = 0;
      i < width;
      i ++)
      {
        d[i, 0] = wall;
        d[i, (height - 1)] = wall;

      }
      for (int j = 1;
      j < height - 1;
      j ++)
      {
        d[ 0, j] = wall;
        d[(width - 1), j] = wall;
      }
      d[targetX + 1, targetY + 1] = goal;

      d = dijkstraInner(self, grid, placing, d);

      return d;
    }
    float[, ] dijkstra(Piece self, int[, ] grid, Piece[, ] placing, int[] targetColors)
    {

      int width = grid.GetLength(0) + 2;
      int height = grid.GetLength(1) + 2;
      float unexplored = 1111;
      float goal = 0;
      int wall = 2222;

      float[, ] d = new float[width, height];

      for (int i = 1;
      i < width - 1;
      i ++)
      {
        for (int j = 1;
        j < height - 1;
        j ++)
        d[i, j] = unexplored;
      }

      for (int i = 0;
      i < width;
      i ++)
      {
        d[i, 0] = wall;
        d[i, (height - 1)] = wall;

      }
      for (int j = 1;
      j < height - 1;
      j ++)
      {
        d[ 0, j] = wall;
        d[(width - 1), j] = wall;
      }
      for (int i = 1;
      i < width - 1;
      i ++)
      {
        for (int j = 1;
        j < height - 1;
        j ++)
        {
          if (targetColors.Any(c => placing[i - 1, j - 1] != null && c == placing[i - 1, j - 1].color) )
          {
            //if (placing[i - 1, j - 1].name == "Castle" || placing[i - 1, j - 1].name == "Estate")
            d[i, j] = goal;
          }
        }
      }
      d = dijkstraInner(self, grid, placing, d);

      return d;
    }
    float[, ] SmartDijkstra(Piece self, int currentWeapon, int[, ] grid, Piece[, ] placing, int[] targetColors)
    {

      int width = grid.GetLength(0) + 2;
      int height = grid.GetLength(1) + 2;
      float unexplored = 1111;
      int wall = 2222;

      float[, ] d = new float[width, height];

      for (int i = 1;
      i < width - 1;
      i ++)
      {
        for (int j = 1;
        j < height - 1;
        j ++)
        {
          if (placing[i - i, j - 1] == null)
          d[i, j] = unexplored;
          else
          d[i, j] = wall;
        }
      }

      for (int i = 0;
      i < width;
      i ++)
      {
        d[i, 0] = wall;
        d[i, (height - 1)] = wall;
      }
      for (int j = 1;
      j < height - 1;
      j ++)
      {
        d[ 0, j] = wall;
        d[(width - 1), j] = wall;
      }
      if (currentWeapon > -1)
        d = DijkstraAttackPositions(self.weaponry[currentWeapon], self.mobility, self.color, targetColors, grid, placing, d);

      return d;
    }
    public float[, ] gradient = new float[ 27, 27];
    List < DirectedPosition > getDijkstraPath(Piece active, int[, ] grid, Piece[, ] placing)
    {
      int width = grid.GetLength(0);
      int height = grid.GetLength(1);
      List < DirectedPosition > path = new List < DirectedPosition >();
      int currentX = active.x, currentY = active.y;
      Direction currentFacing = active.facing;
      Position newpos = new Position(currentX, currentY);
      float[, ] rad = new float[width + 2, height + 2], rad0 = new float[width + 2, height + 2], rad1 = new float[width + 2, height + 2];
      Position best0 = null, best1 = null;
      List < Position > bests0 = new List < Position >(), bests1 = new List < Position >();
      Dictionary < Position, Position > mtt0 = new Dictionary < Position, Position >(), mtt1 = new Dictionary < Position, Position >();
      float choice = -1, eff0 = 0, eff1 = 0;
      if (active.weaponry[ 1].kind != WeaponType.None)
      {
        rad1 = ViableMoves(active, 1, grid, placing);
        bests1 = bestMoves.Clone();
        mtt1 = movesToTargets.Clone();
        var bd = bests1.OrderByDescending(p => (movesToTargets.ContainsKey(p))
          ? active.weaponry[ 1].multipliers[Piece.PieceTypeAsNumber(placing[movesToTargets[p].x, movesToTargets[p].y].kind)]
        : 0.005F * rad1[p.x + 1, p.y + 1] );
        best1 = bd.TakeWhile(p => (movesToTargets.ContainsKey(p))
          ? active.weaponry[ 1].multipliers[Piece.PieceTypeAsNumber(placing[movesToTargets[p].x, movesToTargets[p].y].kind)] ==
        active.weaponry[ 1].multipliers[Piece.PieceTypeAsNumber(placing[movesToTargets[bd.First()].x, movesToTargets[bd.First()].y].kind)]
        : 0.005F * rad1[p.x + 1, p.y + 1] == 0.005F * rad1[bd.First().x + 1, bd.First().y + 1]
        ).RandomElement();
        //best1 = bests1.RandomElement();
        eff1 = (mtt1.ContainsKey(best1))
        ? active.weaponry[ 1].multipliers[Piece.PieceTypeAsNumber(placing[mtt1[best1].x, mtt1[best1].y].kind)]
        : 0;
        choice = 1; //rad1[best1.x + 1, best1.y + 1];

      }
      if (active.weaponry[ 0].kind != WeaponType.None)
      {
        rad0 = ViableMoves(active, 0, grid, placing);
        bests0 = bestMoves.Clone();
        mtt0 = movesToTargets.Clone();
        var bd = bests0.OrderByDescending(p => (movesToTargets.ContainsKey(p))
          ? active.weaponry[ 0].multipliers[Piece.PieceTypeAsNumber(placing[movesToTargets[p].x, movesToTargets[p].y].kind)]
        : 0.005F * rad0[p.x + 1, p.y + 1] );
        best0 = bd.TakeWhile(p => (movesToTargets.ContainsKey(p))
          ? active.weaponry[ 0].multipliers[Piece.PieceTypeAsNumber(placing[movesToTargets[p].x, movesToTargets[p].y].kind)] ==
        active.weaponry[ 0].multipliers[Piece.PieceTypeAsNumber(placing[movesToTargets[bd.First()].x, movesToTargets[bd.First()].y].kind)]
        : 0.005F * rad0[p.x + 1, p.y + 1] == 0.005F * rad0[bd.First().x + 1, bd.First().y + 1]
        ).RandomElement();
        eff0 = (mtt0.ContainsKey(best0))
        ? active.weaponry[ 0].multipliers[Piece.PieceTypeAsNumber(placing[mtt0[best0].x, mtt0[best0].y].kind)]
        : 0;
        //choice = (choice == 2222) ? 1 : (best1 != null && rad1[best1.x + 1, best1.y + 1] > rad0[best0.x + 1, best0.y + 1]) ? 1 : 0;
        if (eff1 > eff0) {
          choice = 1;
        }
        else if (eff0 > eff1) {
          choice = 0;
        }
        else if (active.weaponry[ 1].damage > active.weaponry[ 0].damage)
        {
          choice = 1;
        }
        else if (active.weaponry[ 0].damage > active.weaponry[ 1].damage)
        {
          choice = 0;
        }
        else
        {
          choice = (best1 != null && rad1[best1.x + 1, best1.y + 1] > rad0[best0.x + 1, best0.y + 1] ) ? 1: 0;
        }
      }
      else
      {
        choice = (active.weaponry[ 1].kind == WeaponType.None) ? - 1: 1;
      }
      switch((int) choice) {
        case -1: bestMoves = new List < Position > {
          new Position(active.x, active.y)
        };
        best = new Position(active.x, active.y);
        movesToTargets = new Dictionary < Position, Position >();
        currentlyFiring = -1;
        //gradient.Fill(2222);
        break;
        case 0: bestMoves = bests0.Clone();
        rad = rad0;
        best = new Position(best0.x, best0.y);
        movesToTargets = mtt0.Clone();
        currentlyFiring = 0;
        break;
        case 1: bestMoves = bests1.Clone();
        rad = rad1;
        best = new Position(best1.x, best1.y);
        movesToTargets = mtt1.Clone();
        currentlyFiring = 1;
        break;
      }
      if (currentlyFiring > -1 && movesToTargets.ContainsKey(best)) {
        target = new DirectedPosition(movesToTargets[best].x, movesToTargets[best].y);
        target = DirectedPosition.TurnToFace(best, movesToTargets[best]);
        //                                active.weaponry[currentlyFiring].minRange, active.weaponry[currentlyFiring].maxRange).Where(pos => PieceGrid[pos.x, pos.y] != null && active.isOpposed(PieceGrid[pos.x, pos.y])).RandomElement();
      }
      else target = null;
      /*if (best.x == active.x && best.y == active.y)// && ((0 == placing[newX, newY].color) ? 0 != active.color : 0 == active.color))
      {
          return new List<DirectedPosition> { }; //new DirectedPosition {x=active.x, y=active.y, dir= active.facing }
      }*/
      writeShowLog("Choice is: " + choice);
      writeShowLog("Best is: " + best.x + ", " + best.y);
      writeShowLog("Distance is: " + (rad[best.x + 1, best.y + 1] ) );
      /*
      foreach (Position p in bestMoves)
      {
          writeShowLog("    " + p.x + ", " + p.y + " with an occupant of " + ((placing[p.x, p.y] != null) ? placing[p.x, p.y].name : "EMPTY"));
      }*/
      DirectedPosition oldpos = new DirectedPosition(best.x, best.y);
      path.Add(new DirectedPosition(best.x, best.y));
      if (best.x == active.x && best.y == active.y) {

      }
      else {
        for (int f = 0;
        f < active.speed;
        f ++)
        {
          Dictionary < Position, float > near = new Dictionary < Position, float > () {
          {
            oldpos, rad[oldpos.x + 1, oldpos.y + 1]
          }
        }; // { { oldpos, rad[oldpos.x + 1, oldpos.y + 1] } }
          foreach(Position pos in oldpos.Adjacent(width, height))
          near[pos] = rad[pos.x + 1, pos.y + 1];
          var ordered = near.OrderBy(kv => kv.Value);
          newpos = ordered.TakeWhile(kv => kv.Value == ordered.First().Value).RandomElement().Key;
          if (near.All(e => e.Value == near[newpos]))
            return new List < DirectedPosition >();
          #if DEBUG
          StringBuilder sb = new StringBuilder();
          for (int jj = height;
          jj >= 1;
          jj --)
          {
            for (int ii = 1;
            ii < width + 1;
            ii ++)
            {
              sb.AppendFormat("{0,5}", rad[ii, jj]);
            }
            sb.AppendLine();
          }
          writeShowLog(sb.ToString());
          #endif

          int newX = newpos.x, newY = newpos.y;
          if (!(newX == currentX && newY == currentY)) {
            currentX = newX;
            currentY = newY;
            //                    d_inv = dijkstraInner(active, grid, placing, d_inv);
          }
          DirectedPosition dp = new DirectedPosition(currentX, currentY, currentFacing);
          if (dp.x == active.x && dp.y == active.y) //bestMoves.Any(b => b.x == dp.x && b.y == dp.y))// && ((0 == placing[newX, newY].color) ? 0 != active.color : 0 == active.color)) {
            //oldpos = new DirectedPosition(currentX, currentY, currentFacing);
            writeShowLog("Found target.");
            path.Add(dp);
            f = active.speed + 10;
          }
          else {
            writeShowLog("Continuing pathfind, f is " + f + ", position is " + dp.x + ", " + dp.y);
            if (path.Last().x == dp.x && path.Last().y == dp.y) {
              writeShowLog("Tried to reach unreachable target!!!");
            }
            path.Add(dp);
          }
          oldpos = new DirectedPosition(currentX, currentY, currentFacing);
        }
      }
      path.Reverse();
      path[ 0].dir = active.facing;
      DirectedPosition old2 = new DirectedPosition(path.First().x, path.First().y);
      for (int i = 1;
      i < path.Count;
      i ++)
      {
        currentX = old2.x;
        currentY = old2.y;
        int newX = path[i].x;
        int newY = path[i].y;
        if (newY > currentY) {
          path[i].dir = Direction.SE;
        }
        else if (newY < currentY) {
          path[i].dir = Direction.NW;

        }
        else {
          if (newX < currentX)
            path[i].dir = Direction.SW;
          else
            path[i].dir = Direction.NE;
        }
        old2 = new DirectedPosition(path[i].x, path[i].y);
      }
      /*while (placing[path.Last().x, path.Last().y] != null && path.Count > 0)
      {
          path.RemoveAt(path.Count - 1);
          if (path.Count == 0)
              return path;
          currentX = path.Last().x;
          currentY = path.Last().y;
          currentFacing = path.Last().dir;
      }*/
      /*            DirectedPosition dpos = path.First();
                  for (int i = 1; i < path.Count; i++)
                  {
                      if (path[i].x == dpos.x && path[i].y == dpos.y)
                      {
                          path.RemoveAt(i - 1);
                          i--;
                      }
                      else
                      {
                          dpos = path[i];
                      }
                  }*/
      return path;
    }
    List < DirectedPosition > getDijkstraPath(Piece active, int[, ] grid, Piece[, ] placing, int targetX, int targetY)
    {
      int width = grid.GetLength(0);
      int height = grid.GetLength(1);
      List < DirectedPosition > path = new List < DirectedPosition >();
      int currentX = active.x, currentY = active.y;
      Direction currentFacing = active.facing;
      Position newpos = new Position(currentX, currentY);
      int choice = -1;
      switch((int) choice) {
        case -1: bestMoves = new List < Position > {
          new Position(targetX, targetY)
        };
        best = new Position(targetX, targetY);
        movesToTargets = new Dictionary < Position, Position >();
        currentlyFiring = -1;
        //gradient.Fill(2222);
        break;
      }
      target = null;
      /*if (best.x == active.x && best.y == active.y)// && ((0 == placing[newX, newY].color) ? 0 != active.color : 0 == active.color))
      {
          return new List<DirectedPosition> { }; //new DirectedPosition {x=active.x, y=active.y, dir= active.facing }
      }*/
      writeShowLog("Choice is: " + choice);
      writeShowLog("Best is: " + best.x + ", " + best.y);
      /*
      foreach (Position p in bestMoves)
      {
          writeShowLog("    " + p.x + ", " + p.y + " with an occupant of " + ((placing[p.x, p.y] != null) ? placing[p.x, p.y].name : "EMPTY"));
      }*/
      DirectedPosition oldpos = new DirectedPosition(best.x, best.y);
      path.Add(new DirectedPosition(best.x, best.y));
      if (best.x == active.x && best.y == active.y) {

      }
      else {
        for (int f = 0;
        f < active.speed;
        f ++)
        {
          Dictionary < Position, float > near = new Dictionary < Position, float > () {
          {
            oldpos, outward[oldpos.x + 1, oldpos.y + 1]
          }
        }; // { { oldpos, rad[oldpos.x + 1, oldpos.y + 1] } }
          foreach(Position pos in oldpos.Adjacent(width, height))
          near[pos] = outward[pos.x + 1, pos.y + 1];
          var ordered = near.OrderBy(kv => kv.Value);
          newpos = ordered.TakeWhile(kv => kv.Value == ordered.First().Value).RandomElement().Key;
          if (near.All(e => e.Value == near[newpos]))
            return new List < DirectedPosition >();
          #if DEBUG
          StringBuilder sb = new StringBuilder();
          for (int jj = height;
          jj >= 1;
          jj --)
          {
            for (int ii = 1;
            ii < width + 1;
            ii ++)
            {
              sb.AppendFormat("{0,5}", outward[ii, jj]);
            }
            sb.AppendLine();
          }
          writeShowLog(sb.ToString());
          #endif

          int newX = newpos.x, newY = newpos.y;
          if (!(newX == currentX && newY == currentY)) {
            currentX = newX;
            currentY = newY;
            //                    d_inv = dijkstraInner(active, grid, placing, d_inv);
          }
          DirectedPosition dp = new DirectedPosition(currentX, currentY, currentFacing);
          if (dp.x == active.x && dp.y == active.y) //bestMoves.Any(b => b.x == dp.x && b.y == dp.y))// && ((0 == placing[newX, newY].color) ? 0 != active.color : 0 == active.color)) {
            //oldpos = new DirectedPosition(currentX, currentY, currentFacing);
            writeShowLog("Found target.");
            path.Add(dp);
            f = active.speed + 10;
          }
          else {
            writeShowLog("Continuing pathfind, f is " + f + ", position is " + dp.x + ", " + dp.y);
            if (path.Last().x == dp.x && path.Last().y == dp.y) {
              writeShowLog("Tried to reach unreachable target!!!");
            }
            path.Add(dp);
          }
          oldpos = new DirectedPosition(currentX, currentY, currentFacing);
        }
      }
      path.Reverse();
      path[ 0].dir = active.facing;
      DirectedPosition old2 = new DirectedPosition(path.First().x, path.First().y);
      for (int i = 1;
      i < path.Count;
      i ++)
      {
        currentX = old2.x;
        currentY = old2.y;
        int newX = path[i].x;
        int newY = path[i].y;
        if (newY > currentY) {
          path[i].dir = Direction.SE;
        }
        else if (newY < currentY) {
          path[i].dir = Direction.NW;

        }
        else {
          if (newX < currentX)
            path[i].dir = Direction.SW;
          else
            path[i].dir = Direction.NE;
        }
        old2 = new DirectedPosition(path[i].x, path[i].y);
      }
      /*while (placing[path.Last().x, path.Last().y] != null && path.Count > 0)
      {
          path.RemoveAt(path.Count - 1);
          if (path.Count == 0)
              return path;
          currentX = path.Last().x;
          currentY = path.Last().y;
          currentFacing = path.Last().dir;
      }*/
      /*            DirectedPosition dpos = path.First();
                  for (int i = 1; i < path.Count; i++)
                  {
                      if (path[i].x == dpos.x && path[i].y == dpos.y)
                      {
                          path.RemoveAt(i - 1);
                          i--;
                      }
                      else
                      {
                          dpos = path[i];
                      }
                  }*/
      return path;
    }

    private int failCount = 0;
    void RetryPlacement () {
      failCount ++;
      Console.WriteLine("\n\n!!!!! P L A C E M E N T   F A I L U R E   " + failCount + " !!!!!\n\n");
      if (failCount > 20) {
        Console.WriteLine("Too many placement failures.");
        Console.In.ReadLine();
        return;
      }
      PlacePieces();
    }
    public void PlacePieces() {
      int[] allcolors = {
        1, 2, 3, 4, 5, 6, 7
      };
      Colors = new int[ 4];
      ReverseColors = new int[ 8];
      bool[] taken = {
        false, false, false, false, false, false, false
      };
      for (int i = 1;
      i < 4;
      i ++)
      {
        int col = (i == 1) ? r.Next(1, 7): r.Next (7);
        while (taken[col])
          col = (i == 1) ? r.Next(1, 7): r.Next (7);
        Colors[i] = allcolors[col];
        ReverseColors[Colors[i]] = i;
        taken[col] = true;
      }
      Colors[ 0] = 0;
      ReverseColors[ 0] = 0;

      for (int section = 0;
      section < 2;
      section ++)
      {
        int rx = (width / 4) + (width / 2) * (section % 2);
        int ry = 3 + (height / 6);
        //processSingleOutlined(facilityps[(colors[section] == 0) ? 3 : 2], colors[section], dirs[r.Next(4)])
        if (Colors[section] == 0) {
          PieceGrid[rx, ry] = new Piece("Estate", Colors[section], rx, ry);
          targetX[ 1] = rx;
          targetY[ 1] = ry;
          targetX[ 2] = rx;
          targetY[ 2] = ry;
          targetX[ 3] = rx;
          targetY[ 3] = ry;
        }
        else {
          PieceGrid[rx, ry] = new Piece("Castle", Colors[section], rx, ry);
          targetX[ 0] = rx;
          targetY[ 0] = ry;
        }
        FieldMap.Land[rx, ry] = 10; // +Colors[section];
        for (int i = rx - (width / 6);
        i < rx + (width / 6);
        i ++)
        {
          for (int j = ry - (height / 6);
          j < ry + (height / 6);
          j ++)
          {
            if (PieceGrid[i, j] != null)
              continue;
            //r.Next(14) <= 2
            if (r.Next(14) <= 2 && (FieldMap.Land[i, j] == 0 || FieldMap.Land[i, j] == 1 || FieldMap.Land[i, j] == 2 || FieldMap.Land[i, j] == 4 || FieldMap.Land[i, j] == 8)) {
              //
              PieceGrid[i, j] = new Piece(r.Next(24, 28), Colors[section], i, j);
              FieldMap.Land[i, j] = 10; // +Colors[section];
              //processSingleOutlined(facilityps[r.Next(3) % 2], colors[section], dirs[r.Next(4)]);
            }

          }
        }
      }
      for (int section = 2;
      section < 4;
      section ++)
      {
        int rx = (width / 4) + (width / 2) * (section % 2);
        int ry = height - 3 - (height / 6);
        PieceGrid[rx, ry] = new Piece(((Colors[section] == 0) ? Piece.PieceLookup[ "Estate"]: Piece.PieceLookup[ "Castle"] ), Colors[section], rx, ry);
        FieldMap.Land[rx, ry] = 10; // +Colors[section];
        for (int i = rx - (width / 8);
        i < rx + (width / 8);
        i ++)
        {
          for (int j = ry - (height / 8);
          j < ry + (height / 8);
          j ++)
          {
            if (PieceGrid[i, j] != null)
              continue;
            //r.Next(14) <= 2
            if (r.Next(14) <= 2 && (FieldMap.Land[i, j] == 0 || FieldMap.Land[i, j] == 1 || FieldMap.Land[i, j] == 2 || FieldMap.Land[i, j] == 4 || FieldMap.Land[i, j] == 8)) {
              PieceGrid[i, j] = new Piece(r.Next(24, 28), Colors[section], i, j);
              FieldMap.Land[i, j] = 10; // +Colors[section];

            }

          }
        }
      }
      List < Tuple < int, int >> guarantee = new List < Tuple < int, int >>();
      for (int section = 0;
      section < 4;
      section ++) // section < 4
      {
        for (int i = (width / 2) * (section % 2);
        i < (width / 2) + (width / 2) * (section % 2);
        i ++)
        {
          for (int j = (section / 2 == 0) ? 0: height / 2;
          j < ((section / 2 == 0) ? height / 2: height);
          j ++)
          {
            if (PieceGrid[i, j] != null)
              continue;
            int currentPiece = Piece.TerrainToPieces[FieldMap.Land[i, j]].RandomElement();
            //foot 0-0, treads 1-5, wheels 6-8, flight 9-10
            if (r.Next(25) <= 3) {
              //if(Piece.TerrainToMobilities[FieldMap.Land[i,j]].Contains(MovementType.TreadsAmphi))
              //    PieceGrid[i, j] = new Piece(Piece.PieceLookup["Tank_T"], Colors[section], section, i, j);
              //else
              PieceGrid[i, j] = new Piece(currentPiece, Colors[section], section, i, j);
            }

          }
        }
        /*if (guarantee.Count == section)
        {
            int rgx = r.Next((width / 2) * (section % 2) + 1, (width / 2) - 1 + (width / 2) * (section % 2));
            int rgy = r.Next((section / 2 == 0) ? 1 : height / 2, ((section / 2 == 0) ? height / 2 : height - 1));
            int problems = 0;
            while (PieceGrid[rgx, rgy] != null)
            {
                rgx = r.Next((width / 2) * (section % 2) + 1, (width / 2) - 1 + (width / 2) * (section % 2));
                rgy = r.Next((section / 2 == 0) ? 1 : height / 2, ((section / 2 == 0) ? height / 2 : height - 1));
                if (PieceGrid[rgx, rgy] != null)
                    problems++;
                if (problems > 10)
                {
                    RetryPlacement();
                    return;
                }
            }
            PieceGrid[rgx, rgy] = new Piece(Piece.TerrainToPieces[FieldMap.Land[rgx, rgy]].RandomElement(), Colors[section], section, rgx, rgy);
        }*/

      }
      for (int i = 1;
      i < width - 1;
      i ++)
      {
        for (int j = 1;
        j < height - 1;
        j ++)
        {
          if (r.Next(30) <= 1 && PieceGrid[i, j] == null) {
            int rs = 0; // r.Next(4);
            int currentPiece = Piece.TerrainToPieces[FieldMap.Land[i, j]].RandomElement();
            PieceGrid[i, j] = new Piece(currentPiece, Colors[rs], rs, i, j);

          }
        }
      }
      Piece temp = PieceGrid.RandomFactionPiece(Colors[ActingFaction]);
      ActivePiece = new Piece(temp);
      PieceGrid[temp.x, temp.y] = null;
    }
    public List < DirectedPosition > BestPath;
    public DirectedPosition FuturePosition, target;
    public int currentlyFiring = -1;
    private bool killSuccess = false, hitSuccess = false;
    private int previousHP;
    private Thread thr = null;
    public void dispose() {
      #if DEBUG
      //File.WriteAllText("log.txt", log.ToString());
      #endif
      if (thr != null)
        thr.Abort();
    }
    public void ShowTargets(Piece u, Weapon w) {
      for (int i = 0;
      i < width;
      i ++)
      {
        for (int j = 0;
        j < height;
        j ++)
        {
          if (PieceGrid[i, j] != null && u.isOpposed(PieceGrid[i, j])
            && Math.Abs(u.x - i) + Math.Abs(u.y - j) >= w.minRange
            && Math.Abs(u.x - i) + Math.Abs(u.y - j) <= w.maxRange
            && w.multipliers[Piece.PieceTypeAsNumber(PieceGrid[i, j].kind)] > 0)
          {
            Speech s = new Speech {
              large = false, x = i, y = j,
              text = (100 - PieceGrid[i, j].dodge * 10) + "% / " +
                (int)((w.multipliers[Piece.PieceTypeAsNumber(PieceGrid[i, j].kind)] -0.1f * PieceGrid[i, j].armor) * w.damage) +""
            };
            speaking.Add(s);
            FieldMap.Highlight[i, j] = HighlightType.Bright;
          }
          else
          {
            FieldMap.Highlight[i, j] = HighlightType.Dim;
          }
        }
      }
    }
    float[, ] outward;
    public void advanceTurn() {
      PieceGrid[ActivePiece.x, ActivePiece.y] = new Piece(ActivePiece);
      ActingFaction = (ActingFaction + 1) % 4;
      Piece temp = PieceGrid.RandomFactionPiece(Colors[ActingFaction]);
      ActivePiece = new Piece(temp);
      PieceGrid[temp.x, temp.y] = null;
      speaking.Clear();
      if (ActingFaction == 1) GameGDX.state = GameState.PC_Select_Move;
      else GameGDX.state = GameState.NPC_Play;

      CurrentMode = Mode.Selecting;
      TaskSteps = 0;
    }

    public void ProcessStep() {
      if (GameGDX.state == GameState.PC_Select_Move) {

        Effects.CenterCamera(ActivePiece.x, ActivePiece.y, 0.5F);
        outward = dijkstra(ActivePiece, FieldMap.Land, PieceGrid, ActivePiece.x, ActivePiece.y);
        for (int i = 0;
        i < width;
        i ++)
        {
          for (int j = 0;
          j < height;
          j ++)
          {
            FieldMap.Highlight[i, j] = (outward[i + 1, j + 1] > 0 && outward[i + 1, j + 1] <= ActivePiece.speed) ? HighlightType.Bright: HighlightType.Dim;
          }
        }
        FieldMap.Highlight[ActivePiece.x, ActivePiece.y] = HighlightType.Spectrum;
        return;
      }
      if (GameGDX.state == GameState.PC_Select_UI) {
        return;
      }
      if (GameGDX.state == GameState.PC_Select_Action) {
        return;
      }
      TaskSteps ++;
      switch(CurrentMode) {
        case Mode.Selecting:

        if (TaskSteps > 4 && GameGDX.state != GameState.PC_Play_Move && thr != null && thr.ThreadState == ThreadState.Stopped) {
          FuturePosition = new DirectedPosition(ActivePiece.x, ActivePiece.y, ActivePiece.facing);
          for (int i = 0;
          i < width;
          i ++)
          {
            for (int j = 0;
            j < height;
            j ++)
            {
              FieldMap.Highlight[i, j] = HighlightType.Plain;
            }
          }
          TaskSteps = 0;
          GameGDX.stateTime = 0;
          CurrentMode = Mode.Moving;
        }
        else if (TaskSteps <= 1 && (thr == null || thr.ThreadState == ThreadState.Stopped) && GameGDX.state == GameState.NPC_Play) {
          thr = new Thread(() => {
            BestPath = getDijkstraPath(ActivePiece, FieldMap.Land, PieceGrid);
          });
          thr.Start();

          Effects.CenterCamera(ActivePiece.x, ActivePiece.y, 0.5F);
          outward = dijkstra(ActivePiece, FieldMap.Land, PieceGrid, ActivePiece.x, ActivePiece.y);
          for (int i = 0;
          i < width;
          i ++)
          {
            for (int j = 0;
            j < height;
            j ++)
            {
              FieldMap.Highlight[i, j] = (outward[i + 1, j + 1] > 0 && outward[i + 1, j + 1] <= ActivePiece.speed) ? HighlightType.Bright: HighlightType.Dim;
            }
          }
          FieldMap.Highlight[ActivePiece.x, ActivePiece.y] = HighlightType.Spectrum;

        }
        else if (GameGDX.state == GameState.PC_Play_Move) {
          BestPath = getDijkstraPath(ActivePiece, FieldMap.Land, PieceGrid, GameGDX.cursor.x, GameGDX.cursor.y);
          FuturePosition = new DirectedPosition(ActivePiece.x, ActivePiece.y, ActivePiece.facing);
          for (int i = 0;
          i < width;
          i ++)
          {
            for (int j = 0;
            j < height;
            j ++)
            {
              FieldMap.Highlight[i, j] = HighlightType.Plain;
            }
          }
          TaskSteps = 0;
          GameGDX.stateTime = 0;
          CurrentMode = Mode.Moving;
        }
        else if (GameGDX.state == GameState.PC_Play_Action) {
          target = DirectedPosition.TurnToFace(new Position(ActivePiece.x, ActivePiece.y), new Position(GameGDX.cursor.x, GameGDX.cursor.y));
          CurrentMode = Mode.Attacking;
          GameGDX.state = GameState.PC_Play_Action;
          TaskSteps = 0;
        }
        else {
        }
        break;
        case Mode.Moving:
          ActivePiece.x = FuturePosition.x;
        ActivePiece.y = FuturePosition.y;
        if (BestPath.Count <= 0 || TaskSteps > ActivePiece.speed + 1) {
          //false == (ActivePiece.weaponry[0].kind == WeaponType.None && ActivePiece.weaponry[1].kind == WeaponType.None)
          /*

              (Position.WithinRange(ActivePiece.x, ActivePiece.y,
          ActivePiece.weaponry[currentlyFiring].minRange, ActivePiece.weaponry[currentlyFiring].minRange, width, height,
          ActivePiece.weaponry[currentlyFiring].maxRange, ActivePiece.weaponry[currentlyFiring].maxRange).Any(
          pos => PieceGrid[pos.x, pos.y] != null && ActivePiece.isOpposed(PieceGrid[pos.x, pos.y])))
           */
          if (GameGDX.state == GameState.PC_Play_Move) {
            ActivePiece.worldX = 20 + ActivePiece.x * 64 + ActivePiece.y * 64;
            ActivePiece.worldY = 6 + ActivePiece.x * 32 - ActivePiece.y * 32;
            GameGDX.state = GameState.PC_Select_UI;
            List < MenuEntry > entries = new List < MenuEntry >();
            if (ActivePiece.weaponry[ 0].kind != WeaponType.None)
            entries.Add(new MenuEntry(ActivePiece.weaponry[ 0].kind.ToString(), () => {
              currentlyFiring = 0;
              ShowTargets(ActivePiece, ActivePiece.weaponry[ 0] );
              CurrentMode = Mode.Selecting;
              TaskSteps = 0;
              GameGDX.state = GameState.PC_Select_Action;
            }) );
            if (ActivePiece.weaponry[ 1].kind != WeaponType.None)
            entries.Add(new MenuEntry(ActivePiece.weaponry[ 1].kind.ToString(), () => {
              currentlyFiring = 1;
              ShowTargets(ActivePiece, ActivePiece.weaponry[ 1] );
              CurrentMode = Mode.Selecting;
              TaskSteps = 0;
              GameGDX.state = GameState.PC_Select_Action;
            }) );

            UI.postActor(UI.makeMenu(entries, ActivePiece.color)); //, ActivePiece.worldX, ActivePiece.worldY);
            TaskSteps = 0;
            CurrentMode = Mode.Selecting;
            break;
          }
          else if (currentlyFiring > -1 && target != null && PieceGrid[target.x, target.y] != null && ActivePiece.isOpposed(PieceGrid[target.x, target.y])) {
            ActivePiece.worldX = 20 + ActivePiece.x * 64 + ActivePiece.y * 64;
            ActivePiece.worldY = 6 + ActivePiece.x * 32 - ActivePiece.y * 32;
            CurrentMode = Mode.Attacking;
            TaskSteps = 0;
            break;
          }

          advanceTurn();
          break;
        }
        FuturePosition = new DirectedPosition(BestPath.First().x, BestPath.First().y, BestPath.First().dir);
        int oldx = ActivePiece.x, oldy = ActivePiece.y;

        ActivePiece.facingNumber = ConvertDirection(FuturePosition.dir);
        ActivePiece.facing = FuturePosition.dir;
        NilTask n = new NilTask(() => {
          ActivePiece.worldX += (FuturePosition.x - oldx) * 4 + (FuturePosition.y - oldy) * 4;
          ActivePiece.worldY += (FuturePosition.x - oldx) * 2 - (FuturePosition.y - oldy) * 2;
          ActivePiece.worldY += ((LocalMap.Depths[FieldMap.Land[FuturePosition.x, FuturePosition.y]] - LocalMap.Depths[FieldMap.Land[oldx, oldy]]) * 3F) / 16F;
        });
        Timer.instance().scheduleTask(n, 0, GameGDX.updateStep / 16F, 15);

        Effects.CenterCamera(FuturePosition, 1F);

        BestPath.RemoveAt(0);

        break;
        case Mode.Attacking:
        if (TaskSteps <= 1) {
          if (target.x - ActivePiece.x <= target.y - ActivePiece.y && (target.x - ActivePiece.x) * -1 <= target.y - ActivePiece.y) {
            ActivePiece.facing = Direction.SE;
            ActivePiece.facingNumber = 0;
            if (PieceGrid[target.x, target.y].speed > 0) {
              PieceGrid[target.x, target.y].facing = Direction.NW;
              PieceGrid[target.x, target.y].facingNumber = 2;
            }
          }
          else if ((target.x - ActivePiece.x) * -1 <= target.y - ActivePiece.y && target.x - ActivePiece.x >= target.y - ActivePiece.y) {
            ActivePiece.facing = Direction.NE;
            ActivePiece.facingNumber = 3;
            if (PieceGrid[target.x, target.y].speed > 0) {
              PieceGrid[target.x, target.y].facing = Direction.SW;
              PieceGrid[target.x, target.y].facingNumber = 1;
            }
          }
          else if (target.x - ActivePiece.x >= target.y - ActivePiece.y && (target.x - ActivePiece.x) * -1 >= target.y - ActivePiece.y) {
            ActivePiece.facing = Direction.NW;
            ActivePiece.facingNumber = 2;
            if (PieceGrid[target.x, target.y].speed > 0) {
              PieceGrid[target.x, target.y].facing = Direction.SE;
              PieceGrid[target.x, target.y].facingNumber = 0;
            }
          }
          else if ((target.x - ActivePiece.x) * -1 >= target.y - ActivePiece.y && target.x - ActivePiece.x <= target.y - ActivePiece.y) {
            ActivePiece.facing = Direction.SW;
            ActivePiece.facingNumber = 1;
            if (PieceGrid[target.x, target.y].speed > 0) {
              PieceGrid[target.x, target.y].facing = Direction.NE;
              PieceGrid[target.x, target.y].facingNumber = 3;
            }
          }
          else {
            ActivePiece.facing = Direction.SE;
            ActivePiece.facingNumber = 0;
            if (PieceGrid[target.x, target.y].speed > 0) {
              PieceGrid[target.x, target.y].facing = Direction.NW;
              PieceGrid[target.x, target.y].facingNumber = 2;
            }
          }

          GameGDX.attackTime = 0;
          /*currentlyFiring = -1;
          if (ActivePiece.weaponry[1].kind != WeaponType.None && ActivePiece.weaponry[0].kind != WeaponType.None)
              currentlyFiring = r.Next(2);
          else if (ActivePiece.weaponry[0].kind != WeaponType.None)
              currentlyFiring = 0;
          else currentlyFiring = 1;*/
          if (currentlyFiring > -1) {
            hitSuccess = PieceGrid[target.x, target.y].attemptDodge(ActivePiece.weaponry[currentlyFiring]);
            if (hitSuccess) {
              previousHP = PieceGrid[target.x, target.y].currentHealth;
              killSuccess = PieceGrid[target.x, target.y].takeDamage(ActivePiece.weaponry[currentlyFiring]);
            }
          }
          else {
            hitSuccess = false;
            killSuccess = false;
          }
          ActivePiece.visual = (ActivePiece.weaponry[ 1].kind == WeaponType.None && ActivePiece.weaponry[ 0].kind == WeaponType.None) ? VisualAction.Normal: VisualAction.Firing;
        }
        else if (TaskSteps > 4 + 1 * (Math.Abs(target.x - ActivePiece.x) + Math.Abs(target.y - ActivePiece.y))) {
          currentlyFiring = -1;
          if (killSuccess)
            PieceGrid[target.x, target.y] = null;
          killSuccess = false;
          hitSuccess = false;
          advanceTurn();

          break;
        }
        else if (TaskSteps == 1 + 1 * (Math.Abs(target.x - ActivePiece.x) + Math.Abs(target.y - ActivePiece.y)) && currentlyFiring > -1) {
          if (hitSuccess || Piece.WeaponDisplays[ActivePiece.unitIndex][currentlyFiring] == 1 || Piece.WeaponDisplays[ActivePiece.unitIndex][currentlyFiring] == 7) {
            GameGDX.receiveTime = 0;
            /*
        int w = ((row < width) ? width - 1 - row + col : col); //height + (width - 1 - row) +
        int h = (row < width) ? col : row - width + col;
             */

            ActivePiece.targeting = new List < DirectedPosition > {
              new DirectedPosition(target.x, target.y, target.dir)
            };

          }
          if (!hitSuccess && PieceGrid[target.x, target.y].speed > 0) {
            //se 0 -> sw -x -y
            //sw 1 -> nw -x +y
            //nw 2 -> ne +x +y
            //ne 3 -> se +x -y
            NilTask avoid = new NilTask(() => {
              PieceGrid[target.x, target.y].worldX += ((PieceGrid[target.x, target.y].facingNumber) % 4 >= 2) ? 2: - 2;
              PieceGrid[target.x, target.y].worldY += ((PieceGrid[target.x, target.y].facingNumber + 1) % 4 >= 2) ? 1: - 1;
            });
            Timer.instance().scheduleTask(avoid, 0, GameGDX.updateStep / 16F, 10);
            NilTask calm = new NilTask(() => {
              PieceGrid[target.x, target.y].worldX -= ((PieceGrid[target.x, target.y].facingNumber) % 4 >= 2) ? 2: - 2;
              PieceGrid[target.x, target.y].worldY -= ((PieceGrid[target.x, target.y].facingNumber + 1) % 4 >= 2) ? 1: - 1;
            });
            Timer.instance().scheduleTask(calm, GameGDX.updateStep, GameGDX.updateStep / 8F, 10);
            NilTask reset = new NilTask(() => {
              PieceGrid[target.x, target.y].worldX = 20 + PieceGrid[target.x, target.y].x * 64 + PieceGrid[target.x, target.y].y * 64;
              PieceGrid[target.x, target.y].worldY = 6 + PieceGrid[target.x, target.y].x * 32 - PieceGrid[target.x, target.y].y * 32;
            });
            Timer.instance().scheduleTask(reset, GameGDX.updateStep * 19 / 8F);

          }
        }
        else if (TaskSteps == 2 + 1 * (Math.Abs(target.x - ActivePiece.x) + Math.Abs(target.y - ActivePiece.y)) && currentlyFiring > -1) {
          if (killSuccess) {
            GameGDX.explodeTime = 0;
            PieceGrid[target.x, target.y].visual = VisualAction.Exploding;
            speaking.Add(new Speech {
              x = target.x, y = target.y, large = true, text = "DEAD"
            });
          }
          else if (hitSuccess) {
            speaking.Add(new Speech {
              x = target.x, y = target.y, large = true, text = (previousHP - PieceGrid[target.x, target.y].currentHealth) + ""
            });
          }

        }
        break;
      }
    }
  }

}

}