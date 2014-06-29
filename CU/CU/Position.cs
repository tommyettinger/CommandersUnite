using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace CU
{
    public class Position
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
        public List<Position> Nearby(int width, int height, int radius)
        {
            List<Position> l = new List<Position>();
            for (int i = (x - radius >= 0) ? x - radius : 0; i <= x + radius && i < width; i++)
            {
                for (int j = (y - radius >= 0) ? y - radius : 0; j <= y + radius && y < height; j++)
                {
                    if (Math.Abs(i - x) + Math.Abs(j - y) > radius || (x == i && y == j))
                        continue;
                    l.Add(new Position(i,j));
                }
            }
            return l;
        }
    }
}
