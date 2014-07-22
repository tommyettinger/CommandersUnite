package cu.game;

import com.badlogic.gdx.utils.Array;

/**
 * Created by Tommy Ettinger on 7/21/2014.
 */
public class DirectedPosition extends Position
{
    public Direction dir;
    public DirectedPosition()
    {
        x = 0;
        y = 0;
        dir = Direction.SE;
    }
    public DirectedPosition(int x, int y)
    {
        this.x = x;
        this.y = y;
        dir = Direction.SE;
    }
    public DirectedPosition(int x, int y, Direction dir)
    {
        this.x = x;
        this.y = y;
        this.dir = dir;
    }
    public static Array<DirectedPosition> AdjacentDirected(int x, int y, int width, int height)
    {
        Array<DirectedPosition> l = new Array<DirectedPosition>();
        if (x > 0)
            l.add(new DirectedPosition(x - 1, y, Direction.NE));
        if (y > 0)
            l.add(new DirectedPosition(x, y - 1, Direction.SE));
        if (x < width - 1)
            l.add(new DirectedPosition(x + 1, y, Direction.SW));
        if (y < height - 1)
            l.add(new DirectedPosition(x, y + 1, Direction.NW));
        return l;
    }
    public static Array<DirectedPosition> WithinRangeDirected(int x, int y, int lowerX, int lowerY, int width, int height, int min, int max)
{
    Array<DirectedPosition> l = new Array<DirectedPosition>();
    for (int i = (x - max >= lowerX) ? x - max : lowerX; i <= x + max && i < width; i++)
    {
        for (int j = (y - max >= lowerY) ? y - max : lowerY; j <= y + max && j < height; j++)
        {
            if (Math.abs(i - x) + Math.abs(j - y) < min || Math.abs(i - x) + Math.abs(j - y) > max || (x == i && y == j))
                continue;
            else if (i - x <= j - y && (i - x) * -1 <= j - y)
            {
                l.add(new DirectedPosition(i, j, Direction.NW));
            }
            else if ((i - x) * -1 <= j - y && i - x >= j - y)
            {
                l.add(new DirectedPosition(i, j, Direction.SW));
            }
            else if (i - x >= j - y && (i - x) * -1 >= j - y)
            {
                l.add(new DirectedPosition(i, j, Direction.SE));
            }
            else if ((i - x) * -1 >= j - y && i - x <= j - y)
            {
                l.add(new DirectedPosition(i, j, Direction.NE));
            }
            else
                l.add(new DirectedPosition(i, j));
        }
    }
    return l;
}
    public static DirectedPosition TurnToFace(Position targetToFace, Position turner)
    {
        DirectedPosition dp = null;
        int startx = targetToFace.x, starty = targetToFace.y, endx = turner.x, endy = turner.y;
        if (endx - startx <= endy - starty && (endx - startx) * -1 <= endy - starty)
        {
            dp=(new DirectedPosition(endx, endy, Direction.NW));
        }
        else if ((endx - startx) * -1 <= endy - starty && endx - startx >= endy - starty)
        {
            dp=(new DirectedPosition(endx, endy, Direction.SW));
        }
        else if (endx - startx >= endy - starty && (endx - startx) * -1 >= endy - starty)
        {
            dp=(new DirectedPosition(endx, endy, Direction.SE));
        }
        else if ((endx - startx) * -1 >= endy - starty && endx - startx <= endy - starty)
        {
            dp=(new DirectedPosition(endx, endy, Direction.NE));
        }
        else
            dp=(new DirectedPosition(endx, endy));

        return dp;
    }

    public boolean equals(Object obj)
{
    return this.equals((DirectedPosition)(obj));
}

    public boolean equals(DirectedPosition p)
    {
        // If parameter is null, return false.
if(p == null)
{
            return false;
        }

        // Optimization for a common success case.
        if (this == p)
        {
            return true;
        }

        // If run-time types are not exactly the same, return false.
        if (this.getClass() != p.getClass())
            return false;

        // Return true if the fields match.
        // Note that the base class is not invoked because it is
        // System.Object, which defines Equals as reference equality.
        return (x == p.x) && (y == p.y) && (dir == p.dir);
    }

    public int hashCode()
{
    return x * 0x00100000 + y * 0x100 + Logic.ConvertDirection(dir);
}
}
