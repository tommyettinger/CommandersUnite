﻿using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Drawing;
using System.Drawing.Imaging;
using System.Threading;
using com.badlogic.gdx;
namespace CU
{

    public enum MovementType
    {
        Foot, Treads, TreadsAmphi, Wheels, WheelsTraverse, Flight, FlightFlyby, Immobile
    }
    public enum Direction
    {
        SE, SW, NW, NE
    }
    public enum Mode
    {
        Selecting, Moving, Attacking
    }
    public enum VisualAction
    {
        Normal, Exploding, Firing
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

        public new static List<DirectedPosition> Adjacent(int x, int y, int width, int height)
        {
            List<DirectedPosition> l = new List<DirectedPosition>();
            if (x > 0)
                l.Add(new DirectedPosition(x - 1, y, Direction.NE));
            if (y > 0)
                l.Add(new DirectedPosition(x, y - 1, Direction.SE));
            if (x < width - 1)
                l.Add(new DirectedPosition(x + 1, y, Direction.SW));
            if (y < height - 1)
                l.Add(new DirectedPosition(x, y + 1, Direction.NW));
            return l;
        }
    }
    public enum UnitType
    {
        Personnel, Armored, Vehicle, Plane, Helicopter
    }
    public enum WeaponType
    {
        LightGun, HeavyGun, Cannon, Missile, None
    }
    public struct Weapon
    {
        public WeaponType kind;
        public int damage;
        public float[] multipliers;
        public int minRange;
        public int maxRange;
        public bool moveAndAttack;
        public bool ring;
        public bool alert;
        public bool seek;
        
        public Weapon(WeaponType kind, int damage, float[] multipliers, string specialQualities)
        {
            this.kind = kind;
            this.damage = damage;
            this.multipliers = multipliers;
            minRange = 1;
            maxRange = 1;
            moveAndAttack = true;
            ring = false;
            alert = false;
            seek = false;
            if(specialQualities != "")
            switch(specialQualities.Split(new char[]{' '}, 1)[0])
            {
                case "Alert": alert = true; break;
                case "Seek": seek = true; break;
                case "Ring": ring = true; break;
                case "Indirect": moveAndAttack = false;
                    minRange = int.Parse(specialQualities.Split(new char[] { ' ' }, 2)[1].Substring(0, 1));
                    maxRange = int.Parse(specialQualities.Split(new char[] { ' ' }, 2)[1].Substring(2, 1));
                    break;
            }
        }
    }
    public class Unit
    {
        public int unitIndex;
        public string name;
        public int color;

        public MovementType mobility;
        public Direction facing;
        public int facingNumber;
        public int speed;
        public int maxHealth;
        public int currentHealth;
        public int armor;
        public int dodge;
        public Weapon[] weaponry;
        public UnitType kind;

        public List<DirectedPosition> targetingAbove, targetingBelow;
        public VisualAction visual;
        public int x;
        public int y;
        public float worldX;
        public float worldY;


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
        public string[] UnitTypes = 
        {
            "Personnel", "Armored", "Vehicle", "Plane", "Helicopter"
        };
        public static int[][] WeaponDisplays = {
new int[] {1, -1}, new int[] {0, 5}, new int[] {1, -1}, new int[] {0, 0},
new int[] {-1, 4}, new int[] {3, -1}, new int[] {-1, 6}, new int[] {-1, 6},
new int[] {3, 1}, new int[] {3, 1}, new int[] {1, -1}, new int[] {1, 3},
new int[] {1, -1}, new int[] {-1, 7}, new int[] {5, -1}, new int[] {5, -1},
new int[] {-1, -1}, new int[] {-1, -1}, new int[] {-1, -1}, new int[] {-1, -1},
new int[] {-1, -1}, new int[] {1, 5}, new int[] {1, -1}, new int[] {-1, -1},
new int[] {-1, -1}, new int[] {-1, -1}, new int[] {-1, -1}, new int[] {-1, -1}, new int[] {-1, -1}, new int[] {-1, -1}
};
        public static Weapon[][] Weapons = new Weapon[][] {
 
new Weapon[]{new Weapon(WeaponType.LightGun, 10, new float[]{1, 0.5f, 0.5f, 0, 0.5f}, ""), new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), },
new Weapon[]{new Weapon(WeaponType.LightGun, 10, new float[]{1, 0.5f, 0.5f, 0, 0.5f}, ""), new Weapon(WeaponType.Missile, 13, new float[]{0.5f, 2, 2, 0, 0.5f}, ""), },
new Weapon[]{new Weapon(WeaponType.LightGun, 10, new float[]{1, 0.5f, 0.5f, 0, 0.5f}, "Alert"), new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), },
new Weapon[]{new Weapon(WeaponType.LightGun, 10, new float[]{1, 0.5f, 0.5f, 0, 0.5f}, ""), new Weapon(WeaponType.HeavyGun, 13, new float[]{2, 0.5f, 1, 0, 0.5f}, "Indirect 1-2"), },

new Weapon[]{new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), new Weapon(WeaponType.Cannon, 13, new float[]{1, 2, 2, 0, 0}, "Indirect 2-4"), },
new Weapon[]{new Weapon(WeaponType.Cannon, 16, new float[]{1, 2, 2, 0, 0}, "Indirect 1-3"), new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), },
new Weapon[]{new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), new Weapon(WeaponType.Missile, 13, new float[]{0, 0.5f, 0.5f, 2, 2}, "Indirect 2-6"), },
new Weapon[]{new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), new Weapon(WeaponType.Missile, 19, new float[]{1, 2, 2, 0, 0}, "Indirect 3-7"), },

new Weapon[]{new Weapon(WeaponType.Cannon, 16, new float[]{0.5f, 2, 2, 0, 0}, ""), new Weapon(WeaponType.LightGun, 13, new float[]{1.5f, 1, 1, 0, 1}, ""), },
new Weapon[]{new Weapon(WeaponType.Cannon, 19, new float[]{0.5f, 2, 2, 0, 0}, ""), new Weapon(WeaponType.HeavyGun, 16, new float[]{0.5f, 1, 1, 0, 1.5f}, ""), },
new Weapon[]{new Weapon(WeaponType.HeavyGun, 16, new float[]{2, 0.5f, 1, 2, 2}, ""), new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), },
new Weapon[]{new Weapon(WeaponType.LightGun, 13, new float[]{2, 1, 1, 0, 1}, "Seek"), new Weapon(WeaponType.Cannon, 16, new float[]{0.5f, 2, 2, 0, 0}, ""), },

new Weapon[]{new Weapon(WeaponType.LightGun, 16, new float[]{1.5f, 0.5f, 1, 0.5f, 1.5f}, "Alert"), new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), },
new Weapon[]{new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), new Weapon(WeaponType.Missile, 25, new float[]{1, 2, 2, 0, 0}, "Ring"), },
new Weapon[]{new Weapon(WeaponType.Missile, 25, new float[]{0, 0, 0, 2.5f, 2.5f}, ""), new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), },
new Weapon[]{new Weapon(WeaponType.Missile, 16, new float[]{0.5f, 1.5f, 1.5f, 0.5f, 1}, "Seek"), new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), },

new Weapon[]{new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), },
new Weapon[]{new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), },
new Weapon[]{new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), },
new Weapon[]{new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), },

new Weapon[]{new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), },
new Weapon[]{new Weapon(WeaponType.HeavyGun, 16, new float[]{1, 1, 1.5f, 0, 1}, ""), new Weapon(WeaponType.Missile, 16, new float[]{0, 1.5f, 1.5f, 0, 0.5f}, ""), },
new Weapon[]{new Weapon(WeaponType.LightGun, 13, new float[]{1.5f, 1, 1, 0, 1.5f}, ""), new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), },
new Weapon[]{new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""),},

new Weapon[]{new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""),},
new Weapon[]{new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""),},
new Weapon[]{new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""),},
new Weapon[]{new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""),},
new Weapon[]{new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""),},
new Weapon[]{new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""),},
};
        public static Dictionary<string, int> UnitLookup = new Dictionary<string, int>(30), TerrainLookup = new Dictionary<string, int>(10), NameLookup = new Dictionary<string, int>(30);
        public static Dictionary<MovementType, List<int>> MobilityToUnits = new Dictionary<MovementType, List<int>>(30), MobilityToTerrains = new Dictionary<MovementType, List<int>>();
        public static List<int>[] TerrainToUnits = new List<int>[30];
        public static Dictionary<int, List<MovementType>> TerrainToMobilities = new Dictionary<int, List<MovementType>>();
        public static int[] AllSpeeds = {
3, 3, 5, 3,
4, 3, 6, 4,
6, 4, 7, 6,
7, 5, 9, 8,
5, 5, 6, 6,
7, 5, 8, 7, 
0, 0, 0, 0, 0, 0,
};
        public static int[] AllArmors = {
1, 2, 1, 0, 
2, 3, 2, 1, 
2, 3, 2, 1, 
0, 2, 0, 0, 
2, 4, 3, 2, 
1, 2, 0, 1, 
3, 3, 2, 2, 4, 4,
};
        public static int[] AllDodges = {
2, 2, 3, 4, 
2, 1, 3, 3, 
0, 0, 1, 2, 
3, 2, 3, 4, 
1, 0, 1, 3, 
2, 1, 3, 4, 
0, 0, 0, 0, 0, 0,
};
        public static int[] AllHealths = {
20, 25, 20, 20, 
25, 40, 25, 25, 
30, 50, 35, 30, 
30, 45, 35, 30, 
25, 40, 35, 30, 
25, 30, 25, 25, 
70, 80, 70, 70, 90, 90};
        public static MovementType[] AllMobilities = {
MovementType.Foot, MovementType.Foot, MovementType.WheelsTraverse, MovementType.Foot,
MovementType.Treads, MovementType.Treads, MovementType.Treads, MovementType.Wheels,
MovementType.Treads, MovementType.Treads, MovementType.Treads, MovementType.TreadsAmphi,
MovementType.Flight, MovementType.Flight, MovementType.Flight, MovementType.FlightFlyby,
MovementType.Wheels, MovementType.Treads, MovementType.TreadsAmphi, MovementType.Wheels,
MovementType.Flight, MovementType.Flight, MovementType.Flight, MovementType.FlightFlyby, 
MovementType.Immobile, MovementType.Immobile, MovementType.Immobile, MovementType.Immobile, MovementType.Immobile, MovementType.Immobile, 
                                                     };
        public static UnitType[] AllUnitTypes = {
UnitType.Personnel, UnitType.Personnel, UnitType.Personnel, UnitType.Personnel,
UnitType.Vehicle, UnitType.Armored, UnitType.Vehicle, UnitType.Vehicle,
UnitType.Armored, UnitType.Armored, UnitType.Armored, UnitType.Armored,
UnitType.Plane, UnitType.Plane, UnitType.Plane, UnitType.Plane,
UnitType.Vehicle, UnitType.Vehicle, UnitType.Vehicle, UnitType.Vehicle,
UnitType.Helicopter,UnitType.Helicopter,UnitType.Helicopter,UnitType.Helicopter, 
UnitType.Armored,UnitType.Armored,UnitType.Armored,UnitType.Armored,UnitType.Armored,UnitType.Armored,
                                                         };
        public static int UnitTypeAsNumber(UnitType ut)
        {
            switch(ut)
            {
                case UnitType.Personnel: return 0;
                case UnitType.Armored: return 1;
                case UnitType.Vehicle: return 2;
                case UnitType.Plane: return 3;
                case UnitType.Helicopter: return 4;
            }
            return 1;
        }
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
                MobilityToUnits[AllMobilities[i]].Add(i);
            }
            MobilityToTerrains[MovementType.Flight] =
                new List<int>() { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            MobilityToTerrains[MovementType.FlightFlyby] =
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

            maxHealth = AllHealths[unitIndex];
            currentHealth = maxHealth;
            armor = AllArmors[unitIndex];
            dodge = AllDodges[unitIndex];
            weaponry = Weapons[unitIndex];
            kind = UnitType.Personnel;
            speed = 3;
            mobility = MovementType.Foot;
            color = 1;
            facing = Direction.SE;
            facingNumber = 0;
            x = 3;
            y = 3;
            worldX = 20 + x * 64 + y * 64;
            worldY = 6 + x * 32 - y * 32;
        }
        public Unit(Unit u)
        {
            unitIndex = u.unitIndex;
            name = u.name;

            maxHealth = u.maxHealth;
            currentHealth = u.currentHealth;
            armor = u.armor;
            dodge = u.dodge;
            weaponry = u.weaponry;
            kind = u.kind;
            speed = u.speed;
            mobility = u.mobility;
            color = u.color;
            facing = u.facing;
            facingNumber = u.facingNumber;
            x = u.x;
            y = u.y;
            worldX = 20 + x * 64 + y * 64;
            worldY = 6 + x * 32 - y * 32;
            targetingAbove = new List<DirectedPosition>();
            targetingBelow = new List<DirectedPosition>();
            visual = VisualAction.Normal;
        }
        public Unit(int unit, int color, int x, int y)
        {
            this.name = CurrentUnits[unit];
            this.x = x;
            this.y = y;

            worldX = 20 + x * 64 + y * 64;
            worldY = 6 + x * 32 - y * 32;

            this.unitIndex = unit;
            this.color = color;
            switch (Logic.r.Next(4))
            {
                case 0: facing = Direction.SE; facingNumber = 0; break;
                case 1: facing = Direction.SW; facingNumber = 1; break;
                case 2: facing = Direction.NW; facingNumber = 2; break;
                case 3: facing = Direction.NE; facingNumber = 3; break;
            }
            this.speed = AllSpeeds[this.unitIndex];
            this.mobility = AllMobilities[this.unitIndex];
            this.kind = AllUnitTypes[this.unitIndex];
            this.maxHealth = AllHealths[this.unitIndex];
            this.currentHealth = this.maxHealth;
            this.armor = AllArmors[this.unitIndex];
            this.dodge = AllDodges[this.unitIndex];
            this.weaponry = Weapons[this.unitIndex];

            visual = VisualAction.Normal;
        }
        public Unit(string unit, int color, int x, int y)
        {
            this.name = unit;
            this.x = x;
            this.y = y;

            worldX = 20 + x * 64 + y * 64;
            worldY = 6 + x * 32 - y * 32;
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
            this.speed = AllSpeeds[this.unitIndex];
            this.mobility = AllMobilities[this.unitIndex];
            this.kind = AllUnitTypes[this.unitIndex];
            this.maxHealth = AllHealths[this.unitIndex];
            this.currentHealth = this.maxHealth;
            this.armor = AllArmors[this.unitIndex];
            this.dodge = AllDodges[this.unitIndex];
            this.weaponry = Weapons[this.unitIndex];

            visual = VisualAction.Normal;
        }
        public Unit(int unit, int color, int dir, int x, int y)
        {
            this.name = CurrentUnits[unit];
            this.x = x;
            this.y = y;

            worldX = 20 + x * 64 + y * 64;
            worldY = 6 + x * 32 - y * 32;
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
            this.speed = AllSpeeds[this.unitIndex];
            this.mobility = AllMobilities[this.unitIndex];
            this.kind = AllUnitTypes[this.unitIndex];
            this.maxHealth = AllHealths[this.unitIndex];
            this.currentHealth = this.maxHealth;
            this.armor = AllArmors[this.unitIndex];
            this.dodge = AllDodges[this.unitIndex];
            this.weaponry = Weapons[this.unitIndex];

            visual = VisualAction.Normal;
        }
        public bool isOpposed(Unit u)
        {
            if (color == 0)
                return u.color != 0;
            else
                return u.color == 0;
        }
        public bool attemptDodge(Weapon attacker)
        {
            if (Logic.r.Next(10)+1 > dodge)
            {
                return true;
            }
            return false;
        }
        public bool takeDamage(Weapon attacker)
        {
            return takeDamage(attacker.damage + Logic.r.Next(attacker.damage) / 2f - attacker.damage / 4f, attacker.multipliers[UnitTypeAsNumber(kind)]);
        }
        public bool takeDamage(float amount, float multiplier)
        {
            currentHealth -= (int)Math.Round(amount * (multiplier - 0.1f * armor));
            if (currentHealth <= 0)
            {
                currentHealth = 0;
                return true;
            }
            return false;
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
        float[,] DijkstraAttackPositions(int minRange, int maxRange, MovementType mobility, int selfColor, int[] targetColors, int[,] grid, Unit[,] placing, float[,] d)
        {
            int width = d.GetLength(0);
            int height = d.GetLength(1);
            int wall = 2222;
            int goal = 0;

            Dictionary<Position, float> open = new Dictionary<Position, float>(),
                fringe = new Dictionary<Position, float>(),
                closed = new Dictionary<Position, float>();

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
            switch (mobility)
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


            for (int i = 1; i < width - 1; i++)
            {
                for (int j = 1; j < height - 1; j++)
                {
                    if (targetColors.Any(c => placing[i - 1, j - 1] != null && c == placing[i - 1, j - 1].color))
                    {
                        foreach (Position p in Position.WithinRange(i, j, 1, 1, width - 1, height - 1, minRange, maxRange))
                        {
                            if (ability[grid[p.x - 1, p.y - 1]] == 1 && placing[p.x - 1, p.y - 1] == null)
                            {
                                d[p.x, p.y] = goal;
                                open[p] = goal;
                            }
                        }
                    }
                    else if (d[i, j] >= wall)
                    {
                        closed[new Position(i, j)] = wall;
                    }
                }
            }


            while (open.Count > 0)
            {
                foreach (var idx_dijk in open)
                {
                    List<Position> moves = idx_dijk.Key.Adjacent(width, height);
                    foreach (Position mov in moves)
                        if (open.ContainsKey(mov) ||
                            closed.ContainsKey(mov) ||
                            d[mov.x, mov.y] >= wall ||
                            d[mov.x, mov.y] <= idx_dijk.Value + 1)
                        {

                        }
                        else if (
                        ability[grid[mov.x - 1, mov.y - 1]] == 1
                             && placing[mov.x - 1, mov.y - 1] == null)
                        {
                            fringe[mov] = (idx_dijk.Value + 1);
                            d[mov.x, mov.y] = idx_dijk.Value + 1;
                        }
                        else if (
                        ability[grid[mov.x - 1, mov.y - 1]] == 1 &&
                          (placing[mov.x - 1, mov.y - 1] != null &&
                              (pass[placing[mov.x - 1, mov.y - 1].mobility] ||
                                (placing[mov.x - 1, mov.y - 1].color == selfColor &&
                                 placing[mov.x - 1, mov.y - 1].mobility != MovementType.Immobile)
                            )))
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

        List<Position> bestMoves = new List<Position>();
        float[,] ViableMoves(Unit self, int[,] grid, Unit[,] placing)
        {
            //Gdx.app.log("CommandersUnite", "Unit is at: " + self.x + ", " + self.y);
            //Gdx.app.log("CommandersUnite", "Moves are: ");
            gradient = SmartDijkstra(self, grid, placing, ((self.color == 0) ? new int[] { 1, 2, 3, 4, 5, 6, 7 } : new int[] { 0 }));
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
                //Gdx.app.log("CU", lg.ToString());
            }*/
            int width = gradient.GetLength(0);
            int height = gradient.GetLength(1);
            int wall = 2222;
            int goal = 0;
            float unexplored = 1111;

            List<Position> best = new List<Position> { new Position(self.x, self.y) };
            Dictionary<Position, float>
                open = new Dictionary<Position, float> { { new Position(self.x +1, self.y +1), goal } },
                fringe = new Dictionary<Position, float>(),
                closed = new Dictionary<Position, float>();
            

            float[,] radiate = new float[width, height];

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
                    if (radiate[i, j] >= wall)
                    {
                        closed[new Position(i, j)] = wall;
                    }
                }
            }
            radiate[self.x+1, self.y+1] = goal;
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
                while (open.Count > 0 && furthest <= self.speed)
                {
                    foreach (var idx_dijk in open)
                    {
                        List<Position> moves = idx_dijk.Key.Adjacent(width, height);
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
                                    best.Clear();
                                    best.Add(new Position { x = mov.x - 1, y = mov.y - 1 });
                                    lowest = gradient[mov.x, mov.y];
                                    furthest = idx_dijk.Value + 1;
                                }
                                //else if (d[mov.x, mov.y] == lowest)
                                //{
                                //    best.Add(new Position { x = mov.x - 1, y = mov.y - 1 });
                                //}
                            }
                            else if (
                            ability[grid[mov.x - 1, mov.y - 1]] == 1 &&
                              (placing[mov.x - 1, mov.y - 1] != null &&
                                (Math.Abs(self.x - (mov.x - 1)) + Math.Abs(self.y - (mov.y - 1)) < self.speed &&
                                  (pass[placing[mov.x - 1, mov.y - 1].mobility] ||
                                    (placing[mov.x - 1, mov.y - 1].color == self.color &&
                                     placing[mov.x - 1, mov.y - 1].mobility != MovementType.Immobile)
                                ))))
                            {
                                radiate[mov.x, mov.y] = idx_dijk.Value + 1;
                                fringe[mov] = (idx_dijk.Value + 1);
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
            }
            for (int i = 0; i < width; i++ )
            {
                for(int j = 0; j < height; j++)
                {
                    if (gradient[i, j] <= goal)
                        radiate[i, j] = wall;
                }
            }
            bestMoves = best;
            
            return radiate;
        }

        float[,] dijkstraInner(Unit self, int[,] grid, Unit[,] placing, float[,] d)
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
                    List<Position> moves = idx_dijk.Key.Adjacent(width, height);
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

        float[,] dijkstra(Unit self, int[,] grid, Unit[,] placing, int targetX, int targetY)
        {

            int width = grid.GetLength(0) + 2;
            int height = grid.GetLength(1) + 2;
            float unexplored = 1111;
            float goal = 0;
            int wall = 2222;

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
        float[,] dijkstra(Unit self, int[,] grid, Unit[,] placing, int[] targetColors)
        {

            int width = grid.GetLength(0) + 2;
            int height = grid.GetLength(1) + 2;
            float unexplored = 1111;
            float goal = 0;
            int wall = 2222;

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
                        //if (placing[i - 1, j - 1].name == "Castle" || placing[i - 1, j - 1].name == "Estate")
                        d[i, j] = goal;
                    }
                }
            }
            d = dijkstraInner(self, grid, placing, d);

            return d;
        }
        float[,] SmartDijkstra(Unit self, int[,] grid, Unit[,] placing, int[] targetColors)
        {

            int width = grid.GetLength(0) + 2;
            int height = grid.GetLength(1) + 2;
            float unexplored = 1111;
            int wall = 2222;

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
            d = DijkstraAttackPositions(1, 1, self.mobility, self.color, targetColors, grid, placing, d);

            return d;
        }
        public float[,] gradient = new float[27,27];
        List<DirectedPosition> getDijkstraPath(Unit active, int[,] grid, Unit[,] placing, int targetX, int targetY)
        {
            int width = grid.GetLength(0);
            int height = grid.GetLength(1);
            List<DirectedPosition> path = new List<DirectedPosition>();
            int currentX = active.x, currentY = active.y;
            Direction currentFacing = active.facing;
            Position newpos = new Position(currentX, currentY);
            float[,] rad = ViableMoves(active, grid, placing);
            Position best = bestMoves.RandomElement();
            /*if (best.x == active.x && best.y == active.y)// && ((0 == placing[newX, newY].color) ? 0 != active.color : 0 == active.color))
            {
                return new List<DirectedPosition> { }; //new DirectedPosition {x=active.x, y=active.y, dir= active.facing }
            }*/
            //Gdx.app.log("CommandersUnite", "Best is: " + best.x + ", " + best.y);
            //Gdx.app.log("CommandersUnite", "Distance is: " + (rad[best.x+1, best.y+1]));
            /*
            foreach (Position p in bestMoves)
            {
                Gdx.app.log("CommandersUnite", "    " + p.x + ", " + p.y + " with an occupant of " + ((placing[p.x, p.y] != null) ? placing[p.x, p.y].name : "EMPTY"));
            }*/
            DirectedPosition oldpos = new DirectedPosition(best.x, best.y);
            path.Add(new DirectedPosition(best.x, best.y));
            for (int f = 0; f <= active.speed; f++)
            {
                Dictionary<Position, float> near = new Dictionary<Position, float>() { { oldpos, rad[oldpos.x +1, oldpos.y+1] } };
                foreach (Position pos in oldpos.Adjacent(width, height))
                        near[pos] = rad[pos.x + 1, pos.y + 1];
                var ordered = near.OrderBy(kv => kv.Value);
                newpos = ordered.TakeWhile(kv => kv.Value == ordered.First().Value).RandomElement().Key;
                if (near.All(e => e.Value == near[newpos]))
                    return new List<DirectedPosition>();

                int newX = newpos.x, newY = newpos.y;
                if (!(newX == currentX && newY == currentY))
                {
                    currentX = newX;
                    currentY = newY;
                    //                    d_inv = dijkstraInner(active, grid, placing, d_inv);
                }
                DirectedPosition dp = new DirectedPosition(currentX, currentY, currentFacing);
                if(rad[dp.x+1, dp.y+1] == 0)//bestMoves.Any(b => b.x == dp.x && b.y == dp.y))// && ((0 == placing[newX, newY].color) ? 0 != active.color : 0 == active.color))
                {
                    //oldpos = new DirectedPosition(currentX, currentY, currentFacing);
                    path.Add(dp);
                    f = active.speed+10;
                }
                else
                {
                    path.Add(dp);
                }
                oldpos = new DirectedPosition(currentX, currentY, currentFacing);
            }
            path.Reverse();
            path[0].dir = active.facing;
            DirectedPosition old2 = path.First();
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
                        if (r.Next(25) <= 2)
                        {
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
                    if (r.Next(30) <= -1 && UnitGrid[i, j] == null)
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
        public List<DirectedPosition> BestPath;
        public DirectedPosition FuturePosition, target;
        public int currentlyFiring = -1;
        private bool killSuccess = false, hitSuccess = false;
        private Thread thr = null;
        public void dispose()
        {
            if(thr != null)
                thr.Abort();
        }
        public void ProcessStep()
        {
            TaskSteps++;
            switch (CurrentMode)
            {
                case Mode.Selecting:
                    
                    if(TaskSteps > 5 && thr != null && thr.ThreadState == ThreadState.Stopped)
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
                    else if(TaskSteps <= 1 && (thr == null || thr.ThreadState == ThreadState.Stopped))
                    {
                        thr = new Thread(() =>
                        {
                            BestPath = getDijkstraPath(ActiveUnit, FieldMap.Land, UnitGrid, targetX[ActingFaction], targetY[ActingFaction]);
                        });
                        thr.Start();
                    }
                    else
                    {
                        Effects.CenterCamera(ActiveUnit.x, ActiveUnit.y, 0.5F);
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
                    ActiveUnit.x = FuturePosition.x;
                    ActiveUnit.y = FuturePosition.y;
                    if (BestPath.Count <= 0 || TaskSteps > ActiveUnit.speed+1)
                    {
                        if (Position.Adjacent(ActiveUnit.x, ActiveUnit.y, width, height).Any(pos => UnitGrid[pos.x, pos.y] != null && ActiveUnit.isOpposed(UnitGrid[pos.x, pos.y])))
                        {
                            ActiveUnit.worldX = 20 + ActiveUnit.x * 64 + ActiveUnit.y * 64;
                            ActiveUnit.worldY = 6 + ActiveUnit.x * 32 - ActiveUnit.y * 32;
                            CurrentMode = Mode.Attacking;
                            TaskSteps = 0;
                            break;
                        }

                        UnitGrid[ActiveUnit.x, ActiveUnit.y] = new Unit(ActiveUnit);
                        ActingFaction = (ActingFaction + 1) % 4;
                        Unit temp = UnitGrid.RandomFactionUnit(Colors[ActingFaction]);
                        ActiveUnit = new Unit(temp);
                        UnitGrid[temp.x, temp.y] = null;

                        CurrentMode = Mode.Selecting;
                        //GameGDX.stateTime = 0;
                        TaskSteps = 0;
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
                        target = DirectedPosition.Adjacent(ActiveUnit.x, ActiveUnit.y, width, height).Where(pos => UnitGrid[pos.x, pos.y] != null && ActiveUnit.isOpposed(UnitGrid[pos.x, pos.y])).RandomElement();
                        if (target.y > ActiveUnit.y)
                        {
                            ActiveUnit.facing = Direction.SE;
                            ActiveUnit.facingNumber = 0;
                            if (UnitGrid[target.x, target.y].speed > 0)
                            {
                                UnitGrid[target.x, target.y].facing = Direction.NW;
                                UnitGrid[target.x, target.y].facingNumber = 2;
                            }

                        }
                        else if (target.y < ActiveUnit.y)
                        {
                            ActiveUnit.facing = Direction.NW;
                            ActiveUnit.facingNumber = 2;
                            if (UnitGrid[target.x, target.y].speed > 0)
                            {
                                UnitGrid[target.x, target.y].facing = Direction.SE;
                                UnitGrid[target.x, target.y].facingNumber = 0;
                            }
                        }
                        else
                        {
                            if (target.x < ActiveUnit.x)
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
                                ActiveUnit.facing = Direction.NE;
                                ActiveUnit.facingNumber = 3;
                                if (UnitGrid[target.x, target.y].speed > 0)
                                {
                                    UnitGrid[target.x, target.y].facing = Direction.SW;
                                    UnitGrid[target.x, target.y].facingNumber = 1;
                                }
                            }
                        }
                        GameGDX.attackTime = 0;
                        currentlyFiring = (ActiveUnit.weaponry[1].kind != WeaponType.None) ? 1 : (ActiveUnit.weaponry[0].kind != WeaponType.None) ? 0 : -1;
                        if (currentlyFiring > -1)
                        {
                            hitSuccess = UnitGrid[target.x, target.y].attemptDodge(ActiveUnit.weaponry[currentlyFiring]);
                            if (hitSuccess) killSuccess = UnitGrid[target.x, target.y].takeDamage(ActiveUnit.weaponry[currentlyFiring]);
                        }
                        else
                        {
                            hitSuccess = false;
                            killSuccess = false;
                        }
                        ActiveUnit.visual = (ActiveUnit.weaponry[1].kind == WeaponType.None && ActiveUnit.weaponry[0].kind == WeaponType.None) ? VisualAction.Normal : VisualAction.Firing;
                    }
                    else if (TaskSteps > 4 + 2*(Math.Abs(target.x - ActiveUnit.x) + Math.Abs(target.y - ActiveUnit.y)))
                    {
                        currentlyFiring = -1;
                        if(killSuccess)
                            UnitGrid[target.x, target.y] = null;
                        killSuccess = false;
                        hitSuccess = false;
                        UnitGrid[ActiveUnit.x, ActiveUnit.y] = new Unit(ActiveUnit);
                        ActingFaction = (ActingFaction + 1) % 4;
                        Unit temp = UnitGrid.RandomFactionUnit(Colors[ActingFaction]);
                        ActiveUnit = new Unit(temp);
                        UnitGrid[temp.x, temp.y] = null;

                        CurrentMode = Mode.Selecting;
                        TaskSteps = 0;

                        break;
                    }
                    else if (TaskSteps == 0 + 2 * (Math.Abs(target.x - ActiveUnit.x) + Math.Abs(target.y - ActiveUnit.y)) && currentlyFiring > -1)
                    {
                        if (hitSuccess || Unit.WeaponDisplays[ActiveUnit.unitIndex][currentlyFiring] == 1 || Unit.WeaponDisplays[ActiveUnit.unitIndex][currentlyFiring] == 7)
                        {
                            GameGDX.receiveTime = 0;
                            /*
                        int w = ((row < width) ? width - 1 - row + col : col); //height + (width - 1 - row) + 
                        int h = (row < width) ? col : row - width + col;
                             */
                            if (target.x + target.y <= ActiveUnit.x - ActiveUnit.y)
                            {
                                ActiveUnit.targetingAbove = new List<DirectedPosition> { new DirectedPosition(target.x, target.y, target.dir) };
                                ActiveUnit.targetingBelow = new List<DirectedPosition> { };
                            }
                            else
                            {
                                ActiveUnit.targetingAbove = new List<DirectedPosition> { };
                                ActiveUnit.targetingBelow = new List<DirectedPosition> { new DirectedPosition(target.x, target.y, target.dir) };
                            }
                        }
                        if(!hitSuccess)
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
                            Timer.instance().scheduleTask(reset, GameGDX.updateStep * 19 /8F);

                        }
                    }
                    else if (killSuccess && TaskSteps == 2 + 2 * (Math.Abs(target.x - ActiveUnit.x) + Math.Abs(target.y - ActiveUnit.y)) && currentlyFiring > -1)
                    {
                        GameGDX.explodeTime = 0;
                        UnitGrid[target.x, target.y].visual = VisualAction.Exploding;

                    }
                    break;
            }
        }
    }
}
