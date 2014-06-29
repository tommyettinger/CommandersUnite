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
            switch(facing)
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

        public LocalMap FieldMap;
        public Unit[,] UnitGrid;
        public Logic(int MapWidth, int MapHeight)
        {
            FieldMap = new LocalMap(MapWidth, MapHeight);
            UnitGrid = new Unit[FieldMap.Width, FieldMap.Height];
        }
        public Logic()
        {
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


        static float[,] dijkstra(Unit self, int[,] grid, Unit[,] placing, int goalX, int goalY)
        {
            /*
            (defn find-cells [^doubles a cell-kind]
                (persistent! (areduce ^doubles a i ret (transient {})
                                      (if (= (hiphip/aget ^doubles a i) cell-kind) (assoc! ret i cell-kind) ret))))

            (defn find-goals [^doubles a]
              (find-cells a GOAL))

            (defn find-walls [^doubles a]
                (persistent! (areduce ^doubles a i ret (transient {})
                                      (if (>= (hiphip/aget ^doubles a i) (double wall)) (assoc! ret i wall) ret))))

            (defn find-floors [^doubles a]
              (find-cells a floor))

            (defn find-lowest [^doubles a]
              (let [low-val (hiphip/amin a)]
                (find-cells a low-val)))

            (defn find-monsters [m]
                (into {} (for [mp (map #(:pos @%) m)] [mp 1.0])))

            (defn dijkstra
              ([a]
                 (dijkstra a (find-walls a) (find-lowest a)))
              ([dun _]
                 (dijkstra (:dungeon dun) (merge (find-walls (:dungeon dun)) (find-monsters @(:monsters dun))) (find-lowest (:dungeon dun))))
              ([a closed open-cells]
                 (loop [open open-cells]
                   (when (seq open)
                     (recur (reduce (fn [newly-open [^long i ^double v]]
                                      (reduce (fn [acc dir]
                                                (if (or (closed dir) (open dir)
                                                        (>= (+ 1.0 v) (hiphip/aget ^doubles a dir)))
                                                  acc
                                                  (do (hiphip/aset ^doubles a dir (+ 1.0 v))
                                                      (assoc acc dir (+ 1.0 v)))))
                                              newly-open, [(- i wide2)
                                                           (+ i wide2)
                                                           (- i 2)
                                                           (+ i 2)]))
                                    {}, open))))
                 a))*/
            int width = grid.GetLength(0);
            int height = grid.GetLength(1);
            float wall = 222;
            float unexplored = 111;
            float goal = 0;
            float[] d = new float[width * height];
            for (int i = 0; i < width; i++)
            {
                for (int j = 0; j < height; j++)
                    d[i + width * j] = unexplored;
            }
            for (int i = 0; i < width; i++)
            {
                d[i] = wall;
                d[i + width * (height - 1)] = wall;
            }
            for (int j = 1; j < height - 1; j += 2)
            {
                d[j * width] = wall;
                d[(j + 1) * width + (width - 1)] = wall;
            }
            d[goalX + width * goalY] = goal;
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
            Dictionary<int, int> open = new Dictionary<int, int> { { (goalX + width * goalY), 0 } },
                fringe = new Dictionary<int, int>(),
                closed = new Dictionary<int, int>();
            int[] moves = { width, -1, -width, 1 };
            while (open.Count > 0)
            {
                foreach (var idx_dijk in open)
                {
                    foreach (int mov in moves)
                        if (open.ContainsKey(idx_dijk.Key + mov) ||
                            closed.ContainsKey(idx_dijk.Key + mov) ||
                            d[idx_dijk.Key + mov] == wall ||
                            d[idx_dijk.Key + mov] <= idx_dijk.Value + 1)
                        {

                        }
                        else if (
                            ability[grid[(idx_dijk.Key + mov) % width, (idx_dijk.Key + mov) / width]] == 1 &&
                            (placing[(idx_dijk.Key + mov) % width, (idx_dijk.Key + mov) / width] == null ||
                            pass[placing[(idx_dijk.Key + mov) % width, (idx_dijk.Key + mov) / width].mobility] ||
                            (placing[(idx_dijk.Key + mov) % width, (idx_dijk.Key + mov) / width].color == self.color &&
                            placing[(idx_dijk.Key + mov) % width, (idx_dijk.Key + mov) / width].mobility != MovementType.Immobile)
                            ))
                        {
                            fringe[idx_dijk.Key + mov] = (idx_dijk.Value + 1);
                            d[idx_dijk.Key + mov] = idx_dijk.Value + 1;
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
            d[goalX + width * goalY] = wall;
            float[,] n = new float[width, height];

            for (int j = 0; j < height; j++)
            {
                for (int i = 0; i < width; i++)
                {
                    n[i, j] = d[i + j * width];
                    //                    Console.Write(string.Format("{0,4}", n[i, j]));
                }
                Console.WriteLine();
            }
            return n;
        }
        static List<DirectedPosition> getDijkstraPath(Unit active, int[,] grid, Unit[,] placing, int targetX, int targetY)
        {
            int width = grid.GetLength(0);
            float[,] d_inv = dijkstra(active, grid, placing, targetX, targetY);
            List<DirectedPosition> path = new List<DirectedPosition>();
            int currentX = active.x, currentY = active.y;
            Direction currentFacing = active.facing;

            for (int f = 0; f < active.speed; f++)
            {
                Dictionary<int, float> near = new Dictionary<int, float>();
                near[currentX + width * (currentY)] = d_inv[currentX, currentY];
                near[currentX + width * (currentY + 1)] = d_inv[currentX, currentY + 1];
                near[currentX + width * (currentY - 1)] = d_inv[currentX, currentY - 1];
                if (currentY % 2 == 0)
                {
                    near[currentX + 1 + width * (currentY + 1)] = d_inv[currentX + 1, currentY + 1];
                    near[currentX + 1 + width * (currentY - 1)] = d_inv[currentX + 1, currentY - 1];
                }
                else
                {
                    near[currentX - 1 + width * (currentY + 1)] = d_inv[currentX - 1, currentY + 1];
                    near[currentX - 1 + width * (currentY - 1)] = d_inv[currentX - 1, currentY - 1];
                }
                int newpos = near.OrderBy(kv => kv.Value).First().Key;
                if (near.All(e => e.Value == near[newpos]))
                {
                    return new List<DirectedPosition>();
                }
                int newX = newpos % width, newY = newpos / width;
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
                            currentFacing = Direction.NE;
                        else
                            currentFacing = Direction.SW;
                    }

                    currentX = newX;
                    currentY = newY;
                }
                path.Add(new DirectedPosition(currentX, currentY, currentFacing));
            }
            while (path.Count > 0 && placing[path.Last().x, path.Last().y] != null)
            {
                path.RemoveAt(path.Count - 1);
            }
            return path;
        }

        private static int failCount = 0;
        void retryGamePreview()
        {
            failCount++;
            Console.WriteLine("\n\n!!!!! P L A C E M E N T   F A I L U R E   " + failCount + " !!!!!\n\n");
            if (failCount > 10)
            {
                Console.WriteLine("Too many placement failures.");
                Console.In.ReadLine();
                return;
            }
            PlaceUnits();
        }
        public void PlaceUnits()
        {
            int width = FieldMap.Width;
            int height = FieldMap.Height;
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

            int[] allcolors = { 1, 2, 3, 4, 5, 6, 7 }, colors = new int[4];
            bool[] taken = { false, false, false, false, false, false, false };
            for (int i = 1; i < 4; i++)
            {
                int col = r.Next(7);
                while (taken[col])
                    col = r.Next(7);
                colors[i] = allcolors[col];
                taken[col] = true;
            }
            colors[0] = 0;

            int[] targetX = { width / 4, width / 2, width / 4, width / 2, },
                  targetY = { height / 2, height / 4, height / 2, height / 4 };
            for (int section = 0; section < 2; section++)
            {
                int rx = (width / 4) + (width / 2) * (section % 2);
                int ry = 3 + (height / 6);
                //processSingleOutlined(facilityps[(colors[section] == 0) ? 3 : 2], colors[section], dirs[r.Next(4)])
                if (colors[section] == 0)
                {
                    UnitGrid[rx, ry] = new Unit("Estate", colors[section], rx, ry);
                    targetX[1] = rx;
                    targetY[1] = ry;
                    targetX[2] = rx;
                    targetY[2] = ry;
                    targetX[3] = rx;
                    targetY[3] = ry;
                }
                else
                {
                    UnitGrid[rx, ry] = new Unit("Castle", colors[section], rx, ry);
                    targetX[0] = rx;
                    targetY[0] = ry;
                }
                FieldMap.Land[rx, ry] = 0;
                for (int i = rx - (width / 8); i < rx + (width / 8); i++)
                {
                    for (int j = ry - (height / 8); j < ry + (height / 8); j++)
                    {
                        if (UnitGrid[i, j] != null)
                            continue;
                        if (r.Next(9) <= 1 && (FieldMap.Land[i, j] == 0 || FieldMap.Land[i, j] == 1 || FieldMap.Land[i, j] == 2 || FieldMap.Land[i, j] == 4 || FieldMap.Land[i, j] == 8))
                        {
                            //
                            UnitGrid[i, j] = new Unit(r.Next(24, 28), colors[section], i, j);
                            //processSingleOutlined(facilityps[r.Next(3) % 2], colors[section], dirs[r.Next(4)]);
                        }

                    }
                }
            }
            for (int section = 2; section < 4; section++)
            {
                int rx = (width / 4) + (width / 2) * (section % 2);
                int ry = height - 3 - (height / 6);
                UnitGrid[rx, ry] = new Unit(((colors[section] == 0) ? Unit.UnitLookup["Estate"] : Unit.UnitLookup["Castle"]), colors[section], rx, ry);
                FieldMap.Land[rx, ry] = 0;
                for (int i = rx - (width / 8); i < rx + (width / 8); i++)
                {
                    for (int j = ry - (height / 8); j < ry + (height / 8); j++)
                    {
                        if (UnitGrid[i, j] != null)
                            continue;
                        if (r.Next(9) <= 1 && (FieldMap.Land[i, j] == 0 || FieldMap.Land[i, j] == 1 || FieldMap.Land[i, j] == 2 || FieldMap.Land[i, j] == 4 || FieldMap.Land[i, j] == 8))
                        {
                            UnitGrid[i, j] = new Unit(r.Next(24, 28), colors[section], i, j);
                        }

                    }
                }
            }
            List<Tuple<int, int>> guarantee = new List<Tuple<int, int>>();
            for (int section = 0; section < 4; section++) // section < 4
            {
                for (int i = 2 + (width / 2) * (section % 2); i < (width / 2) - 2 + (width / 2) * (section % 2); i++)
                {
                    for (int j = (section / 2 == 0) ? 3 : height / 2 + 2; j < ((section / 2 == 0) ? height / 2 - 2 : height - 3); j++)
                    {
                        if (UnitGrid[i, j] != null)
                            continue;
                        int currentUnit = Unit.TerrainToUnits[FieldMap.Land[i, j]].RandomElement();
                        //foot 0-0, treads 1-5, wheels 6-8, flight 9-10
                        if (r.Next(16) <= 3)
                        {
                            UnitGrid[i, j] = new Unit(currentUnit, colors[section], section, i, j);
                        }

                    }
                }
                if (guarantee.Count == section)
                {
                    int rgx = r.Next(2 + (width / 2) * (section % 2), (width / 2) - 2 + (width / 2) * (section % 2));
                    int rgy = r.Next((section / 2 == 0) ? 3 : height / 2 + 2, ((section / 2 == 0) ? height / 2 - 2 : height - 3));
                    int problems = 0;
                    while (UnitGrid[rgx, rgy] != null)
                    {
                        rgx = r.Next(2 + (width / 2) * (section % 2), (width / 2) - 2 + (width / 2) * (section % 2));
                        rgy = r.Next((section / 2 == 0) ? 3 : height / 2 + 2, ((section / 2 == 0) ? height / 2 - 2 : height - 3));
                        if (UnitGrid[rgx, rgy] != null)
                            problems++;
                        if (problems > 10)
                        {
                            retryGamePreview();
                            return;
                        }
                    }
                    UnitGrid[rgx, rgy] = new Unit(Unit.TerrainToUnits[FieldMap.Land[rgx, rgy]].RandomElement(), colors[section], section, rgx, rgy);
                }

            }
            for (int i = 1; i < width - 1; i++)
            {
                for (int j = 3; j < height - 3; j++)
                {
                    if (r.Next(22) <= 2 && UnitGrid[i, j] == null)
                    {
                        int rs = r.Next(2);
                        int currentUnit = Unit.TerrainToUnits[FieldMap.Land[i, j]].RandomElement();
                        UnitGrid[i, j] = new Unit(currentUnit, colors[rs], rs, i, j);

                    }
                }
            }
        }
    }
}
