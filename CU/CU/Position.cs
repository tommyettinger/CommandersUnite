using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace CU
{
    public class Position : IEquatable<Position>, ICloneable
    {
        public int x { get; set; }
        public int y { get; set; }
        public Position()
        {
            x = 0;
            y = 0;
        }
        public Position(int x, int y)
        {
            this.x = x;
            this.y = y;
        }
        public List<Position> Adjacent(int width, int height)
        {
            List<Position> l = new List<Position>();
            if (x > 0)
                l.Add(new Position(x - 1, y));
            if (y > 0)
                l.Add(new Position(x, y - 1));
            if (x < width - 1)
                l.Add(new Position(x + 1, y));
            if (y < height - 1)
                l.Add(new Position(x, y + 1));
            return l;
        }
        public static List<Position> Adjacent(int x, int y, int width, int height)
        {
            List<Position> l = new List<Position>();
            if (x > 0)
                l.Add(new Position(x - 1, y));
            if (y > 0)
                l.Add(new Position(x, y - 1));
            if (x < width - 1)
                l.Add(new Position(x + 1, y));
            if (y < height - 1)
                l.Add(new Position(x, y + 1));
            return l;
        }
        public List<Position> Nearby(int width, int height, int radius)
        {
            List<Position> l = new List<Position>();
            for (int i = (x - radius >= 0) ? x - radius : 0; i <= x + radius && i < width; i++)
            {
                for (int j = (y - radius >= 0) ? y - radius : 0; j <= y + radius && j < height; j++)
                {
                    if (Math.Abs(i - x) + Math.Abs(j - y) > radius || (x == i && y == j))
                        continue;
                    l.Add(new Position(i, j));
                }
            }
            return l;
        }
        public static List<Position> Nearby(int x, int y, int width, int height, int radius)
        {
            List<Position> l = new List<Position>();
            for (int i = (x - radius >= 0) ? x - radius : 0; i <= x + radius && i < width; i++)
            {
                for (int j = (y - radius >= 0) ? y - radius : 0; j <= y + radius && j < height; j++)
                {
                    if (Math.Abs(i - x) + Math.Abs(j - y) > radius || (x == i && y == j))
                        continue;
                    l.Add(new Position(i, j));
                }
            }
            return l;
        }
        public List<Position> WithinRange(int width, int height, int min, int max)
        {
            List<Position> l = new List<Position>();
            for (int i = (x - max >= 0) ? x - max : 0; i <= x + max && i < width; i++)
            {
                for (int j = (y - max >= 0) ? y - max : 0; j <= y + max && j < height; j++)
                {
                    if (Math.Abs(i - x) + Math.Abs(j - y) < min || Math.Abs(i - x) + Math.Abs(j - y) > max || (x == i && y == j))
                        continue;
                    l.Add(new Position(i, j));
                }
            }
            return l;
        }
        public static List<Position> WithinRange(int x, int y, int lowerX, int lowerY, int width, int height, int min, int max)
        {
            List<Position> l = new List<Position>();
            for (int i = (x - max >= lowerX) ? x - max : lowerX; i <= x + max && i < width; i++)
            {
                for (int j = (y - max >= lowerY) ? y - max : lowerY; j <= y + max && j < height; j++)
                {
                    if (Math.Abs(i - x) + Math.Abs(j - y) < min || Math.Abs(i - x) + Math.Abs(j - y) > max || (x == i && y == j))
                        continue;
                    l.Add(new Position(i, j));
                }
            }
            return l;
        }

        public bool ValidatePosition(int Width, int Height)
        {
            if (x < 0 || x >= Width || y < 0 || y >= Height)
                return false;
            return true;
        }

        public override bool Equals(object obj)
        {
            return this.Equals(obj as Position);
        }

        public bool Equals(Position p)
        {
            // If parameter is null, return false. 
            if (Object.ReferenceEquals(p, null))
            {
                return false;
            }

            // Optimization for a common success case. 
            if (Object.ReferenceEquals(this, p))
            {
                return true;
            }

            // If run-time types are not exactly the same, return false. 
            if (this.GetType() != p.GetType())
                return false;

            // Return true if the fields match. 
            // Note that the base class is not invoked because it is 
            // System.Object, which defines Equals as reference equality. 
            return (x == p.x) && (y == p.y);
        }

        public override int GetHashCode()
        {
            return x * 0x00010000 + y;
        }

        public static bool operator ==(Position lhs, Position rhs)
        {
            // Check for null on left side. 
            if (Object.ReferenceEquals(lhs, null))
            {
                if (Object.ReferenceEquals(rhs, null))
                {
                    // null == null = true. 
                    return true;
                }

                // Only the left side is null. 
                return false;
            }
            // Equals handles case of null on right side. 
            return lhs.Equals(rhs);
        }

        public static bool operator !=(Position lhs, Position rhs)
        {
            return !(lhs == rhs);
        }
        public Object Clone()
        {
            return new Position(x, y);
        }
    }
}
