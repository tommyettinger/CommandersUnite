package commanders.unite

import commanders.unite.Direction.Direction

import scala.collection.mutable._

/**
 * Created by Tommy Ettinger on 7/22/2014.
 */
case class Position(x: Int, y: Int)
{
  def toWorld() : Position =
  {
    Position(20 + x * 64 + y * 64, 6+ x * 32 - y * 32)
  }
  def add(p2 : Position) : Position =
  {
    Position(x + p2.x, y + p2.y)
  }
  def Adjacent(width: Int, height: Int): MutableList[Position] =
  {
    var l: MutableList[Position] = new MutableList[Position]();
    if (x > 0) {
      l += new Position(x - 1, y)
    }
    if (y > 0) {
      l += new Position(x, y - 1)
    }
    if (x < width - 1) {
      l += new Position(x + 1, y)
    }
    if (y < height - 1) {
      l += new Position(x, y + 1)
    }
    l
  }

  def Nearby(width: Int, height: Int, radius: Int): MutableList[Position] =
  {
    var l: MutableList[Position] = new MutableList[Position]();
    for (i <- (if (x - radius >= 0) x - radius else 0) to (if (x + radius < width - 1) x + radius else width - 1)) {
      for (j <- (if (y - radius >= 0) y - radius else 0) to (if (y + radius < height - 1) y + radius else height - 1)) {
        if (!(Math.abs(i - x) + Math.abs(j - y) > radius || (x == i && y == j))) {
          l += Position(i, j)
        }
      }
    }
    l
  }

  def WithinRange(lowerX: Int, lowerY: Int, width: Int, height: Int, min: Int, max: Int): MutableList[Position] =
  {
    var l: MutableList[Position] = new MutableList[Position]()
    for (i <- (if (x - max >= lowerX) x - max else lowerX) to x + max if i < width) {
      for (j <- (if (y - max >= lowerY) y - max else lowerY) to y + max if j < width) {
        if (!(Math.abs(i - x) + Math.abs(j - y) < min || Math.abs(i - x) + Math.abs(j - y) > max || (x == i && y == j))) {
          l += (new Position(i, j));
        }
      }
    }
    l
  }
}

object Position
{
  def Adjacent(x: Int, y: Int, width: Int, height: Int): MutableList[Position] =
  {
    var l: MutableList[Position] = new MutableList[Position]();
    if (x > 0) {
      l += new Position(x - 1, y)
    }
    if (y > 0) {
      l += new Position(x, y - 1)
    }
    if (x < width - 1) {
      l += new Position(x + 1, y)
    }
    if (y < height - 1) {
      l += new Position(x, y + 1)
    }
    l
  }

  def Nearby(x: Int, y: Int, width: Int, height: Int, radius: Int): MutableList[Position] =
  {
    var l: MutableList[Position] = new MutableList[Position]();
    for (i <- (if (x - radius >= 0) x - radius else 0) to (if (x + radius < width - 1) x + radius else width - 1)) {
      for (j <- (if (y - radius >= 0) y - radius else 0) to (if (y + radius < height - 1) y + radius else height - 1)) {
        if (!(Math.abs(i - x) + Math.abs(j - y) > radius || (x == i && y == j))) {
          l += Position(i, j)
        }
      }
    }
    l
  }

  def WithinRange(x: Int, y: Int, lowerX: Int, lowerY: Int, width: Int, height: Int, min: Int, max: Int): MutableList[Position] =
  {
    var l: MutableList[Position] = new MutableList[Position]()
    for (i <- (if (x - max >= lowerX) x - max else lowerX) to x + max if i < width) {
      for (j <- (if (y - max >= lowerY) y - max else lowerY) to y + max if j < width) {
        if (!(Math.abs(i - x) + Math.abs(j - y) < min || Math.abs(i - x) + Math.abs(j - y) > max || (x == i && y == j))) {
          l += (new Position(i, j));
        }
      }
    }
    l
  }
}

case class DirectedPosition(p: Position, dir: Direction)
{

  def Adjacent(width: Int, height: Int): MutableList[DirectedPosition] =
  {
    var l: MutableList[DirectedPosition] = new MutableList[DirectedPosition]();
    if (p.x > 0) {
      l += new DirectedPosition(Position(p.x - 1, p.y), Direction.NE)
    }
    if (p.y > 0) {
      l += new DirectedPosition(Position(p.x, p.y - 1), Direction.SE)
    }
    if (p.x < width - 1) {
      l += new DirectedPosition(Position(p.x + 1, p.y), Direction.SW)
    }
    if (p.y < height - 1) {
      l += new DirectedPosition(Position(p.x, p.y + 1), Direction.NW)
    }
    l
  }

  def WithinRange(lowerX: Int, lowerY: Int, width: Int, height: Int, min: Int, max: Int): MutableList[DirectedPosition] =
  {
    var l: MutableList[DirectedPosition] = new MutableList[DirectedPosition]()
    for (i <- (if (p.x - max >= lowerX) p.x - max else lowerX) to p.x + max if i < width) {
      for (j <- (if (p.y - max >= lowerY) p.y - max else lowerY) to p.y + max if j < width) {
        if (!(Math.abs(i - p.x) + Math.abs(j - p.y) < min || Math.abs(i - p.x) + Math.abs(j - p.y) > max || (p.x == i && p.y == j))) {
          if (i - p.x <= j - p.y && (i - p.x) * -1 <= j - p.y) {
            l += (new DirectedPosition(Position(i, j), Direction.NW));
          }
          else if ((i - p.x) * -1 <= j - p.y && i - p.x >= j - p.y) {
            l += (new DirectedPosition(Position(i, j), Direction.SW));
          }
          else if (i - p.x >= j - p.y && (i - p.x) * -1 >= j - p.y) {
            l += (new DirectedPosition(Position(i, j), Direction.SE));
          }
          else if ((i - p.x) * -1 >= j - p.y && i - p.x <= j - p.y) {
            l += (new DirectedPosition(Position(i, j), Direction.NE));
          }
          else {
            l += (new DirectedPosition(Position(i, j), Direction.SE))
          };
        }
      }
    }
    l
  }

}

object DirectedPosition
{
  def Adjacent(x: Int, y: Int, width: Int, height: Int): MutableList[DirectedPosition] =
  {
    var l: MutableList[DirectedPosition] = new MutableList[DirectedPosition]();
    if (x > 0) {
      l += new DirectedPosition(Position(x - 1, y), Direction.NE)
    }
    if (y > 0) {
      l += new DirectedPosition(Position(x, y - 1), Direction.SE)
    }
    if (x < width - 1) {
      l += new DirectedPosition(Position(x + 1, y), Direction.SW)
    }
    if (y < height - 1) {
      l += new DirectedPosition(Position(x, y + 1), Direction.NW)
    }
    l
  }

  def WithinRange(x: Int, y: Int, lowerX: Int, lowerY: Int, width: Int, height: Int, min: Int, max: Int): MutableList[DirectedPosition] =
  {
    var l: MutableList[DirectedPosition] = new MutableList[DirectedPosition]()
    for (i <- (if (x - max >= lowerX) x - max else lowerX) to x + max if i < width) {
      for (j <- (if (y - max >= lowerY) y - max else lowerY) to y + max if j < width) {
        if (!(Math.abs(i - x) + Math.abs(j - y) < min || Math.abs(i - x) + Math.abs(j - y) > max || (x == i && y == j))) {
          if (i - x <= j - y && (i - x) * -1 <= j - y) {
            l += (new DirectedPosition(Position(i, j), Direction.NW));
          }
          else if ((i - x) * -1 <= j - y && i - x >= j - y) {
            l += (new DirectedPosition(Position(i, j), Direction.SW));
          }
          else if (i - x >= j - y && (i - x) * -1 >= j - y) {
            l += (new DirectedPosition(Position(i, j), Direction.SE));
          }
          else if ((i - x) * -1 >= j - y && i - x <= j - y) {
            l += (new DirectedPosition(Position(i, j), Direction.NE));
          }
          else {
            l += (new DirectedPosition(Position(i, j), Direction.SE))
          };
        }
      }
    }
    l
  }

  def TurnToFace(targetToFace: Position, turner: Position): DirectedPosition =
  {
    var dp: DirectedPosition = null
    val startx = targetToFace.x
    val starty = targetToFace.y
    val endx = turner.x
    val endy = turner.y
    if (endx - startx <= endy - starty && (endx - startx) * -1 <= endy - starty) {
      dp = (new DirectedPosition(Position(endx, endy), Direction.NW))
    }
    else if ((endx - startx) * -1 <= endy - starty && endx - startx >= endy - starty) {
      dp = (new DirectedPosition(Position(endx, endy), Direction.SW))
    }
    else if (endx - startx >= endy - starty && (endx - startx) * -1 >= endy - starty) {
      dp = (new DirectedPosition(Position(endx, endy), Direction.SE))
    }
    else if ((endx - startx) * -1 >= endy - starty && endx - startx <= endy - starty) {
      dp = (new DirectedPosition(Position(endx, endy), Direction.NE))
    }
    else {
      dp = (new DirectedPosition(Position(endx, endy), Direction.NW))
    }
    dp
  }
}
