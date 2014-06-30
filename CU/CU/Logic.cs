using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Drawing;
using System.Drawing.Imaging;
using System.Diagnostics;


namespace CU
{

    public enum MovementType
    {
        Foot, Treads, TreadsAmphi, Wheels, WheelsTraverse, Flight, Immobile
    }
    public enum Direction
    {
        SE, SW, NW, NE
    }
    public enum Mode
    {
        Selecting, Moving, Attacking
    }
    public class DirectedPosition : Position
    {
        public Direction dir { get; set; }
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
    }

    public class Unit
    {
        public int unitIndex;
        public string name;
        public int color;
        public Direction facing;
        public int facingNumber;

        public int speed;
        public MovementType mobility;
        public int x;
        public int y;

        public static string[] CurrentUnits = {
"Infantry", "Infantry_P", "Infantry_S", "Infantry_T",
"Artillery", "Artillery_P", "Artillery_S", "Artillery_T",
"Tank", "Tank_P", "Tank_S", "Tank_T",
"Plane", "Plane_P", "Plane_S", "Plane_T",
"Supply", "Supply_P", "Supply_S", "Supply_T",
"Copter", "Copter_P", "Copter_S", "Copter_T", 
"City", "Factory", "Airport", "Laboratory", "Castle", "Estate"};
        public static string[] UnitNames =
        {
            "Infantry", "Bazooka", "Bike", "Sniper",
"Light Artillery", "Defensive Artillery", "AA Artillery", "Stealth Artillery",
"Light Tank", "Heavy Tank", "AA Tank", "Recon Tank",
"Prop Plane", "Ground Bomber", "Fighter Jet", "Stealth Bomber",
"Supply Truck", "Rig", "Amphi Transport", "Jammer",
"Transport Copter", "Gunship Copter", "Blitz Copter", "Comm Copter",
"City", "Factory", "Airport", "Laboratory", "Castle", "Estate"
};
        public static Dictionary<string, int> UnitLookup = new Dictionary<string, int>(30), TerrainLookup = new Dictionary<string, int>(10), NameLookup = new Dictionary<string, int>(30);
        public static Dictionary<MovementType, List<int>> MobilityToUnits = new Dictionary<MovementType, List<int>>(30), MobilityToTerrains = new Dictionary<MovementType, List<int>>();
        public static List<int>[] TerrainToUnits = new List<int>[30];
        public static Dictionary<int, List<MovementType>> TerrainToMobilities = new Dictionary<int, List<MovementType>>();
        public static int[] CurrentSpeeds = {
3, 3, 5, 3,
4, 3, 6, 4,
6, 4, 7, 6,
7, 5, 9, 8,
5, 5, 6, 6,
7, 5, 8, 7, 
0,0,0,0,0,0};
        public static MovementType[] CurrentMobilities = {
MovementType.Foot, MovementType.Foot, MovementType.WheelsTraverse, MovementType.Foot,
MovementType.Treads, MovementType.Treads, MovementType.Treads, MovementType.Wheels,
MovementType.Treads, MovementType.Treads, MovementType.Treads, MovementType.TreadsAmphi,
MovementType.Flight, MovementType.Flight, MovementType.Flight, MovementType.Flight,
MovementType.Wheels, MovementType.Treads, MovementType.TreadsAmphi, MovementType.Wheels,
MovementType.Flight, MovementType.Flight, MovementType.Flight, MovementType.Flight, 
MovementType.Immobile, MovementType.Immobile, MovementType.Immobile, MovementType.Immobile, MovementType.Immobile, MovementType.Immobile, 
                                                         };

        static Unit()
        {
            MovementType[] values = (MovementType[])Enum.GetValues(typeof(MovementType));
            foreach (MovementType v in values)
            {
                MobilityToUnits[v] = new List<int>();
            }
            for (int t = 0; t < LocalMap.Terrains.Length; t++)
            {
                TerrainLookup[LocalMap.Terrains[t]] = t;
                TerrainToMobilities[t] = new List<MovementType>();
                TerrainToUnits[t] = new List<int>();
            }
            for (int i = 0; i < CurrentUnits.Length; i++)
            {
                UnitLookup[CurrentUnits[i]] = i;
                NameLookup[UnitNames[i]] = i;
                MobilityToUnits[CurrentMobilities[i]].Add(i);
            }
            MobilityToTerrains[MovementType.Flight] =
                new List<int>() { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            MobilityToTerrains[MovementType.Foot] =
                new List<int>() { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            MobilityToTerrains[MovementType.Treads] =
                new List<int>() { 0, 1, 2, 3, 6, 7, 8 };
            MobilityToTerrains[MovementType.TreadsAmphi] =
                new List<int>() { 0, 1, 2, 3, 6, 7, 8, 9 };
            MobilityToTerrains[MovementType.Wheels] =
                new List<int>() { 0, 2, 7, 8, };
            MobilityToTerrains[MovementType.WheelsTraverse] =
                new List<int>() { 0, 1, 2, 3, 6, 7, 8 };
            MobilityToTerrains[MovementType.Immobile] =
                new List<int>() { };

            foreach (var kv in MobilityToTerrains)
            {
                foreach (int t in kv.Value)
                {
                    TerrainToMobilities[t].Add(kv.Key);
                }
            }
            foreach (var kv in TerrainToMobilities)
            {
                foreach (MovementType m in kv.Value)
                    TerrainToUnits[kv.Key].AddRange(MobilityToUnits[m]);
                TerrainToUnits[kv.Key] = TerrainToUnits[kv.Key].Distinct().ToList();
            }
        }

        public Unit()
        {
            unitIndex = 0;
            name = "Infantry";
            speed = 3;
            mobility = MovementType.Foot;
            color = 1;
            facing = Direction.SE;
            facingNumber = 0;
            x = 3;
            y = 3;
        }
        public Unit(Unit u)
        {
            unitIndex = u.unitIndex;
            name = u.name;
            speed = u.speed;
            mobility = u.mobility;
            color = u.color;
            facing = u.facing;
            facingNumber = u.facingNumber;
            x = u.x;
            y = u.y;
        }
        public Unit(string name, int color, Direction facing, int x, int y)
        {
            this.name = name;
            this.x = x;
            this.y = y;
            //                this.unit = index_matches[unit];
            this.unitIndex = UnitLookup[name];
            this.color = color;
            this.facing = facing;
            switch (facing)
            {
                case Direction.SE: facingNumber = 0; break;
                case Direction.SW: facingNumber = 1; break;
                case Direction.NE: facingNumber = 2; break;
                case Direction.NW: facingNumber = 3; break;
                default: facingNumber = 0; break;
            }
            this.speed = CurrentSpeeds[this.unitIndex];
            this.mobility = CurrentMobilities[this.unitIndex];
        }
        public Unit(int unit, int color, Direction facing, int x, int y)
        {
            this.name = CurrentUnits[unit];
            this.x = x;
            this.y = y;
            //                this.unit = index_matches[unit];
            this.unitIndex = unit;
            this.color = color;
            this.facing = facing;
            switch (facing)
            {
                case Direction.SE: facingNumber = 0; break;
                case Direction.SW: facingNumber = 1; break;
                case Direction.NE: facingNumber = 2; break;
                case Direction.NW: facingNumber = 3; break;
                default: facingNumber = 0; break;
            }
            this.speed = CurrentSpeeds[this.unitIndex];
            this.mobility = CurrentMobilities[this.unitIndex];
        }
        public Unit(int unit, int color, int x, int y)
        {
            this.name = CurrentUnits[unit];
            this.x = x;
            this.y = y;
            //                this.unit = index_matches[unit];
            this.unitIndex = unit;
            this.color = color;
            switch (Logic.r.Next(4))
            {
                case 0: facing = Direction.SE; facingNumber = 0; break;
                case 1: facing = Direction.SW; facingNumber = 1; break;
                case 2: facing = Direction.NW; facingNumber = 2; break;
                case 3: facing = Direction.NE; facingNumber = 3; break;
            }
            this.speed = CurrentSpeeds[this.unitIndex];
            this.mobility = CurrentMobilities[this.unitIndex];
        }
        public Unit(string unit, int color, int x, int y)
        {
            this.name = unit;
            this.x = x;
            this.y = y;
            //                this.unit = index_matches[unit];
            this.unitIndex = UnitLookup[name];
            this.color = color;
            switch (Logic.r.Next(4))
            {
                case 0: facing = Direction.SE; facingNumber = 0; break;
                case 1: facing = Direction.SW; facingNumber = 1; break;
                case 2: facing = Direction.NW; facingNumber = 2; break;
                case 3: facing = Direction.NE; facingNumber = 3; break;
            }
            this.speed = CurrentSpeeds[this.unitIndex];
            this.mobility = CurrentMobilities[this.unitIndex];
        }
        public Unit(int unit, int color, int dir, int x, int y)
        {
            this.name = CurrentUnits[unit];
            this.x = x;
            this.y = y;
            //                this.unit = index_matches[unit];
            this.unitIndex = unit;
            this.color = color;
            switch (dir)
            {
                case 0: facing = Direction.SE; facingNumber = 0; break;
                case 1: facing = Direction.SW; facingNumber = 1; break;
                case 2: facing = Direction.NW; facingNumber = 2; break;
                case 3: facing = Direction.NE; facingNumber = 3; break;
                default: facing = Direction.SE; facingNumber = 0; break;
            }
            this.speed = CurrentSpeeds[this.unitIndex];
            this.mobility = CurrentMobilities[this.unitIndex];
        }
        public Unit(string unit, int color, int dir, int x, int y)
        {
            this.name = unit;
            this.x = x;
            this.y = y;
            //                this.unit = index_matches[unit];
            this.unitIndex = UnitLookup[name];
            this.color = color;
            switch (dir)
            {
                case 0: facing = Direction.SE; facingNumber = 0; break;
                case 1: facing = Direction.SW; facingNumber = 1; break;
                case 2: facing = Direction.NW; facingNumber = 2; break;
                case 3: facing = Direction.NE; facingNumber = 3; break;
                default: facing = Direction.SE; facingNumber = 0; break;
            }
            this.speed = CurrentSpeeds[this.unitIndex];
            this.mobility = CurrentMobilities[this.unitIndex];
        }
    }
    public class Logic
    {
        public Mode CurrentMode;
        public LocalMap FieldMap;
        public Unit[,] UnitGrid;
        public Unit ActiveUnit;
        public int[] Colors;
        public int[] ReverseColors;
        public int ActingFaction { get; set; }
        public int TaskSteps { get; set; }
        public int width;
        public int height;
        int[] targetX;
        int[] targetY;
        public Logic(int MapWidth, int MapHeight)
        {
            ActingFaction = 1;
            TaskSteps = 0;
            CurrentMode = Mode.Selecting;
            FieldMap = new LocalMap(MapWidth, MapHeight);
            UnitGrid = new Unit[FieldMap.Width, FieldMap.Height];

            targetX = new int[] { MapWidth / 4, MapWidth / 2, MapWidth / 4, MapWidth / 2, };
            targetY = new int[] { MapHeight / 2, MapHeight / 4, MapHeight / 2, MapHeight / 4 };
        }
        public Logic()
        {
            ActingFaction = 1;
            TaskSteps = 0;
            CurrentMode = Mode.Selecting;
            FieldMap = new LocalMap(20, 20);
            UnitGrid = new Unit[FieldMap.Width, FieldMap.Height];
        }
        public static string[] DirectionNames = { "SE", "SW", "NW", "NE" };
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
                case Direction.SE: facingNumber = 0; break;
                case Direction.SW: facingNumber = 1; break;
                case Direction.NW: facingNumber = 2; break;
                case Direction.NE: facingNumber = 3; break;
                default: facingNumber = 0; break;
            }
            return facingNumber;
        }
        static float[,] dijkstraInner(Unit self, int[,] grid, Unit[,] placing, float[,] d)
        {
            int width = d.GetLength(0);
            int height = d.GetLength(1);
            int wall = 222;
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
                    else if (d[i, j] == wall)
                    {
                        closed[new Position(i, j)] = wall;
                    }
                }
            }

            int[] ability =
            new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, };
            //plains forest desert jungle hills mountains ruins tundra road river
            Dictionary<MovementType, bool> pass = new Dictionary<MovementType, bool>
            {
                {MovementType.Foot, true},
                {MovementType.Treads, true},
                {MovementType.Wheels, true},
                {MovementType.TreadsAmphi, true},
                {MovementType.WheelsTraverse, true},
                {MovementType.Flight, true},
                {MovementType.Immobile, false},
            };
            switch (self.mobility)
            {
                case MovementType.Foot:
                    ability =
            new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, };
                    pass = new Dictionary<MovementType, bool>
            {
                {MovementType.Foot, false},
                {MovementType.Treads, true},
                {MovementType.Wheels, false},
                {MovementType.TreadsAmphi, true},
                {MovementType.WheelsTraverse, false},
                {MovementType.Flight, true},
                {MovementType.Immobile, false},
            };
                    break;
                case MovementType.Treads:
                    ability =
            new int[] { 1, 1, 1, 1, 0, 0, 1, 1, 1, 0, };
                    pass = new Dictionary<MovementType, bool>
            {
                {MovementType.Foot, false},
                {MovementType.Treads, false},
                {MovementType.Wheels, false},
                {MovementType.TreadsAmphi, false},
                {MovementType.WheelsTraverse, false},
                {MovementType.Flight, true},
                {MovementType.Immobile, false},
            };
                    break;
                case MovementType.Wheels:
                    ability =
            new int[] { 1, 0, 1, 0, 0, 0, 0, 1, 1, 0, };
                    pass = new Dictionary<MovementType, bool>
            {
                {MovementType.Foot, false},
                {MovementType.Treads, false},
                {MovementType.Wheels, false},
                {MovementType.TreadsAmphi, false},
                {MovementType.WheelsTraverse, false},
                {MovementType.Flight, true},
                {MovementType.Immobile, false},
            };
                    break;
                case MovementType.TreadsAmphi:
                    ability =
            new int[] { 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, };
                    pass = new Dictionary<MovementType, bool>
            {
                {MovementType.Foot, false},
                {MovementType.Treads, false},
                {MovementType.Wheels, false},
                {MovementType.TreadsAmphi, false},
                {MovementType.WheelsTraverse, false},
                {MovementType.Flight, true},
                {MovementType.Immobile, false},
            };
                    break;
                case MovementType.WheelsTraverse:
                    ability =
            new int[] { 1, 1, 1, 1, 0, 0, 1, 1, 1, 0, };
                    pass = new Dictionary<MovementType, bool>
            {
                {MovementType.Foot, false},
                {MovementType.Treads, false},
                {MovementType.Wheels, false},
                {MovementType.TreadsAmphi, false},
                {MovementType.WheelsTraverse, false},
                {MovementType.Flight, true},
                {MovementType.Immobile, false},
            };
                    break;
                case MovementType.Flight:
                    ability =
            new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, };
                    pass = new Dictionary<MovementType, bool>
            {
                {MovementType.Foot, true},
                {MovementType.Treads, true},
                {MovementType.Wheels, true},
                {MovementType.TreadsAmphi, true},
                {MovementType.WheelsTraverse, true},
                {MovementType.Flight, false},
                {MovementType.Immobile, false},
            };
                    break;
            }
            while (open.Count > 0)
            {
                foreach (var idx_dijk in open)
                {
                    List<Position> moves = idx_dijk.Key.Adjacent(width, height);
                    foreach (Position mov in moves)
                        if (open.ContainsKey(mov) ||
                            closed.ContainsKey(mov) ||
                            d[mov.x, mov.y] == wall ||
                            d[mov.x, mov.y] <= idx_dijk.Value + 1)
                        {

                        }
                        else if (
                        ability[grid[mov.x - 1, mov.y - 1]] == 1 &&
                          (placing[mov.x - 1, mov.y - 1] == null ||
                            (Math.Abs(self.x - (mov.x - 1)) + Math.Abs(self.y - (mov.y - 1)) < self.speed &&
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
                        d[i, j] = wall;// ((pass[placing[i - 1, j - 1].mobility]) ? 0 : wall);

                    }
                    else if (placing[i-1,j-1] != null)
                    {
                        d[i, j] += 0.5F;
                    }
                }
            }
            return d;
        }
        static float[,] dijkstra(Unit self, int[,] grid, Unit[,] placing, int targetX, int targetY)
        {

            int width = grid.GetLength(0) + 2;
            int height = grid.GetLength(1) + 2;
            float unexplored = 111;
            float goal = 0;
            int wall = 222;

            float[,] d = new float[width, height];

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
        static float[,] dijkstra(Unit self, int[,] grid, Unit[,] placing, int[] targetColors)
        {

            int width = grid.GetLength(0) + 2;
            int height = grid.GetLength(1) + 2;
            float unexplored = 111;
            float goal = 0;
            int wall = 222;

            float[,] d = new float[width, height];

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
                        if (placing[i - 1, j - 1].name == "Castle" || placing[i - 1, j - 1].name == "Estate")
                            d[i, j] = goal;
                    }
                }
            }
            d = dijkstraInner(self, grid, placing, d);

            return d;
        }

        static List<DirectedPosition> getDijkstraPath(Unit active, int[,] grid, Unit[,] placing, int targetX, int targetY)
        {
            int wall = 222;

            int width = grid.GetLength(0);
            int height = grid.GetLength(1);
            float[,] d_inv = dijkstra(active, grid, placing, ((active.color == 0) ? new int[] { 1, 2, 3, 4, 5, 6, 7 } : new int[] { 0 }));
            List<DirectedPosition> path = new List<DirectedPosition>(active.speed);
            int currentX = active.x, currentY = active.y;
            Direction currentFacing = active.facing;
            DirectedPosition oldpos = new DirectedPosition(currentX, currentY, currentFacing);
            Position newpos = new Position(currentX, currentY);
            bool isOverlapping = false;
            for (int f = 0; f < active.speed; f++)
            {
                Dictionary<Position, float> near = new Dictionary<Position, float>() { { oldpos, d_inv[currentX + 1, currentY + 1] } };
                foreach (Position pos in oldpos.Adjacent(width, height))
                {
                    if (isOverlapping && Math.Floor(d_inv[pos.x + 1, pos.y + 1]) == d_inv[pos.x + 1, pos.y + 1])
                    {
                        near[pos] = d_inv[pos.x + 1, pos.y + 1];
                    }
                    else
                    {
                        near[pos] = (float)(Math.Floor(d_inv[pos.x + 1, pos.y + 1]));
                    }
                }
                var ordered = near.OrderBy(kv => kv.Value); //.First().Key;;
                newpos = ordered.TakeWhile(kv => kv.Value == ordered.First().Value).RandomElement().Key;
                if (near.All(e => e.Value == near[newpos]))
                {
                    return new List<DirectedPosition>();
                }
                int newX = newpos.x, newY = newpos.y;
                if (!(newX == currentX && newY == currentY))
                {
                    if (newY > currentY)
                    {
                        currentFacing = Direction.SE;
                    }
                    else if (newY < currentY)
                    {
                        currentFacing = Direction.NW;

                    }
                    else
                    {
                        if (newX < currentX)
                            currentFacing = Direction.SW;
                        else
                            currentFacing = Direction.NE;
                    }

                    currentX = newX;
                    currentY = newY;
                    //                    d_inv = dijkstraInner(active, grid, placing, d_inv);
                }
                if (placing[newX, newY] == null)
                {
                    path.Add(new DirectedPosition(currentX, currentY, currentFacing));
                }
                else if (active.speed - f > 1)
                {
                    d_inv[newX, newY] = wall;
                    path.Add(new DirectedPosition(currentX, currentY, currentFacing));
                }

                oldpos = new DirectedPosition(currentX, currentY, currentFacing);

            }
            while (placing[path.Last().x, path.Last().y] != null && path.Count > 0)
            {
                path.RemoveAt(path.Count - 1);
                if (path.Count == 0)
                    return path;
                currentX = path.Last().x;
                currentY = path.Last().y;
                currentFacing = path.Last().dir;
            }
            DirectedPosition dpos = path.First();
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
            }
            return path;
        }

        private static int failCount = 0;
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
            width = FieldMap.Width;
            height = FieldMap.Height;
            //foot 0-0, treads 1-5, wheels 6-8, flight 9-10
            /*string[] unitnames = { "Infantry", //foot 0 0
                                   "Tank", "Artillery", "Artillery_P", "Artillery_S", "Supply_P", //treads 1 5
                                   "Artillery_T", "Supply", "Supply_T", //wheels 6 8
                                   "Helicopter", "Plane", //flight 9 10
                                   "City", "Factory", "Castle", "Capital" //facility
                                 };*/
            //, City, Castle, Factory, Capital

            //tilings[0].Save("flatgrass.png", ImageFormat.Png);
            /*
            for (int i = 0; i < 9; i++)
            {
                grid[i, 0] = 0;
                grid[i, 1] = 0;
                grid[i, 16] = 0;
            }
            for (int i = 1; i < 16; i++)
            {
                grid[0, i] = 0;
                grid[8, i] = 0;
            }*/

            int[] allcolors = { 1, 2, 3, 4, 5, 6, 7 };
            Colors = new int[4];
            ReverseColors = new int[8];
            bool[] taken = { false, false, false, false, false, false, false };
            for (int i = 1; i < 4; i++)
            {
                int col = r.Next(7);
                while (taken[col])
                    col = r.Next(7);
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
                FieldMap.Land[rx, ry] = 0;
                for (int i = rx - (width / 6); i < rx + (width / 6); i++)
                {
                    for (int j = ry - (height / 6); j < ry + (height / 6); j++)
                    {
                        if (UnitGrid[i, j] != null)
                            continue;
                        if (r.Next(12) <= 1 && (FieldMap.Land[i, j] == 0 || FieldMap.Land[i, j] == 1 || FieldMap.Land[i, j] == 2 || FieldMap.Land[i, j] == 4 || FieldMap.Land[i, j] == 8))
                        {
                            //
                            UnitGrid[i, j] = new Unit(r.Next(24, 28), Colors[section], i, j);
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
                FieldMap.Land[rx, ry] = 0;
                for (int i = rx - (width / 8); i < rx + (width / 8); i++)
                {
                    for (int j = ry - (height / 8); j < ry + (height / 8); j++)
                    {
                        if (UnitGrid[i, j] != null)
                            continue;
                        if (r.Next(12) <= 1 && (FieldMap.Land[i, j] == 0 || FieldMap.Land[i, j] == 1 || FieldMap.Land[i, j] == 2 || FieldMap.Land[i, j] == 4 || FieldMap.Land[i, j] == 8))
                        {
                            UnitGrid[i, j] = new Unit(r.Next(24, 28), Colors[section], i, j);
                        }

                    }
                }
            }
            List<Tuple<int, int>> guarantee = new List<Tuple<int, int>>();
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
                        if (r.Next(20) <= 1)
                        {
                            UnitGrid[i, j] = new Unit(currentUnit, Colors[section], section, i, j);
                        }

                    }
                }
                if (guarantee.Count == section)
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
                }

            }
            for (int i = 1; i < width - 1; i++)
            {
                for (int j = 1; j < height - 1; j++)
                {
                    if (r.Next(25) <= 1 && UnitGrid[i, j] == null)
                    {
                        int rs = r.Next(2);
                        int currentUnit = Unit.TerrainToUnits[FieldMap.Land[i, j]].RandomElement();
                        UnitGrid[i, j] = new Unit(currentUnit, Colors[rs], rs, i, j);

                    }
                }
            }
            Unit temp = UnitGrid.RandomFactionUnit(Colors[ActingFaction]);
            ActiveUnit = new Unit(temp);
            UnitGrid[temp.x, temp.y] = null;
        }
        public List<DirectedPosition> BestPath;
        public void ProcessStep()
        {
            switch (CurrentMode)
            {
                case Mode.Selecting:
                    if (TaskSteps > 3)
                    {
                        BestPath = getDijkstraPath(ActiveUnit, FieldMap.Land, UnitGrid, targetX[ActingFaction], targetY[ActingFaction]);
                        for (int i = 0; i < width; i++)
                        {
                            for (int j = 0; j < height; j++)
                            {
                                FieldMap.Highlight[i, j] = HighlightType.Plain;
                            }
                        }
                        TaskSteps = 0;
                        CurrentMode = Mode.Moving;
                    }
                    else
                    {
                        Effects.CenterCamera(ActiveUnit.x, ActiveUnit.y);
                        float[,] d = dijkstra(ActiveUnit, FieldMap.Land, UnitGrid, ActiveUnit.x, ActiveUnit.y);
                        for (int i = 0; i < width; i++)
                        {
                            for (int j = 0; j < height; j++)
                            {
                                FieldMap.Highlight[i, j] = (d[i + 1, j + 1] > 0 && d[i + 1, j + 1] <= ActiveUnit.speed) ? HighlightType.Bright : HighlightType.Plain;
                            }
                        }
                        FieldMap.Highlight[ActiveUnit.x, ActiveUnit.y] = HighlightType.Spectrum;
                    }
                    break;
                case Mode.Moving:
                    if (BestPath.Count <= 0 || TaskSteps > ActiveUnit.speed)
                    {
                        UnitGrid[ActiveUnit.x, ActiveUnit.y] = new Unit(ActiveUnit);
                        ActingFaction = (ActingFaction + 1) % 4;
                        Unit temp = UnitGrid.RandomFactionUnit(Colors[ActingFaction]);
                        ActiveUnit = new Unit(temp);
                        UnitGrid[temp.x, temp.y] = null;

                        CurrentMode = Mode.Selecting;

                        TaskSteps = 0;
                        break;
                    }
                    DirectedPosition node = BestPath.First();
                    ActiveUnit.x = node.x;
                    ActiveUnit.y = node.y;
                    ActiveUnit.facing = node.dir;
                    ActiveUnit.facingNumber = ConvertDirection(node.dir);
                    Effects.CenterCamera(node);

                    BestPath.RemoveAt(0);

                    break;
                case Mode.Attacking: break;
            }
            TaskSteps++;
        }
    }
}
