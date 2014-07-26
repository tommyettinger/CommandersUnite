package commanders.unite

import commanders.unite.HighlightType.HighlightType
import commanders.unite.Extensions._
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/**
 * Created by Tommy Ettinger on 7/25/2014.
 */
object LocalMap{
  var Terrains = Array(
    "Plains","Forest","Desert","Jungle","Hills"
    ,"Mountains","Ruins","Tundra","Road","River",
    "Basement")
  val Depths = Array(
    3, 3, 3, 3, 7,
    7, 5, 3, 5, 1,
    5)
}
class LocalMap(width : Int, height : Int)
{
  var Land = Array.fill[Int](width, height)(0)
  var Highlight = Array.fill[HighlightType](width, height)(HighlightType.Plain)
  var takenLocations = Array.fill[Int](width, height)(0)
  val r = new Random()
for (i <- 0 until width)
    {
      for (j  <- 0 until height)
      {
        takenLocations(i)(j) = 0
        Land(i)(j) = 0
        Highlight(i)(j) = HighlightType.Plain
      }
    }
    var rivers = MakeSoftPath(RandomPosition(), RandomPosition())
    for (t <- rivers)
    {
      Land(t.x)(t.y) = 9
      takenLocations(t.x)(t.y) = 2
    }
    var numMountains = r.nextIntMin((width * 0.25).toInt, width.toInt)
    MakeMountains(numMountains)
    var roads = MakeHardPath(RandomPosition(), RandomPosition())
    for (t <- roads)
    {
      Land(t.x)(t.y) = 8
      takenLocations(t.x)(t.y) = 4
    }
    roads = MakeHardPath(RandomPosition(), RandomPosition())
  for (t <- roads)
  {
    Land(t.x)(t.y) = 8
    takenLocations(t.x)(t.y) = 4
  }
    roads = MakeHardPath(RandomPosition(), RandomPosition())
  for (t <- roads)
  {
    Land(t.x)(t.y) = 8
    takenLocations(t.x)(t.y) = 4
  }

    var extreme = 0
    r.nextInt(5) match
    {
      case 0=>extreme = 7
      case 1=> extreme = 2
      case 2=> extreme = 2
      case 3=> extreme = 0
      case 4=> extreme = 0
    }
    for (i <- 1 until width - 1)
    {
      for (j <- 1 until height - 1)
      {
        for (v <- 0 until 3)
        {

          var near = Position(i, j).Nearby(width, height, 2);
          var adj = new ArrayBuffer[Int](12)
          for(p <- near)
          {
            adj+=Land(p.x)(p.y)
          }
          var likeliest = 0
          if (!adj.contains(1) && extreme == 2 && r.nextInt(5) > 1)
            likeliest = extreme
          if (adj.contains(2) && r.nextInt(4) == 0)
            likeliest = extreme
          if (extreme == 7 && (r.nextInt(4) == 0) || (adj.contains(7) && r.nextInt(3) > 0))
            likeliest = extreme
          if ((adj.contains(1) && r.nextInt(5) > 2) || r.nextInt(7) == 0)
            likeliest = r.nextInt(2) * 2 + 1
          if (adj.contains(5) && r.nextInt(3) == 0)
            likeliest = r.nextIntMin(4, 6);
          if (r.nextInt(45) == 0)
            likeliest = 6;
          if (takenLocations(i)(j) == 0)
          {
            Land(i)(j) = likeliest;
          }
        }
      }
    }
    

  def RandomPosition():Position=
  {
    Position(r.nextIntMin(0, width - 1), r.nextIntMin(0, height - 1))
  }
  def Bresenham(start:Position, end:Position):ArrayBuffer[Position]=
  {
    var path = new ArrayBuffer[Position]()
    var d = 0
    var x1 = start.x
    var x2 = end.x
    var y1 = start.y
    var y2 = end.y

    var dy = Math.abs(y2 - y1);
    var dx = Math.abs(x2 - x1);

    var dy2 = dy << 1 // slope scaling factors to avoid floating
    var dx2 = dx << 1 // point

    var ix = if(x1 < x2) 1 else -1 // increment direction
    var iy = if(y1 < y2) 1 else -1

    if (dy <= dx)
    {
      var keepGoing = true
      while(keepGoing) {
        path += Position(x1, y1)
        if (x1 == x2)
          keepGoing = false
        else {
          x1 += ix
          d += dy2
          if (d > dx) {
            path += Position(x1, y1)
            y1 += iy
            d -= dx2
          }
        }
      }
    }
    else {

      var keepGoing = true
      while (keepGoing) {
        path += Position(x1, y1)
        if (y1 == y2)
          keepGoing = false
        else {
          y1 += iy
          d += dx2
          if (d > dy) {
            path += Position(x1, y1)
            x1 += ix
            d -= dy2
          }
        }
      }
    }
    path
  }
  def ValidatePosition(p:Position):Boolean=
  {
    if (p.x < 0 || p.x >= width || p.y < 0 || p.y >= height) {
      false
    }
    else {
      true
    }
  }
  def CorrectPosition(p:Position):Position=
  {
    var x = p.x
    var y = p.y
    if (p.x < 0)
      x = 0
    if (p.x >= width)
      x = width - 1
    if (p.y < 0)
      y = 0
    if (p.y >= height)
      y = height - 1
    Position(x,y)
  }
  def MakeSoftPath(start:Position, end:Position):ArrayBuffer[Position]=
  {
    var path = Bresenham(start, end)
    var path2 = new ArrayBuffer[Position]()
    if(path.size == 0) return path
    var midpoint = path(path.size / 2)
    var early = path(path.size / 4)
    var late = path(path.size * 3 / 4)
    midpoint = CorrectPosition(Position(midpoint.x + r.nextInt(width / 4) - width / 8, midpoint.y))
    early = CorrectPosition(Position(r.nextInt(width / 3) - width / 6, early.y))
    late = CorrectPosition(Position(r.nextInt(width / 3) - width / 6, late.y))
    path2 ++= Bresenham(start, early)
    path2.remove(path2.size - 1)
    path2 ++= Bresenham(early, midpoint)
    path2.remove(path2.size - 1)
    path2 ++= Bresenham(midpoint, late)
    path2.remove(path2.size - 1)
    path2 ++= Bresenham(late, end)
    path2
  }
  def MakeHardPath(start:Position, end:Position):ArrayBuffer[Position]=
  {
    var path = new ArrayBuffer[Position]()
    if (!ValidatePosition(start) || !ValidatePosition(end))
      return path
    var x = start.x
    var y = start.y
    var dx = Math.abs(end.x - start.x);
    var dy = Math.abs(end.y - start.y);
    path += Position(x, y)
    if (dx > dy)
    {
      if (end.x > start.x)
      {
        while (end.x > x)
        {
          x = x+1
          path += Position(x, y)
        }
      }
      else
      {
        while (end.x < x)
        {
          x = x - 1
          path += Position(x, y)
        }
      }

      if (end.y > start.y)
      {
        while (end.y > y)
        {
          y = y + 1
          path += Position(x, y)
        }
      }
      else
      {
        while (end.y < y)
        {
          y = y - 1
          path += Position(x, y)
        }
      }
    }
    else
    {
      if (end.y > start.y)
      {
        while (end.y > y)
        {
          y = y + 1
          path += Position(x, y)
        }
      }
      else
      {
        while (end.y < y)
        {
          y = y - 1
          path += Position(x, y)
        }
      }

      if (end.x > start.x)
      {
        while (end.x > x)
        {
          x = x + 1
          path +=  Position(x, y)
        }
      }
      else
      {
        while (end.x < x)
        {
          x = x - 1
          path += Position(x, y)
        }
      }
    }
    path
  }
  def MakeMountains(numMountains:Int)
  {
    var iter = 0
    var rx = r.nextIntMin(1, width - 2)
    var ry = r.nextIntMin(1, height - 2);
    while (iter <= numMountains)
    {
      if (takenLocations(rx)(ry) < 1 && r.nextInt(6) > 0) // && ((ry + 1) / 2 != ry)
      {
        takenLocations(rx)(ry) = 2;
        Land(rx)(ry) = r.nextIntMin(4, 6);

        var np = Position(rx, ry);
        np = np.Adjacent(width, height).RandomElement.get
        rx = np.x;
        ry = np.y;
      }
      else
      {
        rx = r.nextIntMin(1, width - 2);
        ry = r.nextIntMin(1, height - 2);
      }
      iter = iter + 1
    }
  }
}
