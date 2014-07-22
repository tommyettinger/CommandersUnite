package cu.game;
import java.util.ArrayList;

/**
 * Created by Tommy Ettinger on 7/21/2014.
 */
public class Position {
    public int x;
    public int y;

    public Position() {
        x = 0;
        y = 0;
    }

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public ArrayList<Position> Adjacent(int width, int height) {
        ArrayList<Position> l = new ArrayList<Position>();
        if (x > 0)
            l.add(new Position(x - 1, y));
        if (y > 0)
            l.add(new Position(x, y - 1));
        if (x < width - 1)
            l.add(new Position(x + 1, y));
        if (y < height - 1)
            l.add(new Position(x, y + 1));
        return l;
    }

    public static ArrayList<Position> Adjacent(int x, int y, int width, int height) {
        ArrayList<Position> l = new ArrayList<Position>();
        if (x > 0)
            l.add(new Position(x - 1, y));
        if (y > 0)
            l.add(new Position(x, y - 1));
        if (x < width - 1)
            l.add(new Position(x + 1, y));
        if (y < height - 1)
            l.add(new Position(x, y + 1));
        return l;
    }

    public ArrayList<Position> Nearby(int width, int height, int radius) {
        ArrayList<Position> l = new ArrayList<Position>();
        for (int i = (x - radius >= 0) ? x - radius : 0; i <= x + radius && i < width; i++) {
            for (int j = (y - radius >= 0) ? y - radius : 0; j <= y + radius && j < height; j++) {
                if (Math.abs(i - x) + Math.abs(j - y) > radius || (x == i && y == j))
                    continue;
                l.add(new Position(i, j));
            }
        }
        return l;
    }

    public static ArrayList<Position> Nearby(int x, int y, int width, int height, int radius) {
        ArrayList<Position> l = new ArrayList<Position>();
        for (int i = (x - radius >= 0) ? x - radius : 0; i <= x + radius && i < width; i++) {
            for (int j = (y - radius >= 0) ? y - radius : 0; j <= y + radius && j < height; j++) {
                if (Math.abs(i - x) + Math.abs(j - y) > radius || (x == i && y == j))
                    continue;
                l.add(new Position(i, j));
            }
        }
        return l;
    }

    public ArrayList<Position> WithinRange(int width, int height, int min, int max) {
        ArrayList<Position> l = new ArrayList<Position>();
        for (int i = (x - max >= 0) ? x - max : 0; i <= x + max && i < width; i++) {
            for (int j = (y - max >= 0) ? y - max : 0; j <= y + max && j < height; j++) {
                if (Math.abs(i - x) + Math.abs(j - y) < min || Math.abs(i - x) + Math.abs(j - y) > max || (x == i && y == j))
                    continue;
                l.add(new Position(i, j));
            }
        }
        return l;
    }

    public static ArrayList<Position> WithinRange(int x, int y, int lowerX, int lowerY, int width, int height, int min, int max) {
        ArrayList<Position> l = new ArrayList<Position>();
        for (int i = (x - max >= lowerX) ? x - max : lowerX; i <= x + max && i < width; i++) {
            for (int j = (y - max >= lowerY) ? y - max : lowerY; j <= y + max && j < height; j++) {
                if (Math.abs(i - x) + Math.abs(j - y) < min || Math.abs(i - x) + Math.abs(j - y) > max || (x == i && y == j))
                    continue;
                l.add(new Position(i, j));
            }
        }
        return l;
    }

    public boolean ValidatePosition(int Width, int Height) {
        if (x < 0 || x >= Width || y < 0 || y >= Height)
            return false;
        return true;
    }

    public boolean equals(Object obj) {
        return this.equals((Position) obj);
    }

    public boolean equals(Position p) {
        // If parameter is null, return false. 
        if (p == null) {
            return false;
        }
        if (this == p) {
            return true;
        }
        if (this.getClass() != p.getClass())
            return false;
        // Return true if the fields match. 
        // Note that the base class is not invoked because it is 
        // System.Object, which defines Equals as reference equality. 
        return (x == p.x) && (y == p.y);
    }

    public int hashCode() {
        return x * 0x00010000 + y;
    }
}
