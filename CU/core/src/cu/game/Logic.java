package cu.game;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.StringBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import static ch.lambdaj.Lambda.*;
public class Logic
{
    public static StringBuilder log = new StringBuilder();
    public Mode CurrentMode;
    public LocalMap FieldMap;
    public Unit[][] UnitGrid;
    public Unit ActiveUnit;
    public int[] Colors;
    public int[] ReverseColors;
    public int ActingFaction;
    public int TaskSteps;
    public int width;
    public int height;
    public ArrayList<Speech> speaking;
    int[] targetX;
    int[] targetY;
    public Logic(int MapWidth, int MapHeight)
    {
        ActingFaction = 1;
        TaskSteps = 0;
        CurrentMode = Mode.Selecting;
        FieldMap = new LocalMap(MapWidth, MapHeight);
        width = FieldMap.Width;
        height = FieldMap.Height;
        UnitGrid = new Unit[width][height];
        outward = new float[width+2][height+2];
        speaking = new ArrayList<Speech>();
        targetX = new int[] { MapWidth / 4, MapWidth / 2, MapWidth / 4, MapWidth / 2, };
        targetY = new int[] { MapHeight / 2, MapHeight / 4, MapHeight / 2, MapHeight / 4 };
    }
    public Logic()
    {
        ActingFaction = 1;
        TaskSteps = 0;
        CurrentMode = Mode.Selecting;
        FieldMap = new LocalMap(25, 25);
        UnitGrid = new Unit[FieldMap.Width][FieldMap.Height];
        width = FieldMap.Width;
        height = FieldMap.Height;
        outward = new float[width + 2][height + 2];
    }
    public static String[] DirectionNames = { "SE", "SW", "NW", "NE" };
    //foot 0-0, treads 1-5, wheels 6-8, flight 9-10


        /*{ "Infantry", //foot 0 0
                               "Tank", "Artillery", "Artillery_P", "Artillery_S", "Supply_P", //treads 1 5
                               "Artillery_T", "Supply", "Supply_T", //wheels 6 8
                               "Helicopter", "Plane", //flight 9 10
                               "City", "Factory", "Castle", "Capital" //facility
                             };*/


    public static Random r = new Random();

    public static int ConvertDirection(Direction dir)
    {
        int facingNumber;
        switch (dir)
        {
            case SE: facingNumber = 0; break;
            case SW: facingNumber = 1; break;
            case NW: facingNumber = 2; break;
            case NE: facingNumber = 3; break;
            default: facingNumber = 0; break;
        }
        return facingNumber;
    }
    float[][] DijkstraAttackPositions(Weapon weapon, MovementType mobility, int selfColor, ArrayList<Integer> targetColors, int[][] grid, Unit[][] placing, float[][] d)
{
    int width = d.length;
    int height = d[0].length;
    float wall = 2222;
    float goal = 0;

    HashMap<Position, Float> open = new HashMap<>(),
            fringe = new HashMap<>(),
            closed = new HashMap<>();

    int[] ability =
            new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
    //plains forest desert jungle hills mountains ruins tundra road river basement
    HashMap<MovementType, Boolean> pass = new HashMap<>();
    pass.put(MovementType.Foot, true);
    pass.put(MovementType.Treads, true);
    pass.put(MovementType.Wheels, true);
    pass.put(MovementType.TreadsAmphi, true);
    pass.put(MovementType.WheelsTraverse, true);
    pass.put(MovementType.Flight, true);
    pass.put(MovementType.FlightFlyby, true);
    pass.put(MovementType.Immobile, false);

    switch (mobility)
    {
        case Foot:
            ability =
                    new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 };
            pass = new HashMap<>();
            pass.put(MovementType.Foot, false);
            pass.put(MovementType.Treads, true);
            pass.put(MovementType.Wheels, false);
            pass.put(MovementType.TreadsAmphi, true);
            pass.put(MovementType.WheelsTraverse, false);
            pass.put(MovementType.Flight, true);
            pass.put(MovementType.FlightFlyby, true);
            pass.put(MovementType.Immobile, false);

            break;
        case Treads:
            ability =
                    new int[] { 1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            pass = new HashMap<>();
            pass.put(MovementType.Foot, false);
            pass.put(MovementType.Treads, false);
            pass.put(MovementType.Wheels, false);
            pass.put(MovementType.TreadsAmphi, false);
            pass.put(MovementType.WheelsTraverse, false);
            pass.put(MovementType.Flight, true);
            pass.put(MovementType.FlightFlyby, true);
            pass.put(MovementType.Immobile, false);

        break;
        case Wheels:
            ability =
                    new int[] { 1, 0, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

            pass = new HashMap<>();
            pass.put(MovementType.Foot, false);
            pass.put(MovementType.Treads, false);
            pass.put(MovementType.Wheels, false);
            pass.put(MovementType.TreadsAmphi, false);
            pass.put(MovementType.WheelsTraverse, false);
            pass.put(MovementType.Flight, true);
            pass.put(MovementType.FlightFlyby, true);
            pass.put(MovementType.Immobile, false);
        break;
        case TreadsAmphi:
            ability =
                    new int[] { 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 };
            pass = new HashMap<>();
            pass.put(MovementType.Foot, false);
            pass.put(MovementType.Treads, false);
            pass.put(MovementType.Wheels, false);
            pass.put(MovementType.TreadsAmphi, false);
            pass.put(MovementType.WheelsTraverse, false);
            pass.put(MovementType.Flight, true);
            pass.put(MovementType.FlightFlyby, true);
            pass.put(MovementType.Immobile, false);

            break;
        case WheelsTraverse:
            ability =
                    new int[] { 1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            pass = new HashMap<>();
            pass.put(MovementType.Foot, false);
            pass.put(MovementType.Treads, false);
            pass.put(MovementType.Wheels, false);
            pass.put(MovementType.TreadsAmphi, false);
            pass.put(MovementType.WheelsTraverse, false);
            pass.put(MovementType.Flight, true);
            pass.put(MovementType.FlightFlyby, true);
            pass.put(MovementType.Immobile, false);
        break;
        case Flight:
            ability =
                    new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 };

            pass = new HashMap<>();
            pass.put(MovementType.Foot, true);
            pass.put(MovementType.Treads, true);
            pass.put(MovementType.Wheels, true);
            pass.put(MovementType.TreadsAmphi, true);
            pass.put(MovementType.WheelsTraverse, true);
            pass.put(MovementType.Flight, false);
            pass.put(MovementType.FlightFlyby, false);
            pass.put(MovementType.Immobile, false);

        break;
        case FlightFlyby:
            ability =
                    new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 };
            pass = new HashMap<>();
            pass.put(MovementType.Foot, true);
            pass.put(MovementType.Treads, true);
            pass.put(MovementType.Wheels, true);
            pass.put(MovementType.TreadsAmphi, true);
            pass.put(MovementType.WheelsTraverse, true);
            pass.put(MovementType.Flight, true);
            pass.put(MovementType.FlightFlyby, true);
            pass.put(MovementType.Immobile, false);

            break;
    }
    movesToTargets.clear();
    for (int i = 1; i < width - 1; i++)
    {
        for (int j = 1; j < height - 1; j++)
        {
            if (targetColors.(c -> placing[i - 1][j - 1] != null && c == placing[i - 1][j - 1].color))
            {
                if (weapon.multipliers[Unit.UnitTypeAsNumber(placing[i - 1][j - 1].kind)] > 0) {
                    Position tgt = new Position(i - 1, j - 1);
                    for (Position p : Position.WithinRange(i, j, 1, 1, width - 1, height - 1, weapon.minRange, weapon.maxRange)) {
                        if (ability[grid[p.x - 1][p.y - 1]] == 1 && placing[p.x - 1][p.y - 1] == null) {
                            d[p.x][p.y] = goal;
                            open.put(p, goal);
                            movesToTargets.put(new Position(p.x - 1, p.y - 1), tgt);
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
            }
            else if (d[i][j] >= wall)
            {
                closed.put(new Position(i, j), wall);
            }
        }
    }


    while (open.size() > 0)
    {
        for (HashMap.Entry<Position, Float> idx_dijk : open.entrySet())
        {
            Position k = idx_dijk.getKey();
            float v = idx_dijk.getValue();
            ArrayList<Position> moves = k.Adjacent(width, height);
            for (Position mov : moves)
            if (open.containsKey(mov) ||
                    closed.containsKey(mov) ||
                    d[mov.x][mov.y] >= wall ||
                d[mov.x][mov.y] <= v + 1)
            {

            }
            else if (
                ability[grid[mov.x - 1][mov.y - 1]] == 1
                && placing[mov.x - 1][mov.y - 1] == null)
            {
                fringe.put(mov, v + 1);
                d[mov.x][mov.y] = v + 1;
            }
            else if (
                ability[grid[mov.x - 1][mov.y - 1]] == 1 &&
                (placing[mov.x - 1][mov.y - 1] != null &&
                (pass.get(placing[mov.x - 1][mov.y - 1].mobility) ||
            (placing[mov.x - 1][mov.y - 1].color == selfColor &&
                placing[mov.x - 1][mov.y - 1].mobility != MovementType.Immobile)
            )))
            {
                fringe.put(mov, v + 1);
                d[mov.x][mov.y] = v + 1;
            }
        }
        for (HashMap.Entry<Position, Float> kv : open.entrySet())
        {
            closed.put (kv.getKey(), kv.getValue());
        }
        open.clear();
        for (HashMap.Entry<Position, Float> kv : fringe.entrySet())
        {
            open.put(kv.getKey(), kv.getValue());
        }
        fringe.clear();

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
    public static void writeShowLog(String text)
    {
    }
    ArrayList<Position> bestMoves = new ArrayList<Position>();
    HashMap<Position, Position> movesToTargets = new HashMap<>();
    Position best = null;
    float[][] ViableMoves(Unit self, int currentWeapon, int[][] grid, Unit[][] placing)
{
    writeShowLog("\n" + "* * " + self.name + " * *");
    writeShowLog("Unit is at: " + self.x + ", " + self.y);
    gradient = SmartDijkstra(self, currentWeapon, grid, placing, ((self.color == 0) ? new int[] { 1, 2, 3, 4, 5, 6, 7 } : new int[] { 0 }));
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

    bestMoves = new ArrayList<Position> { new Position(self.x, self.y) };
    Dictionary<Position, float>
            open = new Dictionary<Position, float> { { new Position(self.x + 1, self.y + 1), goal } },
    fringe = new Dictionary<Position, float>(),
            closed = new Dictionary<Position, float>();


    float[][] radiate = new float[width, height];

    for (int i = 1; i < width - 1; i++)
    {
        for (int j = 1; j < height - 1; j++)
            radiate[i, j] = unexplored;
    }

    for (int i = 0; i < width; i++)
    {
        radiate[i, 0] = wall;
        radiate[i, (height - 1)] = wall;

    }
    for (int j = 1; j < height - 1; j++)
    {
        radiate[0, j] = wall;
        radiate[(width - 1), j] = wall;
    }

    for (int i = 0; i < width; i++)
    {
        for (int j = 0; j < height; j++)
        {
            if (radiate[i, j] >= wall)// || gradient[i,j] >= wall)
            {
                closed[new Position(i, j)] = wall;
            }
        }
    }
    radiate[self.x + 1, self.y + 1] = goal;
    int[] ability =
            new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
    //plains forest desert jungle hills mountains ruins tundra road river basement
    Dictionary<MovementType, bool> pass = new Dictionary<MovementType, bool>
    {
        {MovementType.Foot, true},
        {MovementType.Treads, true},
        {MovementType.Wheels, true},
        {MovementType.TreadsAmphi, true},
        {MovementType.WheelsTraverse, true},
        {MovementType.Flight, true},
        {MovementType.FlightFlyby, true},
        {MovementType.Immobile, false},
    };
    switch (self.mobility)
    {
        case MovementType.Foot:
            ability =
                    new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 };
            pass = new Dictionary<MovementType, bool>
        {
            {MovementType.Foot, false},
            {MovementType.Treads, true},
            {MovementType.Wheels, false},
            {MovementType.TreadsAmphi, true},
            {MovementType.WheelsTraverse, false},
            {MovementType.Flight, true},
            {MovementType.FlightFlyby, true},
            {MovementType.Immobile, false},
        };
        break;
        case MovementType.Treads:
            ability =
                    new int[] { 1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            pass = new Dictionary<MovementType, bool>
        {
            {MovementType.Foot, false},
            {MovementType.Treads, false},
            {MovementType.Wheels, false},
            {MovementType.TreadsAmphi, false},
            {MovementType.WheelsTraverse, false},
            {MovementType.Flight, true},
            {MovementType.FlightFlyby, true},
            {MovementType.Immobile, false},
        };
        break;
        case MovementType.Wheels:
            ability =
                    new int[] { 1, 0, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            pass = new Dictionary<MovementType, bool>
        {
            {MovementType.Foot, false},
            {MovementType.Treads, false},
            {MovementType.Wheels, false},
            {MovementType.TreadsAmphi, false},
            {MovementType.WheelsTraverse, false},
            {MovementType.Flight, true},
            {MovementType.FlightFlyby, true},
            {MovementType.Immobile, false},
        };
        break;
        case MovementType.TreadsAmphi:
            ability =
                    new int[] { 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 };
            pass = new Dictionary<MovementType, bool>
        {
            {MovementType.Foot, false},
            {MovementType.Treads, false},
            {MovementType.Wheels, false},
            {MovementType.TreadsAmphi, false},
            {MovementType.WheelsTraverse, false},
            {MovementType.Flight, true},
            {MovementType.FlightFlyby, true},
            {MovementType.Immobile, false},
        };
        break;
        case MovementType.WheelsTraverse:
            ability =
                    new int[] { 1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            pass = new Dictionary<MovementType, bool>
        {
            {MovementType.Foot, false},
            {MovementType.Treads, false},
            {MovementType.Wheels, false},
            {MovementType.TreadsAmphi, false},
            {MovementType.WheelsTraverse, false},
            {MovementType.Flight, true},
            {MovementType.FlightFlyby, true},
            {MovementType.Immobile, false},
        };
        break;
        case MovementType.Flight:
            ability =
                    new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 };
            pass = new Dictionary<MovementType, bool>
        {
            {MovementType.Foot, true},
            {MovementType.Treads, true},
            {MovementType.Wheels, true},
            {MovementType.TreadsAmphi, true},
            {MovementType.WheelsTraverse, true},
            {MovementType.Flight, false},
            {MovementType.FlightFlyby, false},
            {MovementType.Immobile, false},
        };
        break;
        case MovementType.FlightFlyby:
            ability =
                    new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 };
            pass = new Dictionary<MovementType, bool>
        {
            {MovementType.Foot, true},
            {MovementType.Treads, true},
            {MovementType.Wheels, true},
            {MovementType.TreadsAmphi, true},
            {MovementType.WheelsTraverse, true},
            {MovementType.Flight, true},
            {MovementType.FlightFlyby, true},
            {MovementType.Immobile, false},
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
        while (open.Count > 0 && furthest < self.speed)
        {
            foreach (var idx_dijk in open)
            {
                ArrayList<Position> moves = idx_dijk.Key.Adjacent(width, height);
                foreach (Position mov in moves)
                if (open.ContainsKey(mov) ||
                        closed.ContainsKey(mov) ||
                        radiate[mov.x, mov.y] >= wall ||
                    radiate[mov.x, mov.y] <= idx_dijk.Value + 1)
                {

                }
                else if (
                    ability[grid[mov.x - 1, mov.y - 1]] == 1 &&
                    placing[mov.x - 1, mov.y - 1] == null)
                {
                    fringe[mov] = (idx_dijk.Value + 1);
                    radiate[mov.x, mov.y] = idx_dijk.Value + 1;
                    if (gradient[mov.x, mov.y] < lowest)
                    {
                        //bestMoves.Clear();
                        bestMoves.add(new Position { x = mov.x - 1, y = mov.y - 1 });
                        lowest = gradient[mov.x, mov.y];
                    }
                    else if (gradient[mov.x, mov.y] == lowest)
                    {
                        bestMoves.add(new Position { x = mov.x - 1, y = mov.y - 1 });
                    }
                }
                else if (
                    ability[grid[mov.x - 1, mov.y - 1]] == 1 &&
                    (placing[mov.x - 1, mov.y - 1] != null &&
                    (//Math.abs(self.x - (mov.x - 1)) + Math.abs(self.y - (mov.y - 1)) < self.speed &&
                            (pass[placing[mov.x - 1, mov.y - 1].mobility] ||
                (placing[mov.x - 1, mov.y - 1].color == self.color &&
                    placing[mov.x - 1, mov.y - 1].mobility != MovementType.Immobile)
                ))))
                {
                    radiate[mov.x, mov.y] = idx_dijk.Value + 1;
                    fringe[mov] = (idx_dijk.Value + 1);
                    //furthest = Math.Max(idx_dijk.Value + 1, furthest);
                }
            }
            foreach (var kv in open)
            {
                closed[kv.Key] = (kv.Value);
            }
            open.Clear();
            foreach (var kv in fringe)
            {
                open[kv.Key] = (kv.Value);
            }
            fringe.Clear();
            furthest++;
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

    float[][] dijkstraInner(Unit self, int[][] grid, Unit[][] placing, float[][] d)
{
    int width = d.GetLength(0);
    int height = d.GetLength(1);
    int wall = 2222;
    int goal = 0;

    Dictionary<Position, float> open = new Dictionary<Position, float>(),
            fringe = new Dictionary<Position, float>(),
            closed = new Dictionary<Position, float>();

    for (int i = 0; i < width; i++)
    {
        for (int j = 0; j < height; j++)
        {
            if (d[i, j] == goal)
            {
                open[new Position(i, j)] = goal;
            }
                    /*else if (d[i, j] == ultimate)
                    {
                        open[new Position(i, j)] = goal;
                    }*/
            else if (d[i, j] >= wall)
            {
                closed[new Position(i, j)] = wall;
            }
        }
    }

    int[] ability =
            new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
    //plains forest desert jungle hills mountains ruins tundra road river basement
    Dictionary<MovementType, bool> pass = new Dictionary<MovementType, bool>
    {
        {MovementType.Foot, true},
        {MovementType.Treads, true},
        {MovementType.Wheels, true},
        {MovementType.TreadsAmphi, true},
        {MovementType.WheelsTraverse, true},
        {MovementType.Flight, true},
        {MovementType.FlightFlyby, true},
        {MovementType.Immobile, false},
    };
    switch (self.mobility)
    {
        case MovementType.Foot:
            ability =
                    new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 };
            pass = new Dictionary<MovementType, bool>
        {
            {MovementType.Foot, false},
            {MovementType.Treads, true},
            {MovementType.Wheels, false},
            {MovementType.TreadsAmphi, true},
            {MovementType.WheelsTraverse, false},
            {MovementType.Flight, true},
            {MovementType.FlightFlyby, true},
            {MovementType.Immobile, false},
        };
        break;
        case MovementType.Treads:
            ability =
                    new int[] { 1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            pass = new Dictionary<MovementType, bool>
        {
            {MovementType.Foot, false},
            {MovementType.Treads, false},
            {MovementType.Wheels, false},
            {MovementType.TreadsAmphi, false},
            {MovementType.WheelsTraverse, false},
            {MovementType.Flight, true},
            {MovementType.FlightFlyby, true},
            {MovementType.Immobile, false},
        };
        break;
        case MovementType.Wheels:
            ability =
                    new int[] { 1, 0, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            pass = new Dictionary<MovementType, bool>
        {
            {MovementType.Foot, false},
            {MovementType.Treads, false},
            {MovementType.Wheels, false},
            {MovementType.TreadsAmphi, false},
            {MovementType.WheelsTraverse, false},
            {MovementType.Flight, true},
            {MovementType.FlightFlyby, true},
            {MovementType.Immobile, false},
        };
        break;
        case MovementType.TreadsAmphi:
            ability =
                    new int[] { 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 };
            pass = new Dictionary<MovementType, bool>
        {
            {MovementType.Foot, false},
            {MovementType.Treads, false},
            {MovementType.Wheels, false},
            {MovementType.TreadsAmphi, false},
            {MovementType.WheelsTraverse, false},
            {MovementType.Flight, true},
            {MovementType.FlightFlyby, true},
            {MovementType.Immobile, false},
        };
        break;
        case MovementType.WheelsTraverse:
            ability =
                    new int[] { 1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            pass = new Dictionary<MovementType, bool>
        {
            {MovementType.Foot, false},
            {MovementType.Treads, false},
            {MovementType.Wheels, false},
            {MovementType.TreadsAmphi, false},
            {MovementType.WheelsTraverse, false},
            {MovementType.Flight, true},
            {MovementType.FlightFlyby, true},
            {MovementType.Immobile, false},
        };
        break;
        case MovementType.Flight:
            ability =
                    new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 };
            pass = new Dictionary<MovementType, bool>
        {
            {MovementType.Foot, true},
            {MovementType.Treads, true},
            {MovementType.Wheels, true},
            {MovementType.TreadsAmphi, true},
            {MovementType.WheelsTraverse, true},
            {MovementType.Flight, false},
            {MovementType.FlightFlyby, false},
            {MovementType.Immobile, false},
        };
        break;
        case MovementType.FlightFlyby:
            ability =
                    new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 };
            pass = new Dictionary<MovementType, bool>
        {
            {MovementType.Foot, true},
            {MovementType.Treads, true},
            {MovementType.Wheels, true},
            {MovementType.TreadsAmphi, true},
            {MovementType.WheelsTraverse, true},
            {MovementType.Flight, true},
            {MovementType.FlightFlyby, true},
            {MovementType.Immobile, false},
        };
        break;
    }
    while (open.Count > 0)
    {
        foreach (var idx_dijk in open)
        {
            ArrayList<Position> moves = idx_dijk.Key.Adjacent(width, height);
            foreach (Position mov in moves)
            if (open.ContainsKey(mov) ||
                    closed.ContainsKey(mov) ||
                    d[mov.x, mov.y] >= wall ||
                d[mov.x, mov.y] <= idx_dijk.Value + 1)
            {

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
                (Math.abs(self.x - (mov.x - 1)) + Math.abs(self.y - (mov.y - 1)) < self.speed &&
                        (pass[placing[mov.x - 1, mov.y - 1].mobility] ||
            (placing[mov.x - 1, mov.y - 1].color == self.color &&
                placing[mov.x - 1, mov.y - 1].mobility != MovementType.Immobile)
            ))))
            {
                fringe[mov] = (idx_dijk.Value + 1);
                d[mov.x, mov.y] = idx_dijk.Value + 1;
            }
        }
        foreach (var kv in open)
        {
            closed[kv.Key] = (kv.Value);
        }
        open.Clear();
        foreach (var kv in fringe)
        {
            open[kv.Key] = (kv.Value);
        }
        fringe.Clear();

    }

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
    }
    return d;
}

    float[][] dijkstra(Unit self, int[][] grid, Unit[][] placing, int targetX, int targetY)
{

    int width = grid.GetLength(0) + 2;
    int height = grid.GetLength(1) + 2;
    float unexplored = 1111;
    float goal = 0;
    int wall = 2222;

    float[][] d = new float[width, height];

    for (int i = 1; i < width - 1; i++)
    {
        for (int j = 1; j < height - 1; j++)
            d[i, j] = unexplored;
    }

    for (int i = 0; i < width; i++)
    {
        d[i, 0] = wall;
        d[i, (height - 1)] = wall;

    }
    for (int j = 1; j < height - 1; j++)
    {
        d[0, j] = wall;
        d[(width - 1), j] = wall;
    }
    d[targetX + 1, targetY + 1] = goal;

    d = dijkstraInner(self, grid, placing, d);

    return d;
}
    float[][] dijkstra(Unit self, int[][] grid, Unit[][] placing, int[] targetColors)
{

    int width = grid.GetLength(0) + 2;
    int height = grid.GetLength(1) + 2;
    float unexplored = 1111;
    float goal = 0;
    int wall = 2222;

    float[][] d = new float[width, height];

    for (int i = 1; i < width - 1; i++)
    {
        for (int j = 1; j < height - 1; j++)
            d[i, j] = unexplored;
    }

    for (int i = 0; i < width; i++)
    {
        d[i, 0] = wall;
        d[i, (height - 1)] = wall;

    }
    for (int j = 1; j < height - 1; j++)
    {
        d[0, j] = wall;
        d[(width - 1), j] = wall;
    }
    for (int i = 1; i < width - 1; i++)
    {
        for (int j = 1; j < height - 1; j++)
        {
            if (targetColors.Any(c => placing[i - 1, j - 1] != null && c == placing[i - 1, j - 1].color))
            {
                //if (placing[i - 1, j - 1].name == "Castle" || placing[i - 1, j - 1].name == "Estate")
                d[i, j] = goal;
            }
        }
    }
    d = dijkstraInner(self, grid, placing, d);

    return d;
}
    float[][] SmartDijkstra(Unit self, int currentWeapon, int[][] grid, Unit[][] placing, int[] targetColors)
{

    int width = grid.GetLength(0) + 2;
    int height = grid.GetLength(1) + 2;
    float unexplored = 1111;
    int wall = 2222;

    float[][] d = new float[width, height];

    for (int i = 1; i < width - 1; i++)
    {
        for (int j = 1; j < height - 1; j++)
        {
            if (placing[i - i, j - 1] == null)
            d[i, j] = unexplored;
            else
            d[i, j] = wall;
        }
    }

    for (int i = 0; i < width; i++)
    {
        d[i, 0] = wall;
        d[i, (height - 1)] = wall;
    }
    for (int j = 1; j < height - 1; j++)
    {
        d[0, j] = wall;
        d[(width - 1), j] = wall;
    }
    if (currentWeapon > -1)
        d = DijkstraAttackPositions(self.weaponry[currentWeapon], self.mobility, self.color, targetColors, grid, placing, d);

    return d;
}
    public float[][] gradient = new float[27, 27];
    ArrayList<DirectedPosition> getDijkstraPath(Unit active, int[][] grid, Unit[][] placing)
    {
        int width = grid.GetLength(0);
        int height = grid.GetLength(1);
        ArrayList<DirectedPosition> path = new ArrayList<DirectedPosition>();
        int currentX = active.x, currentY = active.y;
        Direction currentFacing = active.facing;
        Position newpos = new Position(currentX, currentY);
        float[][] rad = new float[width + 2, height + 2], rad0 = new float[width + 2, height + 2], rad1 = new float[width + 2, height + 2];
        Position best0 = null, best1 = null;
        ArrayList<Position> bests0 = new ArrayList<Position>(), bests1 = new ArrayList<Position>();
        Dictionary<Position, Position> mtt0 = new Dictionary<Position, Position>(), mtt1 = new Dictionary<Position, Position>();
        float choice = -1, eff0 = 0, eff1 = 0;
        if (active.weaponry[1].kind != WeaponType.None)
        {
            rad1 = ViableMoves(active, 1, grid, placing);
            bests1 = bestMoves.Clone();
            mtt1 = movesToTargets.Clone();
            var bd = bests1.OrderByDescending(p => (movesToTargets.ContainsKey(p))
                    ? active.weaponry[1].multipliers[Unit.UnitTypeAsNumber(placing[movesToTargets[p].x, movesToTargets[p].y].kind)]
            : 0.005F * rad1[p.x + 1, p.y + 1]);
            best1 = bd.TakeWhile(p => (movesToTargets.ContainsKey(p))
                    ? active.weaponry[1].multipliers[Unit.UnitTypeAsNumber(placing[movesToTargets[p].x, movesToTargets[p].y].kind)] ==
            active.weaponry[1].multipliers[Unit.UnitTypeAsNumber(placing[movesToTargets[bd.First()].x, movesToTargets[bd.First()].y].kind)]
            : 0.005F * rad1[p.x + 1, p.y + 1] == 0.005F * rad1[bd.First().x + 1, bd.First().y + 1]
            ).RandomElement();
            //best1 = bests1.RandomElement();
            eff1 = (mtt1.ContainsKey(best1))
                    ? active.weaponry[1].multipliers[Unit.UnitTypeAsNumber(placing[mtt1[best1].x, mtt1[best1].y].kind)]
            : 0;
            choice = 1;//rad1[best1.x + 1, best1.y + 1];

        }
        if (active.weaponry[0].kind != WeaponType.None)
        {
            rad0 = ViableMoves(active, 0, grid, placing);
            bests0 = bestMoves.Clone();
            mtt0 = movesToTargets.Clone();
            var bd = bests0.OrderByDescending(p => (movesToTargets.ContainsKey(p))
                    ? active.weaponry[0].multipliers[Unit.UnitTypeAsNumber(placing[movesToTargets[p].x, movesToTargets[p].y].kind)]
            : 0.005F * rad0[p.x + 1, p.y + 1]);
            best0 = bd.TakeWhile(p => (movesToTargets.ContainsKey(p))
                    ? active.weaponry[0].multipliers[Unit.UnitTypeAsNumber(placing[movesToTargets[p].x, movesToTargets[p].y].kind)] ==
            active.weaponry[0].multipliers[Unit.UnitTypeAsNumber(placing[movesToTargets[bd.First()].x, movesToTargets[bd.First()].y].kind)]
            : 0.005F * rad0[p.x + 1, p.y + 1] == 0.005F * rad0[bd.First().x + 1, bd.First().y + 1]
            ).RandomElement();
            eff0 = (mtt0.ContainsKey(best0))
                    ? active.weaponry[0].multipliers[Unit.UnitTypeAsNumber(placing[mtt0[best0].x, mtt0[best0].y].kind)]
            : 0;
            //choice = (choice == 2222) ? 1 : (best1 != null && rad1[best1.x + 1, best1.y + 1] > rad0[best0.x + 1, best0.y + 1]) ? 1 : 0;
            if (eff1 > eff0)
            {
                choice = 1;
            }
            else if (eff0 > eff1)
            {
                choice = 0;
            }
            else if (active.weaponry[1].damage > active.weaponry[0].damage)
            {
                choice = 1;
            }
            else if (active.weaponry[0].damage > active.weaponry[1].damage)
            {
                choice = 0;
            }
            else
            {
                choice = (best1 != null && rad1[best1.x + 1, best1.y + 1] > rad0[best0.x + 1, best0.y + 1]) ? 1 : 0;
            }
        }
        else
        {
            choice = (active.weaponry[1].kind == WeaponType.None) ? -1 : 1;
        }
        switch ((int)choice)
        {
            case -1: bestMoves = new ArrayList<Position> { new Position(active.x, active.y) };
            best = new Position(active.x, active.y);
            movesToTargets = new Dictionary<Position, Position>();
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
        if (currentlyFiring > -1 && movesToTargets.ContainsKey(best))
        {
            target = new DirectedPosition(movesToTargets[best].x, movesToTargets[best].y);
            target = DirectedPosition.TurnToFace(best, movesToTargets[best]);
            //                                active.weaponry[currentlyFiring].minRange, active.weaponry[currentlyFiring].maxRange).Where(pos => UnitGrid[pos.x, pos.y] != null && active.isOpposed(UnitGrid[pos.x, pos.y])).RandomElement();
        }
        else target = null;
            /*if (best.x == active.x && best.y == active.y)// && ((0 == placing[newX, newY].color) ? 0 != active.color : 0 == active.color))
            {
                return new ArrayList<DirectedPosition> { }; //new DirectedPosition {x=active.x, y=active.y, dir= active.facing }
            }*/
        writeShowLog("Choice is: " + choice);
        writeShowLog("Best is: " + best.x + ", " + best.y);
        writeShowLog("Distance is: " + (rad[best.x + 1, best.y + 1]));
            /*
            foreach (Position p in bestMoves)
            {
                writeShowLog("    " + p.x + ", " + p.y + " with an occupant of " + ((placing[p.x, p.y] != null) ? placing[p.x, p.y].name : "EMPTY"));
            }*/
        DirectedPosition oldpos = new DirectedPosition(best.x, best.y);
        path.add(new DirectedPosition(best.x, best.y));
        if (best.x == active.x && best.y == active.y)
        {

        }
        else
        {
            for (int f = 0; f < active.speed; f++)
            {
                Dictionary<Position, float> near = new Dictionary<Position, float>() { { oldpos, rad[oldpos.x + 1, oldpos.y + 1] } }; // { { oldpos, rad[oldpos.x + 1, oldpos.y + 1] } }
                foreach (Position pos in oldpos.Adjacent(width, height))
                near[pos] = rad[pos.x + 1, pos.y + 1];
                var ordered = near.OrderBy(kv => kv.Value);
                newpos = ordered.TakeWhile(kv => kv.Value == ordered.First().Value).RandomElement().Key;
                if (near.All(e => e.Value == near[newpos]))
                return new ArrayList<DirectedPosition>();
                #if DEBUG
                StringBuilder sb = new StringBuilder();
                for (int jj = height; jj >= 1; jj--)
                {
                    for (int ii = 1; ii < width + 1; ii++)
                    {
                        sb.AppendFormat("{0,5}", rad[ii, jj]);
                    }
                    sb.AppendLine();
                }
                writeShowLog(sb.ToString());
                #endif

                int newX = newpos.x, newY = newpos.y;
                if (!(newX == currentX && newY == currentY))
                {
                    currentX = newX;
                    currentY = newY;
                    //                    d_inv = dijkstraInner(active, grid, placing, d_inv);
                }
                DirectedPosition dp = new DirectedPosition(currentX, currentY, currentFacing);
                if (dp.x == active.x && dp.y == active.y)//bestMoves.Any(b => b.x == dp.x && b.y == dp.y))// && ((0 == placing[newX, newY].color) ? 0 != active.color : 0 == active.color))
                {
                    //oldpos = new DirectedPosition(currentX, currentY, currentFacing);
                    writeShowLog("Found target.");
                    path.add(dp);
                    f = active.speed + 10;
                }
                else
                {
                    writeShowLog("Continuing pathfind, f is " + f + ", position is " + dp.x + ", " + dp.y);
                    if (path.Last().x == dp.x && path.Last().y == dp.y)
                    {
                        writeShowLog("Tried to reach unreachable target!!!");
                    }
                    path.add(dp);
                }
                oldpos = new DirectedPosition(currentX, currentY, currentFacing);
            }
        }
        path.Reverse();
        path[0].dir = active.facing;
        DirectedPosition old2 = new DirectedPosition(path.First().x, path.First().y);
        for (int i = 1; i < path.Count; i++)
        {
            currentX = old2.x;
            currentY = old2.y;
            int newX = path[i].x;
            int newY = path[i].y;
            if (newY > currentY)
            {
                path[i].dir = Direction.SE;
            }
            else if (newY < currentY)
            {
                path[i].dir = Direction.NW;

            }
            else
            {
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
    ArrayList<DirectedPosition> getDijkstraPath(Unit active, int[][] grid, Unit[][] placing, int targetX, int targetY)
    {
        int width = grid.GetLength(0);
        int height = grid.GetLength(1);
        ArrayList<DirectedPosition> path = new ArrayList<DirectedPosition>();
        int currentX = active.x, currentY = active.y;
        Direction currentFacing = active.facing;
        Position newpos = new Position(currentX, currentY);
        int choice = -1;
        switch ((int)choice)
        {
            case -1: bestMoves = new ArrayList<Position> { new Position(targetX, targetY) };
            best = new Position(targetX, targetY);
            movesToTargets = new Dictionary<Position, Position>();
            currentlyFiring = -1;
            //gradient.Fill(2222);
            break;
        }
        target = null;
            /*if (best.x == active.x && best.y == active.y)// && ((0 == placing[newX, newY].color) ? 0 != active.color : 0 == active.color))
            {
                return new ArrayList<DirectedPosition> { }; //new DirectedPosition {x=active.x, y=active.y, dir= active.facing }
            }*/
        writeShowLog("Choice is: " + choice);
        writeShowLog("Best is: " + best.x + ", " + best.y);
            /*
            foreach (Position p in bestMoves)
            {
                writeShowLog("    " + p.x + ", " + p.y + " with an occupant of " + ((placing[p.x, p.y] != null) ? placing[p.x, p.y].name : "EMPTY"));
            }*/
        DirectedPosition oldpos = new DirectedPosition(best.x, best.y);
        path.add(new DirectedPosition(best.x, best.y));
        if (best.x == active.x && best.y == active.y)
        {

        }
        else
        {
            for (int f = 0; f < active.speed; f++)
            {
                Dictionary<Position, float> near = new Dictionary<Position, float>() { { oldpos, outward[oldpos.x + 1, oldpos.y + 1] } }; // { { oldpos, rad[oldpos.x + 1, oldpos.y + 1] } }
                foreach (Position pos in oldpos.Adjacent(width, height))
                near[pos] = outward[pos.x + 1, pos.y + 1];
                var ordered = near.OrderBy(kv => kv.Value);
                newpos = ordered.TakeWhile(kv => kv.Value == ordered.First().Value).RandomElement().Key;
                if (near.All(e => e.Value == near[newpos]))
                return new ArrayList<DirectedPosition>();
                #if DEBUG
                StringBuilder sb = new StringBuilder();
                for (int jj = height; jj >= 1; jj--)
                {
                    for (int ii = 1; ii < width + 1; ii++)
                    {
                        sb.AppendFormat("{0,5}", outward[ii, jj]);
                    }
                    sb.AppendLine();
                }
                writeShowLog(sb.ToString());
                #endif

                int newX = newpos.x, newY = newpos.y;
                if (!(newX == currentX && newY == currentY))
                {
                    currentX = newX;
                    currentY = newY;
                    //                    d_inv = dijkstraInner(active, grid, placing, d_inv);
                }
                DirectedPosition dp = new DirectedPosition(currentX, currentY, currentFacing);
                if (dp.x == active.x && dp.y == active.y)//bestMoves.Any(b => b.x == dp.x && b.y == dp.y))// && ((0 == placing[newX, newY].color) ? 0 != active.color : 0 == active.color))
                {
                    //oldpos = new DirectedPosition(currentX, currentY, currentFacing);
                    writeShowLog("Found target.");
                    path.add(dp);
                    f = active.speed + 10;
                }
                else
                {
                    writeShowLog("Continuing pathfind, f is " + f + ", position is " + dp.x + ", " + dp.y);
                    if (path.Last().x == dp.x && path.Last().y == dp.y)
                    {
                        writeShowLog("Tried to reach unreachable target!!!");
                    }
                    path.add(dp);
                }
                oldpos = new DirectedPosition(currentX, currentY, currentFacing);
            }
        }
        path.Reverse();
        path[0].dir = active.facing;
        DirectedPosition old2 = new DirectedPosition(path.First().x, path.First().y);
        for (int i = 1; i < path.Count; i++)
        {
            currentX = old2.x;
            currentY = old2.y;
            int newX = path[i].x;
            int newY = path[i].y;
            if (newY > currentY)
            {
                path[i].dir = Direction.SE;
            }
            else if (newY < currentY)
            {
                path[i].dir = Direction.NW;

            }
            else
            {
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
    void RetryPlacement()
    {
        failCount++;
        Console.WriteLine("\n\n!!!!! P L A C E M E N T   F A I L U R E   " + failCount + " !!!!!\n\n");
        if (failCount > 20)
        {
            Console.WriteLine("Too many placement failures.");
            Console.In.ReadLine();
            return;
        }
        PlaceUnits();
    }
    public void PlaceUnits()
    {
        int[] allcolors = { 1, 2, 3, 4, 5, 6, 7 };
        Colors = new int[4];
        ReverseColors = new int[8];
        bool[] taken = { false, false, false, false, false, false, false };
        for (int i = 1; i < 4; i++)
        {
            int col = (i == 1) ? r.Next(1,7) : r.Next(7);
            while (taken[col])
                col = (i == 1) ? r.Next(1, 7) : r.Next(7);
            Colors[i] = allcolors[col];
            ReverseColors[Colors[i]] = i;
            taken[col] = true;
        }
        Colors[0] = 0;
        ReverseColors[0] = 0;

        for (int section = 0; section < 2; section++)
        {
            int rx = (width / 4) + (width / 2) * (section % 2);
            int ry = 3 + (height / 6);
            //processSingleOutlined(facilityps[(colors[section] == 0) ? 3 : 2], colors[section], dirs[r.Next(4)])
            if (Colors[section] == 0)
            {
                UnitGrid[rx, ry] = new Unit("Estate", Colors[section], rx, ry);
                targetX[1] = rx;
                targetY[1] = ry;
                targetX[2] = rx;
                targetY[2] = ry;
                targetX[3] = rx;
                targetY[3] = ry;
            }
            else
            {
                UnitGrid[rx, ry] = new Unit("Castle", Colors[section], rx, ry);
                targetX[0] = rx;
                targetY[0] = ry;
            }
            FieldMap.Land[rx, ry] = 10;// +Colors[section];
            for (int i = rx - (width / 6); i < rx + (width / 6); i++)
            {
                for (int j = ry - (height / 6); j < ry + (height / 6); j++)
                {
                    if (UnitGrid[i, j] != null)
                    continue;
                    //r.Next(14) <= 2
                    if (r.Next(14) <= 2 && (FieldMap.Land[i, j] == 0 || FieldMap.Land[i, j] == 1 || FieldMap.Land[i, j] == 2 || FieldMap.Land[i, j] == 4 || FieldMap.Land[i, j] == 8))
                    {
                        //
                        UnitGrid[i, j] = new Unit(r.Next(24, 28), Colors[section], i, j);
                        FieldMap.Land[i, j] = 10;// +Colors[section];
                        //processSingleOutlined(facilityps[r.Next(3) % 2], colors[section], dirs[r.Next(4)]);
                    }

                }
            }
        }
        for (int section = 2; section < 4; section++)
        {
            int rx = (width / 4) + (width / 2) * (section % 2);
            int ry = height - 3 - (height / 6);
            UnitGrid[rx, ry] = new Unit(((Colors[section] == 0) ? Unit.UnitLookup["Estate"] : Unit.UnitLookup["Castle"]), Colors[section], rx, ry);
            FieldMap.Land[rx, ry] = 10;// +Colors[section];
            for (int i = rx - (width / 8); i < rx + (width / 8); i++)
            {
                for (int j = ry - (height / 8); j < ry + (height / 8); j++)
                {
                    if (UnitGrid[i, j] != null)
                    continue;
                    //r.Next(14) <= 2
                    if (r.Next(14) <= 2 && (FieldMap.Land[i, j] == 0 || FieldMap.Land[i, j] == 1 || FieldMap.Land[i, j] == 2 || FieldMap.Land[i, j] == 4 || FieldMap.Land[i, j] == 8))
                    {
                        UnitGrid[i, j] = new Unit(r.Next(24, 28), Colors[section], i, j);
                        FieldMap.Land[i, j] = 10;// +Colors[section];

                    }

                }
            }
        }
        ArrayList<Tuple<int, int>> guarantee = new ArrayList<Tuple<int, int>>();
        for (int section = 0; section < 4; section++) // section < 4
        {
            for (int i = (width / 2) * (section % 2); i < (width / 2) + (width / 2) * (section % 2); i++)
            {
                for (int j = (section / 2 == 0) ? 0 : height / 2; j < ((section / 2 == 0) ? height / 2 : height); j++)
                {
                    if (UnitGrid[i, j] != null)
                    continue;
                    int currentUnit = Unit.TerrainToUnits[FieldMap.Land[i, j]].RandomElement();
                    //foot 0-0, treads 1-5, wheels 6-8, flight 9-10
                    if (r.Next(25) <= 3)
                    {
                        //if(Unit.TerrainToMobilities[FieldMap.Land[i,j]].Contains(MovementType.TreadsAmphi))
                        //    UnitGrid[i, j] = new Unit(Unit.UnitLookup["Tank_T"], Colors[section], section, i, j);
                        //else
                        UnitGrid[i, j] = new Unit(currentUnit, Colors[section], section, i, j);
                    }

                }
            }
                /*if (guarantee.Count == section)
                {
                    int rgx = r.Next((width / 2) * (section % 2) + 1, (width / 2) - 1 + (width / 2) * (section % 2));
                    int rgy = r.Next((section / 2 == 0) ? 1 : height / 2, ((section / 2 == 0) ? height / 2 : height - 1));
                    int problems = 0;
                    while (UnitGrid[rgx, rgy] != null)
                    {
                        rgx = r.Next((width / 2) * (section % 2) + 1, (width / 2) - 1 + (width / 2) * (section % 2));
                        rgy = r.Next((section / 2 == 0) ? 1 : height / 2, ((section / 2 == 0) ? height / 2 : height - 1));
                        if (UnitGrid[rgx, rgy] != null)
                            problems++;
                        if (problems > 10)
                        {
                            RetryPlacement();
                            return;
                        }
                    }
                    UnitGrid[rgx, rgy] = new Unit(Unit.TerrainToUnits[FieldMap.Land[rgx, rgy]].RandomElement(), Colors[section], section, rgx, rgy);
                }*/

        }
        for (int i = 1; i < width - 1; i++)
        {
            for (int j = 1; j < height - 1; j++)
            {
                if (r.Next(30) <= 1 && UnitGrid[i, j] == null)
                {
                    int rs = 0;// r.Next(4);
                    int currentUnit = Unit.TerrainToUnits[FieldMap.Land[i, j]].RandomElement();
                    UnitGrid[i, j] = new Unit(currentUnit, Colors[rs], rs, i, j);

                }
            }
        }
        Unit temp = UnitGrid.RandomFactionUnit(Colors[ActingFaction]);
        ActiveUnit = new Unit(temp);
        UnitGrid[temp.x, temp.y] = null;
    }
    public ArrayList<DirectedPosition> BestPath;
    public DirectedPosition FuturePosition, target;
    public int currentlyFiring = -1;
    private boolean killSuccess = false, hitSuccess = false;
    private int previousHP;
    private Thread thr = null;
    public void dispose()
    {
        if (thr != null)
            thr.Abort();
    }
    public void ShowTargets(Unit u, Weapon w)
    {
        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                if (UnitGrid[i, j] != null && u.isOpposed(UnitGrid[i, j])
                && Math.abs(u.x - i) + Math.abs(u.y - j) >= w.minRange
                    && Math.abs(u.x - i) + Math.abs(u.y - j) <= w.maxRange
                    && w.multipliers[Unit.UnitTypeAsNumber(UnitGrid[i, j].kind)] > 0)
                {
                    Speech s = new Speech { large = false, x = i, y = j,
                        text = (100 - UnitGrid[i, j].dodge * 10) + "% / " +
                        (int)((w.multipliers[Unit.UnitTypeAsNumber(UnitGrid[i, j].kind)] - 0.1f * UnitGrid[i, j].armor) * w.damage) + ""
                };
                    speaking.add(s);
                    FieldMap.Highlight[i, j] = HighlightType.Bright;
                }
                else
                {
                    FieldMap.Highlight[i, j] = HighlightType.Dim;
                }
            }
        }
    }
    float[][] outward;
    public void advanceTurn()
    {
        UnitGrid[ActiveUnit.x, ActiveUnit.y] = new Unit(ActiveUnit);
        ActingFaction = (ActingFaction + 1) % 4;
        Unit temp = UnitGrid.RandomFactionUnit(Colors[ActingFaction]);
        ActiveUnit = new Unit(temp);
        UnitGrid[temp.x, temp.y] = null;
        speaking.Clear();
        if (ActingFaction == 1) GameGDX.state = GameState.PC_Select_Move;
        else GameGDX.state = GameState.NPC_Play;

        CurrentMode = Mode.Selecting;
        TaskSteps = 0;
    }

    public void ProcessStep()
    {
        if (GameGDX.state == GameState.PC_Select_Move)
        {

            Effects.CenterCamera(ActiveUnit.x, ActiveUnit.y, 0.5F);
            outward = dijkstra(ActiveUnit, FieldMap.Land, UnitGrid, ActiveUnit.x, ActiveUnit.y);
            for (int i = 0; i < width; i++)
            {
                for (int j = 0; j < height; j++)
                {
                    FieldMap.Highlight[i, j] = (outward[i + 1, j + 1] > 0 && outward[i + 1, j + 1] <= ActiveUnit.speed) ? HighlightType.Bright : HighlightType.Dim;
                }
            }
            FieldMap.Highlight[ActiveUnit.x, ActiveUnit.y] = HighlightType.Spectrum;
            return;
        }
        if (GameGDX.state == GameState.PC_Select_UI)
        {
            return;
        }
        if (GameGDX.state == GameState.PC_Select_Action)
        {
            return;
        }
        TaskSteps++;
        switch (CurrentMode)
        {
            case Mode.Selecting:

                if (TaskSteps > 4 && GameGDX.state != GameState.PC_Play_Move && thr != null && thr.ThreadState == ThreadState.Stopped)
                {
                    FuturePosition = new DirectedPosition(ActiveUnit.x, ActiveUnit.y, ActiveUnit.facing);
                    for (int i = 0; i < width; i++)
                    {
                        for (int j = 0; j < height; j++)
                        {
                            FieldMap.Highlight[i, j] = HighlightType.Plain;
                        }
                    }
                    TaskSteps = 0;
                    GameGDX.stateTime = 0;
                    CurrentMode = Mode.Moving;
                }
                else if (TaskSteps <= 1 && (thr == null || thr.ThreadState == ThreadState.Stopped) && GameGDX.state == GameState.NPC_Play)
                {
                    thr = new Thread(() =>
                            {
                                    BestPath = getDijkstraPath(ActiveUnit, FieldMap.Land, UnitGrid);
                    });
                    thr.Start();

                    Effects.CenterCamera(ActiveUnit.x, ActiveUnit.y, 0.5F);
                    outward = dijkstra(ActiveUnit, FieldMap.Land, UnitGrid, ActiveUnit.x, ActiveUnit.y);
                    for (int i = 0; i < width; i++)
                    {
                        for (int j = 0; j < height; j++)
                        {
                            FieldMap.Highlight[i, j] = (outward[i + 1, j + 1] > 0 && outward[i + 1, j + 1] <= ActiveUnit.speed) ? HighlightType.Bright : HighlightType.Dim;
                        }
                    }
                    FieldMap.Highlight[ActiveUnit.x, ActiveUnit.y] = HighlightType.Spectrum;

                }
                else if (GameGDX.state == GameState.PC_Play_Move)
                {
                    BestPath = getDijkstraPath(ActiveUnit, FieldMap.Land, UnitGrid, GameGDX.cursor.x, GameGDX.cursor.y);
                    FuturePosition = new DirectedPosition(ActiveUnit.x, ActiveUnit.y, ActiveUnit.facing);
                    for (int i = 0; i < width; i++)
                    {
                        for (int j = 0; j < height; j++)
                        {
                            FieldMap.Highlight[i, j] = HighlightType.Plain;
                        }
                    }
                    TaskSteps = 0;
                    GameGDX.stateTime = 0;
                    CurrentMode = Mode.Moving;
                }
                else if (GameGDX.state == GameState.PC_Play_Action)
                {
                    target = DirectedPosition.TurnToFace(new Position(ActiveUnit.x, ActiveUnit.y), new Position(GameGDX.cursor.x, GameGDX.cursor.y));
                    CurrentMode = Mode.Attacking;
                    GameGDX.state = GameState.PC_Play_Action;
                    TaskSteps = 0;
                }
                else
                {
                }
                break;
            case Mode.Moving:
                ActiveUnit.x = FuturePosition.x;
                ActiveUnit.y = FuturePosition.y;
                if (BestPath.Count <= 0 || TaskSteps > ActiveUnit.speed + 1)
                { //false == (ActiveUnit.weaponry[0].kind == WeaponType.None && ActiveUnit.weaponry[1].kind == WeaponType.None)
                        /*

                            (Position.WithinRange(ActiveUnit.x, ActiveUnit.y,
                        ActiveUnit.weaponry[currentlyFiring].minRange, ActiveUnit.weaponry[currentlyFiring].minRange, width, height,
                        ActiveUnit.weaponry[currentlyFiring].maxRange, ActiveUnit.weaponry[currentlyFiring].maxRange).Any(
                        pos => UnitGrid[pos.x, pos.y] != null && ActiveUnit.isOpposed(UnitGrid[pos.x, pos.y])))
                         */
                    if (GameGDX.state == GameState.PC_Play_Move)
                    {
                        ActiveUnit.worldX = 20 + ActiveUnit.x * 64 + ActiveUnit.y * 64;
                        ActiveUnit.worldY = 6 + ActiveUnit.x * 32 - ActiveUnit.y * 32;
                        GameGDX.state = GameState.PC_Select_UI;
                        ArrayList<MenuEntry> entries = new ArrayList<MenuEntry>();
                        if (ActiveUnit.weaponry[0].kind != WeaponType.None)
                            entries.add(new MenuEntry(ActiveUnit.weaponry[0].kind.ToString(), () =>
                                    {
                                            currentlyFiring = 0;
                        ShowTargets(ActiveUnit, ActiveUnit.weaponry[0]);
                        CurrentMode = Mode.Selecting; TaskSteps = 0;
                        GameGDX.state = GameState.PC_Select_Action;
                        }));
                        if (ActiveUnit.weaponry[1].kind != WeaponType.None)
                            entries.add(new MenuEntry(ActiveUnit.weaponry[1].kind.ToString(), () =>
                                    {
                                            currentlyFiring = 1;
                        ShowTargets(ActiveUnit, ActiveUnit.weaponry[1]);
                        CurrentMode = Mode.Selecting; TaskSteps = 0;
                        GameGDX.state = GameState.PC_Select_Action;
                        }));

                        UI.postActor(UI.makeMenu(entries, ActiveUnit.color));//, ActiveUnit.worldX, ActiveUnit.worldY);
                        TaskSteps = 0;
                        CurrentMode = Mode.Selecting;
                        break;
                    }
                    else if (currentlyFiring > -1 && target != null && UnitGrid[target.x, target.y] != null && ActiveUnit.isOpposed(UnitGrid[target.x, target.y]))
                    {
                        ActiveUnit.worldX = 20 + ActiveUnit.x * 64 + ActiveUnit.y * 64;
                        ActiveUnit.worldY = 6 + ActiveUnit.x * 32 - ActiveUnit.y * 32;
                        CurrentMode = Mode.Attacking;
                        TaskSteps = 0;
                        break;
                    }

                    advanceTurn();
                    break;
                }
                FuturePosition = new DirectedPosition(BestPath.First().x, BestPath.First().y, BestPath.First().dir);
                int oldx = ActiveUnit.x, oldy = ActiveUnit.y;

                ActiveUnit.facingNumber = ConvertDirection(FuturePosition.dir);
                ActiveUnit.facing = FuturePosition.dir;
                NilTask n = new NilTask(() =>
                        {
                                ActiveUnit.worldX += (FuturePosition.x - oldx) * 4 + (FuturePosition.y - oldy) * 4;
                ActiveUnit.worldY += (FuturePosition.x - oldx) * 2 - (FuturePosition.y - oldy) * 2;
                ActiveUnit.worldY += ((LocalMap.Depths[FieldMap.Land[FuturePosition.x, FuturePosition.y]] - LocalMap.Depths[FieldMap.Land[oldx, oldy]]) * 3F) / 16F;
                });
                Timer.instance().scheduleTask(n, 0, GameGDX.updateStep / 16F, 15);

                Effects.CenterCamera(FuturePosition, 1F);

                BestPath.RemoveAt(0);

                break;
            case Mode.Attacking:
                if (TaskSteps <= 1)
                {
                    if (target.x - ActiveUnit.x <= target.y - ActiveUnit.y && (target.x - ActiveUnit.x) * -1 <= target.y - ActiveUnit.y)
                    {
                        ActiveUnit.facing = Direction.SE;
                        ActiveUnit.facingNumber = 0;
                        if (UnitGrid[target.x, target.y].speed > 0)
                        {
                            UnitGrid[target.x, target.y].facing = Direction.NW;
                            UnitGrid[target.x, target.y].facingNumber = 2;
                        }
                    }
                    else if ((target.x - ActiveUnit.x) * -1 <= target.y - ActiveUnit.y && target.x - ActiveUnit.x >= target.y - ActiveUnit.y)
                    {
                        ActiveUnit.facing = Direction.NE;
                        ActiveUnit.facingNumber = 3;
                        if (UnitGrid[target.x, target.y].speed > 0)
                        {
                            UnitGrid[target.x, target.y].facing = Direction.SW;
                            UnitGrid[target.x, target.y].facingNumber = 1;
                        }
                    }
                    else if (target.x - ActiveUnit.x >= target.y - ActiveUnit.y && (target.x - ActiveUnit.x) * -1 >= target.y - ActiveUnit.y)
                    {
                        ActiveUnit.facing = Direction.NW;
                        ActiveUnit.facingNumber = 2;
                        if (UnitGrid[target.x, target.y].speed > 0)
                        {
                            UnitGrid[target.x, target.y].facing = Direction.SE;
                            UnitGrid[target.x, target.y].facingNumber = 0;
                        }
                    }
                    else if ((target.x - ActiveUnit.x) * -1 >= target.y - ActiveUnit.y && target.x - ActiveUnit.x <= target.y - ActiveUnit.y)
                    {
                        ActiveUnit.facing = Direction.SW;
                        ActiveUnit.facingNumber = 1;
                        if (UnitGrid[target.x, target.y].speed > 0)
                        {
                            UnitGrid[target.x, target.y].facing = Direction.NE;
                            UnitGrid[target.x, target.y].facingNumber = 3;
                        }
                    }
                    else
                    {
                        ActiveUnit.facing = Direction.SE;
                        ActiveUnit.facingNumber = 0;
                        if (UnitGrid[target.x, target.y].speed > 0)
                        {
                            UnitGrid[target.x, target.y].facing = Direction.NW;
                            UnitGrid[target.x, target.y].facingNumber = 2;
                        }
                    }

                    GameGDX.attackTime = 0;
                        /*currentlyFiring = -1;
                        if (ActiveUnit.weaponry[1].kind != WeaponType.None && ActiveUnit.weaponry[0].kind != WeaponType.None)
                            currentlyFiring = r.Next(2);
                        else if (ActiveUnit.weaponry[0].kind != WeaponType.None)
                            currentlyFiring = 0;
                        else currentlyFiring = 1;*/
                    if (currentlyFiring > -1)
                    {
                        hitSuccess = UnitGrid[target.x, target.y].attemptDodge(ActiveUnit.weaponry[currentlyFiring]);
                        if (hitSuccess)
                        {
                            previousHP = UnitGrid[target.x, target.y].currentHealth;
                            killSuccess = UnitGrid[target.x, target.y].takeDamage(ActiveUnit.weaponry[currentlyFiring]);
                        }
                    }
                    else
                    {
                        hitSuccess = false;
                        killSuccess = false;
                    }
                    ActiveUnit.visual = (ActiveUnit.weaponry[1].kind == WeaponType.None && ActiveUnit.weaponry[0].kind == WeaponType.None) ? VisualAction.Normal : VisualAction.Firing;
                }
                else if (TaskSteps > 4 + 1 * (Math.abs(target.x - ActiveUnit.x) + Math.abs(target.y - ActiveUnit.y)))
                {
                    currentlyFiring = -1;
                    if (killSuccess)
                        UnitGrid[target.x, target.y] = null;
                    killSuccess = false;
                    hitSuccess = false;
                    advanceTurn();

                    break;
                }
                else if (TaskSteps == 1 + 1 * (Math.abs(target.x - ActiveUnit.x) + Math.abs(target.y - ActiveUnit.y)) && currentlyFiring > -1)
                {
                    if (hitSuccess || Unit.WeaponDisplays[ActiveUnit.unitIndex][currentlyFiring] == 1 || Unit.WeaponDisplays[ActiveUnit.unitIndex][currentlyFiring] == 7)
                    {
                        GameGDX.receiveTime = 0;
                            /*
                        int w = ((row < width) ? width - 1 - row + col : col); //height + (width - 1 - row) +
                        int h = (row < width) ? col : row - width + col;
                             */

                        ActiveUnit.targeting = new ArrayList<DirectedPosition> { new DirectedPosition(target.x, target.y, target.dir) };

                    }
                    if (!hitSuccess && UnitGrid[target.x, target.y].speed > 0)
                    {
                        //se 0 -> sw -x -y
                        //sw 1 -> nw -x +y
                        //nw 2 -> ne +x +y
                        //ne 3 -> se +x -y
                        NilTask avoid = new NilTask(() =>
                                {
                                        UnitGrid[target.x, target.y].worldX += ((UnitGrid[target.x, target.y].facingNumber) % 4 >= 2) ? 2 : -2;
                        UnitGrid[target.x, target.y].worldY += ((UnitGrid[target.x, target.y].facingNumber + 1) % 4 >= 2) ? 1 : -1;
                        });
                        Timer.instance().scheduleTask(avoid, 0, GameGDX.updateStep / 16F, 10);
                        NilTask calm = new NilTask(() =>
                                {
                                        UnitGrid[target.x, target.y].worldX -= ((UnitGrid[target.x, target.y].facingNumber) % 4 >= 2) ? 2 : -2;
                        UnitGrid[target.x, target.y].worldY -= ((UnitGrid[target.x, target.y].facingNumber + 1) % 4 >= 2) ? 1 : -1;
                        });
                        Timer.instance().scheduleTask(calm, GameGDX.updateStep, GameGDX.updateStep / 8F, 10);
                        NilTask reset = new NilTask(() =>
                                {
                                        UnitGrid[target.x, target.y].worldX = 20 + UnitGrid[target.x, target.y].x * 64 + UnitGrid[target.x, target.y].y * 64;
                        UnitGrid[target.x, target.y].worldY = 6 + UnitGrid[target.x, target.y].x * 32 - UnitGrid[target.x, target.y].y * 32;
                        });
                        Timer.instance().scheduleTask(reset, GameGDX.updateStep * 19 / 8F);

                    }
                }
                else if (TaskSteps == 2 + 1 * (Math.abs(target.x - ActiveUnit.x) + Math.abs(target.y - ActiveUnit.y)) && currentlyFiring > -1)
                {
                    if (killSuccess)
                    {
                        GameGDX.explodeTime = 0;
                        UnitGrid[target.x, target.y].visual = VisualAction.Exploding;
                        speaking.add(new Speech { x = target.x, y = target.y, large = true, text = "DEAD" });
                    }
                    else if(hitSuccess)
                    {
                        speaking.add(new Speech { x = target.x, y = target.y, large = true, text = (previousHP - UnitGrid[target.x, target.y].currentHealth) + "" });
                    }

                }
                break;
        }
    }
}
