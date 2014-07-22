package cu.game;

import com.badlogic.gdx.utils.*;

import java.util.ArrayList;

/**
 * Created by Tommy Ettinger on 7/22/2014.
 */
public class Unit
{
    public int unitIndex;
    public String name;
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

    public ArrayList<DirectedPosition> targeting;
    public VisualAction visual;
    public int x;
    public int y;
    public float worldX;
    public float worldY;


    public static String[] CurrentUnits = {
            "Infantry", "Infantry_P", "Infantry_S", "Infantry_T",
            "Artillery", "Artillery_P", "Artillery_S", "Artillery_T",
            "Tank", "Tank_P", "Tank_S", "Tank_T",
            "Plane", "Plane_P", "Plane_S", "Plane_T",
            "Supply", "Supply_P", "Supply_S", "Supply_T",
            "Copter", "Copter_P", "Copter_S", "Copter_T",
            "City", "Factory", "Airport", "Laboratory", "Castle", "Estate"};
    public static String[] UnitNames =
            {
                    "Infantry", "Bazooka", "Bike", "Sniper",
                    "Light Artillery", "Defensive Artillery", "AA Artillery", "Stealth Artillery",
                    "Light Tank", "Heavy Tank", "AA Tank", "Recon Tank",
                    "Prop Plane", "Ground Bomber", "Fighter Jet", "Stealth Bomber",
                    "Supply Truck", "Rig", "Amphi Transport", "Jammer",
                    "Transport Copter", "Gunship Copter", "Blitz Copter", "Comm Copter",
                    "City", "Factory", "Airport", "Laboratory", "Castle", "Estate"
            };
    public String[] UnitTypes =
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
            new Weapon[]{new Weapon(WeaponType.LightGun, 10, new float[]{1, 0.5f, 0.5f, 0, 0.5f}, ""), new Weapon(WeaponType.HeavyGun, 13, new float[]{2, 0.5f, 1, 0, 0.5f}, "Indirect", 1, 2), },

            new Weapon[]{new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), new Weapon(WeaponType.Cannon, 13, new float[]{1, 2, 2, 0, 0}, "Indirect", 2, 4), },
            new Weapon[]{new Weapon(WeaponType.Cannon, 16, new float[]{1, 2, 2, 0, 0}, "Indirect", 1, 3), new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), },
            new Weapon[]{new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), new Weapon(WeaponType.Missile, 13, new float[]{0, 0.5f, 0.5f, 2, 2}, "Indirect", 2, 6), },
            new Weapon[]{new Weapon(WeaponType.None, 0, new float[]{0, 0, 0, 0, 0}, ""), new Weapon(WeaponType.Missile, 19, new float[]{1, 2, 2, 0, 0}, "Indirect", 3, 6), },

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
    public static ObjectIntMap<String> UnitLookup = new ObjectIntMap<String>(30), TerrainLookup = new ObjectIntMap<String>(10), NameLookup = new ObjectIntMap<String>(30);
    public static ObjectMap<MovementType, ArrayList<Integer>> MobilityToUnits = new ObjectMap<MovementType, ArrayList<Integer>>(30);
    public static ObjectMap<MovementType, int[]> MobilityToTerrains = new ObjectMap<MovementType, int[]>();
    public static ArrayList<ArrayList<Integer>> TerrainToUnits = new ArrayList<ArrayList<Integer>>(30);
    public static IntMap<ArrayList<MovementType>> TerrainToMobilities = new IntMap<ArrayList<MovementType>>();
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
            MovementType.Treads, MovementType.Treads, MovementType.Treads, MovementType.WheelsTraverse,
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
        switch (ut)
        {
            case Personnel: return 0;
            case Armored: return 1;
            case Vehicle: return 2;
            case Plane: return 3;
            case Helicopter: return 4;
        }
        return 1;
    }
    static
    {
        MovementType[] values = MovementType.values();
        for (MovementType v : values)
        {
            MobilityToUnits.put(v, new ArrayList<Integer>());
        }
        for (int t = 0; t < Logic.LocalMap.Terrains.Length; t++)
        {
            TerrainLookup[Logic.LocalMap.Terrains[t]] = t;
            TerrainToMobilities.put(t, new ArrayList<MovementType>());
            TerrainToUnits.add(new ArrayList<Integer>(32));
        }
        for (int i = 0; i < CurrentUnits.length; i++)
        {
            UnitLookup.put(CurrentUnits[i], i);
            NameLookup.put(UnitNames[i], i);
            MobilityToUnits.get(AllMobilities[i]).add(i);
        }
        MobilityToTerrains.put(MovementType.Flight,
                new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 });
        MobilityToTerrains.put(MovementType.FlightFlyby,
                new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 });
        MobilityToTerrains.put(MovementType.Foot,
                new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 });
        MobilityToTerrains.put(MovementType.Treads,
                new int[] { 0, 1, 2, 3, 6, 7, 8});
        MobilityToTerrains.put(MovementType.TreadsAmphi,
                new int[] { 0, 1, 2, 3, 6, 7, 8, 9});
        MobilityToTerrains.put(MovementType.Wheels,
                new int[] { 0, 2, 7, 8});
        MobilityToTerrains.put(MovementType.WheelsTraverse,
                new int[] { 0, 1, 2, 3, 6, 7, 8});
        MobilityToTerrains.put(MovementType.Immobile,
                new int[] {});
        for (ObjectMap.Entry<MovementType, int[]> kv : MobilityToTerrains)
        {
            for (int t : kv.value)
            {
                TerrainToMobilities.get(t).add(kv.key);
            }
        }
        for (IntMap.Entry<ArrayList<MovementType>> kv : TerrainToMobilities)
        {
            for (MovementType m : kv.value)
                TerrainToUnits.get(kv.key).addAll(MobilityToUnits.get(m));
            TerrainToUnits.set(kv.key, Tools.distinct(TerrainToUnits.get(kv.key)));
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
        targeting = new ArrayList<DirectedPosition>();
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
        int f = Logic.r.nextInt(4);
        if(f==0) {
            facing = Direction.SE;
            facingNumber = 0;
        }
        else if(f==1) {
            facing = Direction.SW;
            facingNumber = 1;
        }
        else if(f==2) {
            facing = Direction.NW;
            facingNumber = 2;
        }
        else if(f==3) {
            facing = Direction.NE;
            facingNumber = 3;
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
    public Unit(String unit, int color, int x, int y)
    {
        this.name = unit;
        this.x = x;
        this.y = y;

        worldX = 20 + x * 64 + y * 64;
        worldY = 6 + x * 32 - y * 32;
        //                this.unit = index_matches[unit];
        this.unitIndex = UnitLookup.get(name, 0);
        this.color = color;
        int f = Logic.r.nextInt(4);
        if(f==0) {
            facing = Direction.SE;
            facingNumber = 0;
        }
        else if(f==1) {
            facing = Direction.SW;
            facingNumber = 1;
        }
        else if(f==2) {
            facing = Direction.NW;
            facingNumber = 2;
        }
        else if(f==3) {
            facing = Direction.NE;
            facingNumber = 3;
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
        if(dir==0) {
            facing = Direction.SE;
            facingNumber = 0;
        }
        else if(dir==1) {
            facing = Direction.SW;
            facingNumber = 1;
        }
        else if(dir==2) {
            facing = Direction.NW;
            facingNumber = 2;
        }
        else if(dir==3) {
            facing = Direction.NE;
            facingNumber = 3;
        }
        else {
            facing = Direction.SE;
            facingNumber = 0;
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
    public boolean isOpposed(Unit u)
    {
        if (color == 0)
            return u.color != 0;
        else
            return u.color == 0;
    }
    public boolean attemptDodge(Weapon attacker)
    {
        if (Logic.r.nextInt(10) + 1 > dodge && attacker.multipliers[UnitTypeAsNumber(kind)] > 0)
        {
            return true;
        }
        return false;
    }
    public boolean takeDamage(Weapon attacker)
    {
        return takeDamage(attacker.damage + Logic.r.nextInt(attacker.damage) / 2f - attacker.damage / 4f, attacker.multipliers[UnitTypeAsNumber(kind)]);
    }
    public boolean takeDamage(float amount, float multiplier)
    {
        currentHealth -= (int)Math.round(amount * (multiplier - 0.1f * armor));
        if (currentHealth <= 0)
        {
            currentHealth = 0;
            return true;
        }
        return false;
    }
}
