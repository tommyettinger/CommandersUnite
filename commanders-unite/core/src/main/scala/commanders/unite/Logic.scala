/**
 * Created by Tommy Ettinger on 7/22/2014.
 */
package commanders.unite {

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.TimeUtils
import commanders.unite._
import commanders.unite.Direction.Direction
import commanders.unite.Extensions._
import commanders.unite.MovementType.MovementType
import commanders.unite.utils.{SmoothAction, RepeatedAction, Timer}
import game.commanders.unite.CommandersUnite
import scala.collection.mutable._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
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
object Logic
{
  type IntMat = Array[Array[Int]]
  type FloatMat = Array[Array[Float]]
  type PieceMat = Array[Array[Piece]]

  var state = GameState.PC_Select_Move
  var previousState = GameState.PC_Select_Move;
  var CurrentMode = Mode.Selecting

  def width = 24

  def height = 24

  var FieldMap = new LocalMap(width, height)
  var PieceGrid = Array.ofDim[Piece](width, height)
  var ActivePiece: Piece = null
  var colors = new Array[Int](4)
  var reverseColors = new Array[Int](8)
  var ActingFaction = 1
  var TaskSteps = 0
  var speaking = new ArrayBuffer[Speech](16)
  var targetX = Array(width / 4, width / 2, width / 4, width / 2)
  var targetY = Array(height / 2, height / 4, height / 2, height / 4)
  var outward = Array.ofDim[Float](width + 2, height + 2)

  def DirectionNames = Array("SE", "SW", "NW", "NE")

  var BestPath = ArrayBuffer[DirectedPosition]()
  var FuturePosition: DirectedPosition = null
  var target: DirectedPosition = null
  var currentlyFiring = -1
  var killSuccess = false
  var hitSuccess = false
  var previousHP: Int = 0
  var thr: Future[ArrayBuffer[DirectedPosition]] = null
  var failCount = 0
  val directions = Array(Direction.SE, Direction.SW, Direction.NW, Direction.NE)
  //foot 0-0, treads 1-5, wheels 6-8, flight 9-10


  /*{ "Infantry", //foot 0 0
                         "Tank", "Artillery", "Artillery_P", "Artillery_S", "Supply_P", //treads 1 5
                         "Artillery_T", "Supply", "Supply_T", //wheels 6 8
                         "Helicopter", "Plane", //flight 9 10
                         "City", "Factory", "Castle", "Capital" //facility
                       };*/


  def r = new Random()

  def ConvertDirection(dir: Direction): Int =
  {
    dir match {
      case Direction.SE => return 0
      case Direction.SW => return 1
      case Direction.NW => return 2
      case Direction.NE => return 3
      case _ => return 0
    }
  }


  var bestMoves = new ArrayBuffer[Position]()
  var movesToTargets = new HashMap[Position, Position]
  var best: Position = null
  var gradient = Array.ofDim[Float](width + 2, height + 2)

  def movementAbility(mobility: MovementType): Array[Int] =
  {
    //plains forest desert jungle hills mountains ruins tundra road river basement
    mobility match {
      case MovementType.Foot =>
        Array(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0)

      case MovementType.Treads =>
        Array(1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0)

      case MovementType.Wheels =>
        Array(1, 0, 1, 0, 0, 0, 0, 1, 1, 0, 0)

      case MovementType.TreadsAmphi =>
        Array(1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0)

      case MovementType.WheelsTraverse =>
        Array(1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0)

      case MovementType.Flight =>
        Array(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0)

      case MovementType.FlightFlyby =>
        Array(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0)
      case _ =>
        Array(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
    }
  }

  def movementPass(mobility: MovementType): HashMap[MovementType, Boolean] =
  {
    mobility match {
      case MovementType.Foot =>
        HashMap[MovementType, Boolean](
          MovementType.Foot -> false,
          MovementType.Treads -> true,
          MovementType.Wheels -> false,
          MovementType.TreadsAmphi -> true,
          MovementType.WheelsTraverse -> false,
          MovementType.Flight -> true,
          MovementType.FlightFlyby -> true,
          MovementType.Immobile -> false
        )
      case MovementType.Treads =>
        HashMap[MovementType, Boolean](
          MovementType.Foot -> false,
          MovementType.Treads -> false,
          MovementType.Wheels -> false,
          MovementType.TreadsAmphi -> false,
          MovementType.WheelsTraverse -> false,
          MovementType.Flight -> true,
          MovementType.FlightFlyby -> true,
          MovementType.Immobile -> false
        )
      case MovementType.Wheels =>
        HashMap[MovementType, Boolean](
          MovementType.Foot -> false,
          MovementType.Treads -> false,
          MovementType.Wheels -> false,
          MovementType.TreadsAmphi -> false,
          MovementType.WheelsTraverse -> false,
          MovementType.Flight -> true,
          MovementType.FlightFlyby -> true,
          MovementType.Immobile -> false
        )
      case MovementType.TreadsAmphi =>
        HashMap[MovementType, Boolean](
          MovementType.Foot -> false,
          MovementType.Treads -> false,
          MovementType.Wheels -> false,
          MovementType.TreadsAmphi -> false,
          MovementType.WheelsTraverse -> false,
          MovementType.Flight -> true,
          MovementType.FlightFlyby -> true,
          MovementType.Immobile -> false
        )
      case MovementType.WheelsTraverse =>
        HashMap[MovementType, Boolean](
          MovementType.Foot -> false,
          MovementType.Treads -> false,
          MovementType.Wheels -> false,
          MovementType.TreadsAmphi -> false,
          MovementType.WheelsTraverse -> false,
          MovementType.Flight -> true,
          MovementType.FlightFlyby -> true,
          MovementType.Immobile -> false
        )
      case MovementType.Flight =>
        HashMap[MovementType, Boolean](
          MovementType.Foot -> true,
          MovementType.Treads -> true,
          MovementType.Wheels -> true,
          MovementType.TreadsAmphi -> true,
          MovementType.WheelsTraverse -> true,
          MovementType.Flight -> false,
          MovementType.FlightFlyby -> false,
          MovementType.Immobile -> false
        )
      case MovementType.FlightFlyby =>
        HashMap[MovementType, Boolean](
          MovementType.Foot -> true,
          MovementType.Treads -> true,
          MovementType.Wheels -> true,
          MovementType.TreadsAmphi -> true,
          MovementType.WheelsTraverse -> true,
          MovementType.Flight -> true,
          MovementType.FlightFlyby -> true,
          MovementType.Immobile -> false
        )
      case _ =>
        HashMap[MovementType, Boolean](
          MovementType.Foot -> false,
          MovementType.Treads -> false,
          MovementType.Wheels -> false,
          MovementType.TreadsAmphi -> false,
          MovementType.WheelsTraverse -> false,
          MovementType.Flight -> false,
          MovementType.FlightFlyby -> false,
          MovementType.Immobile -> false
        )
    }

  }

  def DijkstraAttackPositions(weapon: Weapon, mobility: MovementType, selfColor: Int, targetColors: List[Int], grid: IntMat, placing: PieceMat, d: FloatMat): FloatMat =
  {
    val width = d.length
    val height = d(0).length
    val wall: Float = 2222
    val goal: Float = 0

    val open = new HashMap[Position, Float]()
    val fringe = new HashMap[Position, Float]()
    val closed = new HashMap[Position, Float]()

    val ability = movementAbility(mobility)
    val pass = movementPass(mobility)
    movesToTargets.clear()
    for (i <- 1 until width - 1) {
      for (j <- 1 until height - 1) {
        if (targetColors.exists(c => placing(i - 1)(j - 1) != null && c == placing(i - 1)(j - 1).color)) {
          if (weapon.multipliers(Piece.PieceTypeAsNumber(placing(i - 1)(j - 1).kind)) > 0) {
            val tgt = Position(i - 1, j - 1)
            for (p <- Position.WithinRange(i, j, 1, 1, width - 1, height - 1, weapon.minRange, weapon.maxRange)) {
              if (ability(grid(p.x - 1)(p.y - 1)) == 1 && placing(p.x - 1)(p.y - 1) == null) {
                d(p.x)(p.y) = goal
                open += ((p, goal))
                movesToTargets += ((Position(p.x - 1, p.y - 1), tgt))
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
        else if (d(i)(j) >= wall) {
          closed += ((Position(i, j), wall))
        }
      }
    }


    while (open.size > 0) {
      for (idx_dijk <- open) {
        var moves = idx_dijk._1.Adjacent(width, height)
        for (mov <- moves)
          if (open.contains(mov) ||
            closed.contains(mov) ||
            d(mov.x)(mov.y) >= wall ||
            d(mov.x)(mov.y) <= idx_dijk._2 + 1) {
          }
          else if (
                 ability(grid(mov.x - 1)(mov.y - 1)) == 1
                   && placing(mov.x - 1)(mov.y - 1) == null) {
            fringe += ((mov, idx_dijk._2 + 1))
            d(mov.x)(mov.y) = idx_dijk._2 + 1
          }
          else if (
                 ability(grid(mov.x - 1)(mov.y - 1)) == 1 &&
                   (placing(mov.x - 1)(mov.y - 1) != null &&
                     (pass(placing(mov.x - 1)(mov.y - 1).mobility) ||
                       (placing(mov.x - 1)(mov.y - 1).color == selfColor &&
                         placing(mov.x - 1)(mov.y - 1).mobility != MovementType.Immobile)
                       ))) {
            fringe += ((mov, idx_dijk._2 + 1))
            d(mov.x)(mov.y) = idx_dijk._2 + 1;
          }
      }
      for (kv <- open) {
        closed += ((kv._1, kv._2))
      }
      open.clear()
      for (kv <- fringe) {
        open += ((kv._1, kv._2))
      }
      fringe.clear()

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

  def ViableMoves(self: Piece, currentWeapon: Int, grid: IntMat, placing: PieceMat): FloatMat =
  {
    gradient = SmartDijkstra(self, currentWeapon, grid, placing,
      if (self.color == 0) List(1, 2, 3, 4, 5, 6, 7) else List(0)
    )
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
    val width = gradient.length
    val height = gradient(0).length
    val wall: Float = 2222
    val goal: Float = 0
    val unexplored: Float = 1111

    bestMoves = ArrayBuffer(Position(self.x, self.y))

    var open = HashMap(Position(self.x + 1, self.y + 1) -> goal)
    var fringe = new HashMap[Position, Float]
    var closed = new HashMap[Position, Float]

    var radiate = Array.ofDim[Float](width, height)

    for (i <- 1 until width - 1) {
      for (j <- 1 until height - 1) {
        radiate(i)(j) = unexplored
      }
    }

    for (i <- 0 until width) {
      radiate(i)(0) = wall
      radiate(i)(height - 1) = wall

    }
    for (j <- 1 until height - 1) {
      radiate(0)(j) = wall
      radiate(width - 1)(j) = wall
    }

    for (i <- 0 until width) {
      for (j <- 0 until height) {
        if (radiate(i)(j) >= wall) {
          closed += ((Position(i, j), wall))
        }
      }
    }
    radiate(self.x + 1)(self.y + 1) = goal;
    val ability = movementAbility(self.mobility)
    val pass = movementPass(self.mobility)
    var furthest: Float = 0;
    var lowest: Float = 1000;
    /*
    if (gradient(self.x + 1)(self.y + 1) <= goal)
    {
      radiate = Array.fill[Float](width, height)(wall)
      radiate(self.x + 1)(self.y + 1) = goal
    }
    else
    */
    {
      while (open.size > 0 && furthest < self.speed) {
        for (idx_dijk <- open) {
          var moves = idx_dijk._1.Adjacent(width, height)
          for (mov <- moves)
            if (open.contains(mov) ||
              closed.contains(mov) ||
              radiate(mov.x)(mov.y) >= wall ||
              radiate(mov.x)(mov.y) <= idx_dijk._2 + 1) {
            }
            else if (
                   ability(grid(mov.x - 1)(mov.y - 1)) == 1
                     && placing(mov.x - 1)(mov.y - 1) == null) {
              fringe += ((mov, idx_dijk._2 + 1))
              radiate(mov.x)(mov.y) = idx_dijk._2 + 1
              if (gradient(mov.x)(mov.y) < lowest) {
                //bestMoves.Clear();
                bestMoves += Position(mov.x - 1, mov.y - 1)
                lowest = gradient(mov.x)(mov.y)
              }
              else if (gradient(mov.x)(mov.y) == lowest) {
                // else  if (gradient(mov.x)(mov.y) == lowest)
                bestMoves += Position(mov.x - 1, mov.y - 1)
              }
            }
            else if (
                   ability(grid(mov.x - 1)(mov.y - 1)) == 1 &&
                     (placing(mov.x - 1)(mov.y - 1) != null &&
                       (pass(placing(mov.x - 1)(mov.y - 1).mobility) ||
                         (placing(mov.x - 1)(mov.y - 1).color == self.color &&
                           placing(mov.x - 1)(mov.y - 1).mobility != MovementType.Immobile)
                         ))) {
              fringe += ((mov, idx_dijk._2 + 1))
              radiate(mov.x)(mov.y) = idx_dijk._2 + 1;
            }

        }

        for (kv <- open) {
          closed += ((kv._1, kv._2))
        }
        open.clear()
        for (kv <- fringe) {
          open += ((kv._1, kv._2))
        }
        fringe.clear()
        furthest = furthest + 1
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

    radiate
  }

  def dijkstraInner(self: Piece, grid: IntMat, placing: PieceMat, d: FloatMat): FloatMat =
  {

    val width = d.length
    val height = d(0).length
    val wall: Float = 2222
    val goal: Float = 0

    val open = new HashMap[Position, Float]()
    val fringe = new HashMap[Position, Float]()
    val closed = new HashMap[Position, Float]()

    val ability = movementAbility(self.mobility)
    val pass = movementPass(self.mobility)

    for (i <- 0 until width) {
      for (j <- 0 until height) {
        if (d(i)(j) == goal) {
          open += ((Position(i, j), goal))
        }
        else if (d(i)(j) >= wall) {
          closed += ((Position(i, j), wall))
        }
      }
    }

    while (open.size > 0) {
      for (idx_dijk <- open) {
        var moves = idx_dijk._1.Adjacent(width, height)
        for (mov <- moves)
          if (open.contains(mov) ||
            closed.contains(mov) ||
            d(mov.x)(mov.y) >= wall ||
            d(mov.x)(mov.y) <= idx_dijk._2 + 1) {
          }
          else if (
                 ability(grid(mov.x - 1)(mov.y - 1)) == 1
                   && placing(mov.x - 1)(mov.y - 1) == null) {
            fringe += ((mov, idx_dijk._2 + 1))
            d(mov.x)(mov.y) = idx_dijk._2 + 1
          }
          else if (
                 ability(grid(mov.x - 1)(mov.y - 1)) == 1 &&
                   (placing(mov.x - 1)(mov.y - 1) != null &&
                     (Math.abs(self.x - (mov.x - 1)) + Math.abs(self.y - (mov.y - 1)) < self.speed &&
                       (pass(placing(mov.x - 1)(mov.y - 1).mobility) ||
                         (placing(mov.x - 1)(mov.y - 1).color == self.color &&
                           placing(mov.x - 1)(mov.y - 1).mobility != MovementType.Immobile)
                         )))) {
            fringe += ((mov, idx_dijk._2 + 1))
            d(mov.x)(mov.y) = idx_dijk._2 + 1;
          }
      }

      for (kv <- open) {
        closed += ((kv._1, kv._2))
      }
      open.clear()
      for (kv <- fringe) {
        open += ((kv._1, kv._2))
      }
      fringe.clear()
    }


    for (i <- 1 until width - 1) {
      for (j <- 1 until height - 1) {
        if (d(i)(j) == goal && placing(i - 1)(j - 1) != null) {
          d(i)(j) = 3333; // ((pass[placing[i - 1, j - 1].mobility]) ? 0 : wall);
        }
      }
    }
    d
  }

  def dijkstra(self: Piece, grid: IntMat, placing: PieceMat, targetX: Int, targetY: Int): FloatMat =
  {

    val width = grid.length + 2
    val height = grid(0).length + 2
    val unexplored: Float = 1111
    val goal: Float = 0
    val wall: Float = 2222

    val d = Array.ofDim[Float](width, height)

    for (i <- 1 until width - 1) {
      for (j <- 1 until height - 1) {
        d(i)(j) = unexplored
      }
    }
    for (i <- 0 until width) {
      d(i)(0) = wall
      d(i)(height - 1) = wall
    }
    for (j <- 1 until height - 1) {
      d(0)(j) = wall
      d(width - 1)(j) = wall
    }
    d(targetX + 1)(targetY + 1) = goal

    dijkstraInner(self, grid, placing, d)
  }

  def dijkstra(self: Piece, grid: IntMat, placing: PieceMat, targetColors: Array[Int]): FloatMat =
  {
    val width = grid.length + 2
    val height = grid(0).length + 2
    val unexplored: Float = 1111
    val goal: Float = 0
    val wall: Float = 2222

    var d = Array.ofDim[Float](width, height)

    for (i <- 1 until width - 1) {
      for (j <- 1 until height - 1) {
        d(i)(j) = unexplored
      }
    }
    for (i <- 0 until width) {
      d(i)(0) = wall
      d(i)(height - 1) = wall
    }
    for (j <- 1 until height - 1) {
      d(0)(j) = wall
      d(width - 1)(j) = wall
    }

    for (i <- 1 until width - 1) {
      for (j <- 1 until height - 1) {
        if (targetColors.exists(c => placing(i - 1)(j - 1) != null && c == placing(i - 1)(j - 1).color)) {
          //if (placing[i - 1, j - 1].name == "Castle" || placing[i - 1, j - 1].name == "Estate")
          d(i)(j) = goal;
        }
      }
    }

    dijkstraInner(self, grid, placing, d)
  }

  def findWeapon(piece: Piece, i: Int): Weapon =
  {
    if (i == 0) {
      piece.weaponry._1
    }
    else {
      piece.weaponry._2
    }
  }

  def SmartDijkstra(self: Piece, currentWeapon: Int, grid: IntMat, placing: PieceMat, targetColors: List[Int]): FloatMat =
  {

    val width = grid.length + 2
    val height = grid(0).length + 2
    val unexplored: Float = 1111
    val goal: Float = 0
    val wall: Float = 2222

    var d = Array.ofDim[Float](width, height)

    for (i <- 1 until width - 1) {
      for (j <- 1 until height - 1) {
        d(i)(j) = unexplored
      }
    }
    for (i <- 0 until width) {
      d(i)(0) = wall
      d(i)(height - 1) = wall
    }
    for (j <- 1 until height - 1) {
      d(0)(j) = wall
      d(width - 1)(j) = wall
    }
    if (currentWeapon > -1) {
      d = DijkstraAttackPositions(findWeapon(self, currentWeapon), self.mobility, self.color, targetColors, grid, placing, d)
    };

    d
  }

  def getDijkstraPath(active: Piece, grid: IntMat, placing: PieceMat): ArrayBuffer[DirectedPosition] =
  {
    val width = grid.length
    val height = grid(0).length
    var path = new ArrayBuffer[DirectedPosition]
    var currentX = active.x
    var currentY = active.y
    var currentFacing = active.facing
    var newpos = Position(currentX, currentY)
    var rad = Array.ofDim[Float](width + 2, height + 2)
    var rad0 = Array.ofDim[Float](width + 2, height + 2)
    var rad1 = Array.ofDim[Float](width + 2, height + 2)
    var best0: Position = null
    var best1: Position = null
    var bests0 = new ArrayBuffer[Position]
    var bests1 = new ArrayBuffer[Position]
    var mtt0 = new HashMap[Position, Position]
    var mtt1 = new HashMap[Position, Position]
    var choice = -1
    var eff0: Float = 0
    var eff1: Float = 0
    if (findWeapon(active, 1).kind != WeaponType.Non) {
      rad1 = ViableMoves(active, 1, grid, placing)
      bests1 = bestMoves.clone()
      mtt1 = movesToTargets.clone()
      val bd1 = bests1.sortBy(p => if (movesToTargets.contains(p)) {
        findWeapon(active, 1).multipliers(Piece.PieceTypeAsNumber(placing(movesToTargets(p).x)(movesToTargets(p).y).kind))
      }
                                   else {
                                     0.003F * rad1(p.x + 1)(p.y + 1)
                                   }).reverse
      /*
      println(active.name)

      bd1.map(p => if(movesToTargets.contains(p))
                     println("Found! " + findWeapon(active, 1).multipliers(Piece.PieceTypeAsNumber(placing(movesToTargets(p).x)(movesToTargets(p).y).kind)))
                   else println("Not found. " + 0.005F * rad1(p.x + 1)(p.y + 1) ))
      println()
      */
      best1 = bd1.takeWhile(p => if (movesToTargets.contains(p)) {
        findWeapon(active, 1).multipliers(Piece.PieceTypeAsNumber(placing(movesToTargets(p).x)(movesToTargets(p).y).kind)) ==
          findWeapon(active, 1).multipliers(Piece.PieceTypeAsNumber(placing(movesToTargets(bd1(0)).x)(movesToTargets(bd1(0)).y).kind))
      }
                                 else {
                                   0.003F * rad1(p.x + 1)(p.y + 1) == 0.003F * rad1(bd1(0).x + 1)(bd1(0).y + 1)
                                 }).toArray[Position].RandomItem
      eff1 = if (mtt1.contains(best1)) {
        findWeapon(active, 1).multipliers(Piece.PieceTypeAsNumber(placing(mtt1(best1).x)(mtt1(best1).y).kind))
      }
             else {
               0
             }
      choice = 1
    }
    if (findWeapon(active, 0).kind != WeaponType.Non) {

      rad0 = ViableMoves(active, 0, grid, placing);
      bests0 = bestMoves.clone()
      mtt0 = movesToTargets.clone()
      val bd0 = bests0.sortBy(p => if (movesToTargets.contains(p)) {
        findWeapon(active, 0).multipliers(Piece.PieceTypeAsNumber(placing(movesToTargets(p).x)(movesToTargets(p).y).kind))
      }
                                   else {
                                     0.003F * rad0(p.x + 1)(p.y + 1)
                                   }).reverse
      /*
      println(active.name)
      bd0.map(p => if(movesToTargets.contains(p))
                     println("Found! " + findWeapon(active, 0).multipliers(Piece.PieceTypeAsNumber(placing(movesToTargets(p).x)(movesToTargets(p).y).kind)))
                   else println("Not found. " + 0.005F * rad0(p.x + 1)(p.y + 1) ))
      println()
      */
      best0 = bd0.takeWhile(p => if (movesToTargets.contains(p)) {
        findWeapon(active, 0).multipliers(Piece.PieceTypeAsNumber(placing(movesToTargets(p).x)(movesToTargets(p).y).kind)) ==
          findWeapon(active, 0).multipliers(Piece.PieceTypeAsNumber(placing(movesToTargets(bd0(0)).x)(movesToTargets(bd0(0)).y).kind))
      }
                                 else {
                                   0.003F * rad0(p.x + 1)(p.y + 1) == 0.003F * rad0(bd0(0).x + 1)(bd0(0).y + 1)
                                 }).toArray[Position].RandomItem
      eff0 = if (mtt0.contains(best0)) {
        findWeapon(active, 0).multipliers(Piece.PieceTypeAsNumber(placing(mtt0(best0).x)(mtt0(best0).y).kind))
      }
      else {
        0
      }
      //choice = (choice == 2222) ? 1 : (best1 != null && rad1[best1.x + 1, best1.y + 1] > rad0[best0.x + 1, best0.y + 1]) ? 1 : 0;
      if (eff1 > eff0) {
        choice = 1
      }
      else if (eff0 > eff1) {
        choice = 0
      }
      else if (findWeapon(active, 1).damage > findWeapon(active, 0).damage) {
        choice = 1
      }
      else if (findWeapon(active, 0).damage > findWeapon(active, 1).damage) {
        choice = 0
      }
      else {
        choice = if (best1 != null && rad1(best1.x + 1)(best1.y + 1) > rad0(best0.x + 1)(best0.y + 1)) 1 else 0
      }
    }
    else {
      choice = if (findWeapon(active, 1).kind == WeaponType.Non) -1 else 1
    }
    choice match {
      case -1 =>
        bestMoves = ArrayBuffer(Position(active.x, active.y))
        best = Position(active.x, active.y)
        movesToTargets = new HashMap[Position, Position]
        currentlyFiring = -1
      case 0 =>
        bestMoves = bests0.clone()
        rad = rad0
        best = Position(best0.x, best0.y)
        movesToTargets = mtt0.clone()
        currentlyFiring = 0
      case 1 =>
        bestMoves = bests1.clone()
        rad = rad1
        best = Position(best1.x, best1.y)
        movesToTargets = mtt1.clone()
        currentlyFiring = 1
    }
    if (currentlyFiring > -1 && movesToTargets.contains(best)) {
      target = DirectedPosition.TurnToFace(best, movesToTargets(best))
      //                                active.weaponry[currentlyFiring].minRange, active.weaponry[currentlyFiring].maxRange).Where(pos => PieceGrid[pos.x, pos.y] != null && active.isOpposed(PieceGrid[pos.x, pos.y])).RandomElement();
    }
    else {
      target = null
    }
    var oldpos = DirectedPosition(Position(best.x, best.y), Direction.SE)
    path += DirectedPosition(Position(best.x, best.y), Direction.SE)
    if (best.x == active.x && best.y == active.y) {
    }
    else {
      var f = 0
      while (f < active.speed) {
        var near = HashMap[Position, Float](oldpos.p -> rad(oldpos.p.x + 1)(oldpos.p.y + 1))
        for (pos <- oldpos.p.Adjacent(width, height)) {
          near += ((pos, rad(pos.x + 1)(pos.y + 1)))
        }
        var ordered = near.toSeq.sortBy(_._2)
        newpos = ordered.takeWhile(kv => kv._2 == ordered(0)._2).RandomElement.get._1
        if (near.forall(e => e._2 == near(newpos))) {
          return ArrayBuffer[DirectedPosition]()
        }
        var newX = newpos.x
        var newY = newpos.y
        if (!(newX == currentX && newY == currentY)) {
          currentX = newX
          currentY = newY
        }
        var dp = DirectedPosition(Position(currentX, currentY), currentFacing)
        if (dp.p.x == active.x && dp.p.y == active.y) {
          path += dp
          f = active.speed + 10
        }
        else {
          path += dp
        }
        oldpos = new DirectedPosition(Position(currentX, currentY), currentFacing);
        f = f + 1
      }
    }
    path = path.reverse
    path(0) = DirectedPosition(path(0).p, active.facing)
    var old2 = DirectedPosition(Position(path(0).p.x, path(0).p.y), Direction.SE)
    for (i <- 1 until path.size) {
      currentX = old2.p.x;
      currentY = old2.p.y;
      var newX = path(i).p.x;
      var newY = path(i).p.y;
      if (newY > currentY) {
        path(i) = DirectedPosition(path(i).p, Direction.SE)
      }
      else if (newY < currentY) {
        path(i) = DirectedPosition(path(i).p, Direction.NW)
      }
      else {
        if (newX < currentX) {
          path(i) = DirectedPosition(path(i).p, Direction.SW)
        }
        else {
          path(i) = DirectedPosition(path(i).p, Direction.NE)
        }
      }
      old2 = new DirectedPosition(Position(path(i).p.x, path(i).p.y), Direction.SE)
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
    path
  }

  def getDijkstraPath(active: Piece, grid: IntMat, placing: PieceMat, targetX: Int, targetY: Int): ArrayBuffer[DirectedPosition] =
  {
    val width = grid.length
    val height = grid(0).length
    var path = new ArrayBuffer[DirectedPosition]
    var currentX = active.x
    var currentY = active.y
    var currentFacing = active.facing
    var newpos = Position(currentX, currentY)
    var rad = Array.ofDim[Float](width + 2, height + 2)
    var choice = -1
    bestMoves = ArrayBuffer(Position(targetX, targetY))
    best = Position(targetX, targetY)
    movesToTargets = new HashMap[Position, Position]
    currentlyFiring = -1
    target = null
    var oldpos = DirectedPosition(Position(best.x, best.y), Direction.SE)
    path += DirectedPosition(Position(best.x, best.y), Direction.SE)
    if (best.x == active.x && best.y == active.y) {

    }
    else {
      var f = 0
      while (f < active.speed) {
        var near = HashMap[Position, Float](oldpos.p -> outward(oldpos.p.x + 1)(oldpos.p.y + 1))
        for (pos <- oldpos.p.Adjacent(width, height)) {
          near += ((pos, outward(pos.x + 1)(pos.y + 1)))
        }
        rad
        var ordered = near.toSeq.sortBy(_._2)
        newpos = ordered.takeWhile(kv => kv._2 == ordered(0)._2).RandomElement.get._1
        if (near.forall(e => e._2 == near(newpos))) {
          return ArrayBuffer[DirectedPosition]()
        }
        var newX = newpos.x
        var newY = newpos.y
        if (!(newX == currentX && newY == currentY)) {
          currentX = newX
          currentY = newY
        }
        var dp = DirectedPosition(Position(currentX, currentY), currentFacing)
        if (dp.p.x == active.x && dp.p.y == active.y) {
          path += dp
          f = active.speed + 10
        }
        else {
          path += dp
        }
        oldpos = new DirectedPosition(Position(currentX, currentY), currentFacing)
        f = f + 1
      }
    }
    path = path.reverse
    path(0) = DirectedPosition(path(0).p, active.facing)
    var old2 = DirectedPosition(Position(path(0).p.x, path(0).p.y), Direction.SE)
    for (i <- 1 until path.size) {
      currentX = old2.p.x
      currentY = old2.p.y
      var newX = path(i).p.x
      var newY = path(i).p.y
      if (newY > currentY) {
        path(i) = DirectedPosition(path(i).p, Direction.SE)
      }
      else if (newY < currentY) {
        path(i) = DirectedPosition(path(i).p, Direction.NW)
      }
      else {
        if (newX < currentX) {
          path(i) = DirectedPosition(path(i).p, Direction.SW)
        }
        else {
          path(i) = DirectedPosition(path(i).p, Direction.NE)
        }
      }
      old2 = new DirectedPosition(Position(path(i).p.x, path(i).p.y), Direction.SE)
    }
    path
  }

  def RetryPlacement()
  {
    failCount = failCount + 1
    println("\n\n!!!!! P L A C E M E N T   F A I L U R E   " + failCount + " !!!!!\n\n");
    if (failCount > 20) {
      println("Too many placement failures.")
      return
    }
    PlacePieces()
    return
  }

  def PlacePieces()
  {
    val allcolors = Array(
      1, 2, 3, 4, 5, 6, 7
    )
    colors = new Array[Int](4)
    reverseColors = new Array[Int](8)
    var taken = Array(false, false, false, false, false, false, false)

    for (i <- 1 until 4) {
      var col = if (i == 1) r.nextIntMin(1, 7) else r.nextInt(7)
      while (taken(col)) {
        col = if (i == 1) r.nextIntMin(1, 7) else r.nextInt(7)
      }
      colors(i) = allcolors(col)
      reverseColors(colors(i)) = i
      taken(col) = true
    }
    colors(0) = 0
    reverseColors(0) = 0

    for (section <- 0 until 2) {
      val rx = (width / 4) + (width / 2) * (section % 2);
      val ry = 3 + (height / 6);
      if (colors(section) == 0) {
        PieceGrid(rx)(ry) = new Piece(Piece.NameLookup("Estate"), colors(section), r.nextInt(4), rx, ry)()
        targetX(1) = rx;
        targetY(1) = ry;
        targetX(2) = rx;
        targetY(2) = ry;
        targetX(3) = rx;
        targetY(3) = ry;
      }
      else {
        PieceGrid(rx)(ry) = new Piece(Piece.NameLookup("Castle"), colors(section), r.nextInt(4), rx, ry)()
        targetX(0) = rx;
        targetY(0) = ry;
      }
      FieldMap.Land(rx)(ry) = 10; // +colors[section];
      for (i <- rx - (width / 6) until rx + (width / 6)) {
        for (j <- ry - (height / 6) until ry + (height / 6)) {
          if (PieceGrid(i)(j) == null) {
            if (r.nextInt(14) <= 2 && (FieldMap.Land(i)(j) == 0 || FieldMap.Land(i)(j) == 1 || FieldMap.Land(i)(j) == 2 || FieldMap.Land(i)(j) == 4 || FieldMap.Land(i)(j) == 8)) {
              PieceGrid(i)(j) = new Piece(r.nextIntMin(24, 28), colors(section), r.nextInt(4), i, j)()
              FieldMap.Land(i)(j) = 10; // +colors[section];
              //processSingleOutlined(facilityps[r.Next(3) % 2], colors[section], dirs[r.Next(4)]);
            }
          }

        }
      }
    }
    for (section <- 2 until 4) {
      val rx = (width / 4) + (width / 2) * (section % 2);
      val ry = height - 3 - (height / 6);
      PieceGrid(rx)(ry) = new Piece(if (colors(section) == 0) Piece.PieceLookup("Estate") else Piece.PieceLookup("Castle"), colors(section), r.nextInt(4), rx, ry)()
      FieldMap.Land(rx)(ry) = 10; // +colors[section];
      for (i <- rx - (width / 8) until rx + (width / 8)) {
        for (j <- ry - (height / 8) until ry + (height / 8)) {
          if (PieceGrid(i)(j) == null) {
            if (r.nextInt(14) <= 2 && (FieldMap.Land(i)(j) == 0 || FieldMap.Land(i)(j) == 1 || FieldMap.Land(i)(j) == 2 || FieldMap.Land(i)(j) == 4 || FieldMap.Land(i)(j) == 8)) {
              PieceGrid(i)(j) = new Piece(r.nextIntMin(24, 28), colors(section), r.nextInt(4), i, j)()
              FieldMap.Land(i)(j) = 10; // +colors[section];
            }
          }
        }
      }
    }

    for (section <- 0 until 4) {
      for (i <- (width / 2) * (section % 2) until (width / 2) + (width / 2) * (section % 2)) {
        for (j <- (if (section / 2 == 0) 0 else height / 2) until (if (section / 2 == 0) height / 2 else height)) {
          if (PieceGrid(i)(j) == null) {
            val currentPiece = Piece.TerrainToPieces(FieldMap.Land(i)(j)).RandomElement().get
            //foot 0-0, treads 1-5, wheels 6-8, flight 9-10
            if (r.nextInt(25) <= 3) {
              //if(Piece.TerrainToMobilities(FieldMap.Land(i)(j)).contains(MovementType.Foot))
              //    PieceGrid(i)(j) = new Piece(Piece.PieceLookup("Infantry_T"), colors(section), section, i, j)()
              //else
              PieceGrid(i)(j) = new Piece(currentPiece, colors(section), section, i, j)()
            }
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
          PieceGrid[rgx, rgy] = new Piece(Piece.TerrainToPieces[FieldMap.Land[rgx, rgy]].RandomElement(), colors[section], section, rgx, rgy);
      }*/
    }
    for (i <- 1 until width - 1) {
      for (j <- 1 until height - 1) {
        if (r.nextInt(30) <= 1 && PieceGrid(i)(j) == null) {
          val rs = 0;
          // r.Next(4);
          val currentPiece = Piece.TerrainToPieces.apply(FieldMap.Land(i)(j)).RandomElement().get
          PieceGrid(i)(j) = new Piece(currentPiece, colors(rs), rs, i, j)()
        }
      }
    }
    val temp = PieceGrid.RandomFactionPiece(colors(ActingFaction))
    ActivePiece = Piece.reset(temp)
    PieceGrid(temp.x)(temp.y) = null
  }

  def dispose() =
  {
  }

  def ShowTargets(u: Piece, w: Weapon) =
  {
    for (i <- 0 until width) {
      for (j <- 0 until height) {
        if (PieceGrid(i)(j) != null && u.isOpposed(PieceGrid(i)(j))
          && Math.abs(u.x - i) + Math.abs(u.y - j) >= w.minRange
          && Math.abs(u.x - i) + Math.abs(u.y - j) <= w.maxRange
          && w.multipliers(Piece.PieceTypeAsNumber(PieceGrid(i)(j).kind)) > 0) {
          val s = Speech(
            i,
            j,
            false,
            (100 - PieceGrid(i)(j).dodge * 10) + "% / " +
              ((w.multipliers(Piece.PieceTypeAsNumber(PieceGrid(i)(j).kind)) - 0.1f * PieceGrid(i)(j).armor) * w.damage).toInt)()
          speaking += s
          FieldMap.Highlight(i)(j) = HighlightType.Bright
        }
        else {
          FieldMap.Highlight(i)(j) = HighlightType.Dim;
        }
      }
    }
  }

  def advanceTurn() =
  {
    PieceGrid(ActivePiece.x)(ActivePiece.y) = Piece.reset(ActivePiece)
    ActingFaction = (ActingFaction + 1) % 4
    var temp = PieceGrid.RandomFactionPiece(colors(ActingFaction))
    ActivePiece = Piece.reset(temp)
    PieceGrid(temp.x)(temp.y) = null
    speaking.clear()
    if (ActingFaction == 1) {
      state = GameState.PC_Select_Move
    }
    else {
      state = GameState.NPC_Play
    }

    CurrentMode = Mode.Selecting
    TaskSteps = 0
  }

  def ProcessStep(): Unit =
  {
    if (state == GameState.PC_Select_Move) {
      ActivePiece.worldX = 20 + ActivePiece.x * 64 + ActivePiece.y * 64
      ActivePiece.worldY = 6 + ActivePiece.x * 32 - ActivePiece.y * 32
      Effects.CenterCamera(ActivePiece.x, ActivePiece.y, 1F);
      outward = dijkstra(ActivePiece, FieldMap.Land, PieceGrid, ActivePiece.x, ActivePiece.y);
      for (i <- 0 until width) {
        for (j <- 0 until height) {
          FieldMap.Highlight(i)(j) = if (outward(i + 1)(j + 1) > 0 && outward(i + 1)(j + 1) <= ActivePiece.speed) HighlightType.Bright else HighlightType.Dim
        }
      }
      FieldMap.Highlight(ActivePiece.x)(ActivePiece.y) = HighlightType.Spectrum;
      return
    }
    if (state == GameState.PC_Select_UI) {
      return
    }
    if (state == GameState.PC_Select_Action) {
      ActivePiece.worldX = 20 + ActivePiece.x * 64 + ActivePiece.y * 64
      ActivePiece.worldY = 6 + ActivePiece.x * 32 - ActivePiece.y * 32
      return
    }
    TaskSteps = TaskSteps + 1
    CurrentMode match {
      case Mode.Selecting =>
        if (TaskSteps > 4 && state != GameState.PC_Play_Move) { // && thr != null && thr.isCompleted)
          BestPath = getDijkstraPath(ActivePiece, FieldMap.Land, PieceGrid)
          FuturePosition = new DirectedPosition(Position(ActivePiece.x, ActivePiece.y), ActivePiece.facing);
          for (i <- 0 until width) {
            for (j <- 0 until height) {
              FieldMap.Highlight(i)(j) = HighlightType.Plain;
            }
          }
          TaskSteps = 0
          CommandersUnite.stateTime = 0
          CurrentMode = Mode.Moving
        }
        else if (TaskSteps <= 1 && state == GameState.NPC_Play) { // (thr == null || thr.isCompleted) &&
          /*
          thr = Future[ArrayBuffer[DirectedPosition]]({
            getDijkstraPath(ActivePiece, FieldMap.Land, PieceGrid)
          })
          thr onSuccess {
            case path: ArrayBuffer[DirectedPosition] => BestPath = path
          }*/
          Effects.CenterCamera(ActivePiece.x, ActivePiece.y, 1F)
          outward = dijkstra(ActivePiece, FieldMap.Land, PieceGrid, ActivePiece.x, ActivePiece.y)
          for (i <- 0 until width) {
            for (j <- 0 until height) {
              FieldMap.Highlight(i)(j) = if (outward(i + 1)(j + 1) > 0 && outward(i + 1)(j + 1) <= ActivePiece.speed) HighlightType.Bright else HighlightType.Dim
            }
          }
          FieldMap.Highlight(ActivePiece.x)(ActivePiece.y) = HighlightType.Spectrum

        }
        else if (state == GameState.PC_Play_Move) {
          BestPath = getDijkstraPath(ActivePiece, FieldMap.Land, PieceGrid, CommandersUnite.cursor.x, CommandersUnite.cursor.y);
          FuturePosition = new DirectedPosition(Position(ActivePiece.x, ActivePiece.y), ActivePiece.facing);
          for (i <- 0 until width) {
            for (j <- 0 until height) {
              FieldMap.Highlight(i)(j) = HighlightType.Plain
            }
          }
          TaskSteps = 0
          CommandersUnite.stateTime = 0
          CurrentMode = Mode.Moving
        }
        else if (state == GameState.PC_Play_Action) {
          target = DirectedPosition.TurnToFace(new Position(ActivePiece.x, ActivePiece.y), new Position(CommandersUnite.cursor.x, CommandersUnite.cursor.y))
          CurrentMode = Mode.Attacking
          state = GameState.PC_Play_Action
          TaskSteps = 0
        }
        else {
        }
        ActivePiece.worldX = 20 + ActivePiece.x * 64 + ActivePiece.y * 64
        ActivePiece.worldY = 6 + ActivePiece.x * 32 - ActivePiece.y * 32
      case Mode.Moving =>
        ActivePiece.x = FuturePosition.p.x
        ActivePiece.y = FuturePosition.p.y
        //ActivePiece.worldX = 20 + ActivePiece.x * 64 + ActivePiece.y * 64
        //ActivePiece.worldY = 6 + ActivePiece.x * 32 - ActivePiece.y * 32

        if ((BestPath.size <= 0 || TaskSteps > ActivePiece.speed + 1)) {
          if(CommandersUnite.smoothActions.size > 0)
            return
          //false == (ActivePiece.weaponry[0].kind == WeaponType.None && ActivePiece.weaponry[1].kind == WeaponType.None)
          /*

              (Position.WithinRange(ActivePiece.x, ActivePiece.y,
          ActivePiece.weaponry[currentlyFiring].minRange, ActivePiece.weaponry[currentlyFiring].minRange, width, height,
          ActivePiece.weaponry[currentlyFiring].maxRange, ActivePiece.weaponry[currentlyFiring].maxRange).Any(
          pos => PieceGrid[pos.x, pos.y] != null && ActivePiece.isOpposed(PieceGrid[pos.x, pos.y])))
           */
          if (state == GameState.PC_Play_Move) {
            ActivePiece.worldX = 20 + ActivePiece.x * 64 + ActivePiece.y * 64
            ActivePiece.worldY = 6 + ActivePiece.x * 32 - ActivePiece.y * 32
            state = GameState.PC_Select_UI
            var entries = new ArrayBuffer[MenuEntry]()
            if (findWeapon(ActivePiece, 0).kind != WeaponType.Non)
              entries += new MenuEntry(findWeapon(ActivePiece, 0).kind.toString, new Runnable
              {
                override def run(): Unit =
                {
                  currentlyFiring = 0
                  ShowTargets(ActivePiece, findWeapon(ActivePiece, 0))
                  CurrentMode = Mode.Selecting;
                  TaskSteps = 0;
                  state = GameState.PC_Select_Action;
                }
              }
              )
            if (findWeapon(ActivePiece, 1).kind != WeaponType.Non)
              entries += new MenuEntry(findWeapon(ActivePiece, 1).kind.toString, new Runnable
              {
                override def run(): Unit =
                {
                  currentlyFiring = 1
                  ShowTargets(ActivePiece, findWeapon(ActivePiece, 1))
                  CurrentMode = Mode.Selecting
                  TaskSteps = 0
                  state = GameState.PC_Select_Action
                }
              })

            UI.postActor(UI.makeMenu(entries, ActivePiece.color)) //, ActivePiece.worldX, ActivePiece.worldY);
            TaskSteps = 0
            CurrentMode = Mode.Selecting
            return
          }
          else if (currentlyFiring > -1 && target != null && PieceGrid(target.p.x)(target.p.y) != null && ActivePiece.isOpposed(PieceGrid(target.p.x)(target.p.y))) {
            //ActivePiece.worldX = 20 + ActivePiece.x * 64 + ActivePiece.y * 64;
            //ActivePiece.worldY = 6 + ActivePiece.x * 32 - ActivePiece.y * 32;
            CurrentMode = Mode.Attacking
            TaskSteps = 0
            return
          }
          advanceTurn()
          return
        }
        FuturePosition = new DirectedPosition(Position(BestPath(0).p.x, BestPath(0).p.y), BestPath(0).dir);
        val oldx = ActivePiece.x
        val oldy = ActivePiece.y

        ActivePiece.facingNumber = ConvertDirection(FuturePosition.dir)
        ActivePiece.facing = FuturePosition.dir
        /*val n = new Timer.Task
        {
          def run()
          {
            ActivePiece.worldX = ActivePiece.worldX + (FuturePosition.p.x - oldx) * 64 / CommandersUnite.smoothMove + (FuturePosition.p.y - oldy) * 64 / CommandersUnite.smoothMove
            ActivePiece.worldY = ActivePiece.worldY + (FuturePosition.p.x - oldx) * 32 / CommandersUnite.smoothMove - (FuturePosition.p.y - oldy) * 32 / CommandersUnite.smoothMove +
              ((LocalMap.Depths(FieldMap.Land(FuturePosition.p.x)(FuturePosition.p.y)) - LocalMap.Depths(FieldMap.Land(oldx)(oldy))) * 3F) / CommandersUnite.smoothMove.toFloat
          }
        }
        Timer.instance.scheduleTask(n, 0, CommandersUnite.updateStep / CommandersUnite.smoothMove.toFloat, CommandersUnite.smoothMove - 1);*/
        val FutureWorld = FuturePosition.p.toWorld()
        val OldWorld = Position(oldx, oldy).toWorld()
        val futureV2 = new Vector2(FutureWorld.x, FutureWorld.y)// + LocalMap.Depths(FieldMap.Land(FuturePosition.p.x)(FuturePosition.p.y)) * 3F)
        val oldV2 = new Vector2(OldWorld.x, OldWorld.y)// + LocalMap.Depths(FieldMap.Land(oldx)(oldy)) * 3F)
        CommandersUnite.smoothActions += SmoothAction((t : Float) =>
          {
            val v = new Vector2(oldV2).lerp(futureV2, t)
            ActivePiece.worldX = v.x
            ActivePiece.worldY = v.y
          }, TimeUtils.millis(), TimeUtils.millis() + CommandersUnite.updateMillis - 20)
          /*
          ActivePiece.worldX += (FuturePosition.p.x - oldx) * 64 / CommandersUnite.smoothMove + (FuturePosition.p.y - oldy) * 64 / CommandersUnite.smoothMove
          ActivePiece.worldY += (FuturePosition.p.x - oldx) * 32 / CommandersUnite.smoothMove - (FuturePosition.p.y - oldy) * 32 / CommandersUnite.smoothMove +
            ((LocalMap.Depths(FieldMap.Land(FuturePosition.p.x)(FuturePosition.p.y)) - LocalMap.Depths(FieldMap.Land(oldx)(oldy))) * 3F) / CommandersUnite.smoothMove.toFloat
        }, 0, CommandersUnite.updateStep / CommandersUnite.smoothMove.toFloat, CommandersUnite.smoothMove - 1)*/

        Effects.CenterCamera(FuturePosition.p, 1F)
        BestPath.remove(0)
      case Mode.Attacking =>
        if (TaskSteps <= 1) {
          ActivePiece.worldX = 20 + ActivePiece.x * 64 + ActivePiece.y * 64
          ActivePiece.worldY = 6 + ActivePiece.x * 32 - ActivePiece.y * 32
          if (target.p.x - ActivePiece.x <= target.p.y - ActivePiece.y && (target.p.x - ActivePiece.x) * -1 <= target.p.y - ActivePiece.y) {
            ActivePiece.facing = Direction.SE
            ActivePiece.facingNumber = 0
            if (PieceGrid(target.p.x)(target.p.y).speed > 0) {
              PieceGrid(target.p.x)(target.p.y).facing = Direction.NW
              PieceGrid(target.p.x)(target.p.y).facingNumber = 2
            }
          }
          else if ((target.p.x - ActivePiece.x) * -1 <= target.p.y - ActivePiece.y && target.p.x - ActivePiece.x >= target.p.y - ActivePiece.y) {
            ActivePiece.facing = Direction.NE
            ActivePiece.facingNumber = 3
            if (PieceGrid(target.p.x)(target.p.y).speed > 0) {
              PieceGrid(target.p.x)(target.p.y).facing = Direction.SW
              PieceGrid(target.p.x)(target.p.y).facingNumber = 1
            }
          }
          else if (target.p.x - ActivePiece.x >= target.p.y - ActivePiece.y && (target.p.x - ActivePiece.x) * -1 >= target.p.y - ActivePiece.y) {
            ActivePiece.facing = Direction.NW
            ActivePiece.facingNumber = 2
            if (PieceGrid(target.p.x)(target.p.y).speed > 0) {
              PieceGrid(target.p.x)(target.p.y).facing = Direction.SE
              PieceGrid(target.p.x)(target.p.y).facingNumber = 0
            }
          }
          else if ((target.p.x - ActivePiece.x) * -1 >= target.p.y - ActivePiece.y && target.p.x - ActivePiece.x <= target.p.y - ActivePiece.y) {
            ActivePiece.facing = Direction.SW;
            ActivePiece.facingNumber = 1;
            if (PieceGrid(target.p.x)(target.p.y).speed > 0) {
              PieceGrid(target.p.x)(target.p.y).facing = Direction.NE;
              PieceGrid(target.p.x)(target.p.y).facingNumber = 3;
            }
          }
          else {
            ActivePiece.facing = Direction.SE;
            ActivePiece.facingNumber = 0;
            if (PieceGrid(target.p.x)(target.p.y).speed > 0) {
              PieceGrid(target.p.x)(target.p.y).facing = Direction.NW;
              PieceGrid(target.p.x)(target.p.y).facingNumber = 2;
            }
          }

          CommandersUnite.attackTime = 0;
          /*currentlyFiring = -1;
          if (ActivePiece.weaponry[1].kind != WeaponType.None && ActivePiece.weaponry[0].kind != WeaponType.None)
              currentlyFiring = r.Next(2);
          else if (ActivePiece.weaponry[0].kind != WeaponType.None)
              currentlyFiring = 0;
          else currentlyFiring = 1;*/
          if (currentlyFiring > -1) {
            hitSuccess = PieceGrid(target.p.x)(target.p.y).attemptDodge(findWeapon(ActivePiece, currentlyFiring));
            if (hitSuccess) {
              previousHP = PieceGrid(target.p.x)(target.p.y).currentHealth;
              killSuccess = PieceGrid(target.p.x)(target.p.y).takeDamage(findWeapon(ActivePiece, currentlyFiring));
            }
          }
          else {
            hitSuccess = false
            killSuccess = false
          }
          ActivePiece.visual = if (findWeapon(ActivePiece, 1).kind == WeaponType.Non &&
            findWeapon(ActivePiece, 0).kind == WeaponType.Non) VisualAction.Normal
                               else VisualAction.Firing;
        }
        else if (TaskSteps > 4 + 1 * (Math.abs(target.p.x - ActivePiece.x) + Math.abs(target.p.y - ActivePiece.y))) {
          currentlyFiring = -1
          if (killSuccess)
            PieceGrid(target.p.x)(target.p.y) = null
          killSuccess = false
          hitSuccess = false
          advanceTurn()
          return
        }
        else if (TaskSteps == 1 + 1 * (Math.abs(target.p.x - ActivePiece.x) + Math.abs(target.p.y - ActivePiece.y)) && currentlyFiring > -1) {
          currentlyFiring match {
            case 0 =>
              if (hitSuccess || Piece.WeaponDisplays(ActivePiece.unitIndex)._1 == 1 || Piece.WeaponDisplays(ActivePiece.unitIndex)._1 == 7) {
                CommandersUnite.receiveTime = 0
                ActivePiece.targeting = ArrayBuffer[DirectedPosition](new DirectedPosition(target.p, target.dir))
              }

            case 1 =>
              if (hitSuccess || Piece.WeaponDisplays(ActivePiece.unitIndex)._2 == 1 || Piece.WeaponDisplays(ActivePiece.unitIndex)._2 == 7) {
                CommandersUnite.receiveTime = 0
                ActivePiece.targeting = ArrayBuffer[DirectedPosition](new DirectedPosition(target.p, target.dir))
              }
          }
          if (!hitSuccess && PieceGrid(target.p.x)(target.p.y).speed > 0) {
            //se 0 -> sw -x -y
            //sw 1 -> nw -x +y
            //nw 2 -> ne +x +y
            //ne 3 -> se +x -y
            /*val avoid = new Timer.Task
            {
              def run()
              {
                PieceGrid(target.p.x)(target.p.y).worldX = PieceGrid(target.p.x)(target.p.y).worldX + (if
                                                                                                       (PieceGrid(target.p.x)(target.p.y).facingNumber % 4 >= 2) 32 / CommandersUnite.smoothMove.toFloat
                                                                                                       else -32 / CommandersUnite.smoothMove.toFloat)
                PieceGrid(target.p.x)(target.p.y).worldY = PieceGrid(target.p.x)(target.p.y).worldY + (if ((PieceGrid(target.p.x)(target.p.y).facingNumber + 1) % 4 >= 2) 16 / CommandersUnite.smoothMove.toFloat
                                                                                                       else -16 / CommandersUnite.smoothMove.toFloat)
              }
            }
            Timer.instance.scheduleTask(avoid, 0, CommandersUnite.updateStep / CommandersUnite.smoothMove.toFloat, CommandersUnite.smoothMove * 5 / 8)*/
            CommandersUnite.repeatedActions += RepeatedAction(() => {
              PieceGrid(target.p.x)(target.p.y).worldX = PieceGrid(target.p.x)(target.p.y).worldX + (if
                                                                                                     (PieceGrid(target.p.x)(target.p.y).facingNumber % 4 >= 2) 32 / CommandersUnite.smoothMove.toFloat
                                                                                                     else -32 / CommandersUnite.smoothMove.toFloat)
              PieceGrid(target.p.x)(target.p.y).worldY = PieceGrid(target.p.x)(target.p.y).worldY + (if ((PieceGrid(target.p.x)(target.p.y).facingNumber + 1) % 4 >= 2) 16 / CommandersUnite.smoothMove.toFloat
                                                                                                     else -16 / CommandersUnite.smoothMove.toFloat)
            }, 0, CommandersUnite.updateStep / CommandersUnite.smoothMove.toFloat, CommandersUnite.smoothMove * 5 / 8 + 1)
/*            val calm = new Timer.Task
            {
              def run()
              {
                PieceGrid(target.p.x)(target.p.y).worldX = PieceGrid(target.p.x)(target.p.y).worldX - (if (PieceGrid(target.p.x)(target.p.y).facingNumber % 4 >= 2) 32 / CommandersUnite.smoothMove.toFloat
                                                                                                       else -32 / CommandersUnite.smoothMove.toFloat)
                PieceGrid(target.p.x)(target.p.y).worldY = PieceGrid(target.p.x)(target.p.y).worldY - (if ((PieceGrid(target.p.x)(target.p.y).facingNumber + 1) % 4 >= 2) 16 / CommandersUnite.smoothMove.toFloat
                                                                                                       else -16 / CommandersUnite.smoothMove.toFloat)
              }
            }
            Timer.instance.scheduleTask(calm, CommandersUnite.updateStep, CommandersUnite.updateStep / CommandersUnite.smoothMove.toFloat * 2, CommandersUnite.smoothMove * 5 / 8);*/
            CommandersUnite.repeatedActions += RepeatedAction(() => {
              PieceGrid(target.p.x)(target.p.y).worldX = PieceGrid(target.p.x)(target.p.y).worldX - (if (PieceGrid(target.p.x)(target.p.y).facingNumber % 4 >= 2) 32 / CommandersUnite.smoothMove.toFloat
                                                                                                     else -32 / CommandersUnite.smoothMove.toFloat)
              PieceGrid(target.p.x)(target.p.y).worldY = PieceGrid(target.p.x)(target.p.y).worldY - (if ((PieceGrid(target.p.x)(target.p.y).facingNumber + 1) % 4 >= 2) 16 / CommandersUnite.smoothMove.toFloat
                                                                                                     else -16 / CommandersUnite.smoothMove.toFloat)
            }, CommandersUnite.updateStep, CommandersUnite.updateStep / CommandersUnite.smoothMove.toFloat * 2, CommandersUnite.smoothMove * 5 / 8 + 1)
            /*  val reset = new Timer.Task
            {
              def run()
              {
                PieceGrid(target.p.x)(target.p.y).worldX = 20 + PieceGrid(target.p.x)(target.p.y).x * 64 + PieceGrid(target.p.x)(target.p.y).y * 64
                PieceGrid(target.p.x)(target.p.y).worldY = 6 + PieceGrid(target.p.x)(target.p.y).x * 32 - PieceGrid(target.p.x)(target.p.y).y * 32
              }
            }
            Timer.instance.scheduleTask(reset, CommandersUnite.updateStep * 19 / CommandersUnite.smoothMove.toFloat * 2)*/
            CommandersUnite.repeatedActions += RepeatedAction(() => {
              PieceGrid(target.p.x)(target.p.y).worldX = 20 + PieceGrid(target.p.x)(target.p.y).x * 64 + PieceGrid(target.p.x)(target.p.y).y * 64
              PieceGrid(target.p.x)(target.p.y).worldY = 6 + PieceGrid(target.p.x)(target.p.y).x * 32 - PieceGrid(target.p.x)(target.p.y).y * 32
            }, CommandersUnite.updateStep * 19 / CommandersUnite.smoothMove.toFloat * 2, 0, 0)


            }
        }
        else if (TaskSteps == 2 + 1 * (Math.abs(target.p.x - ActivePiece.x) + Math.abs(target.p.y - ActivePiece.y)) && currentlyFiring > -1) {
          if (killSuccess) {
            CommandersUnite.explodeTime = 0
            PieceGrid(target.p.x)(target.p.y).visual = VisualAction.Exploding
            speaking += Speech(
              target.p.x, target.p.y, large=true, "DEAD"
            )()
          }
          else if (hitSuccess) {
            speaking += Speech(target.p.x, target.p.y, large = true, "" + (previousHP - PieceGrid(target.p.x)(target.p.y).currentHealth))()
          }

        }
    }
  }
}

}
