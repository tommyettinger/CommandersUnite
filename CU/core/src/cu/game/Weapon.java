package cu.game;

public class Weapon
{
public WeaponType kind;
public int damage;
public float[] multipliers;
public int minRange;
public int maxRange;
public boolean moveAndAttack;
public boolean ring;
public boolean alert;
public boolean seek;

public Weapon(WeaponType kind, int damage, float[] multipliers, String specialQualities)
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
        if (specialQualities.isEmpty())
        switch (specialQualities.split(" ", 1)[0])
        {
        case "Alert": alert = true; break;
        case "Seek": seek = true; break;
        case "Ring": ring = true; break;
        case "Indirect": moveAndAttack = false;
        minRange = Integer.parseInt(specialQualities.split(" ", 2)[1].substring(0, 1));
        maxRange = Integer.parseInt(specialQualities.split(" ", 2)[1].substring(2, 3));
        break;
        }
        }
public Weapon(WeaponType kind, int damage, float[] multipliers, String specialQualities, int minimumRange, int maximumRange)
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
        if (specialQualities != "")
        switch (specialQualities.split(" ", 1)[0])
        {
        case "Alert": alert = true; break;
        case "Seek": seek = true; break;
        case "Ring": ring = true; break;
        case "Indirect": moveAndAttack = false; break;
        }
        minRange = minimumRange;
        maxRange = maximumRange;

        }
        }
