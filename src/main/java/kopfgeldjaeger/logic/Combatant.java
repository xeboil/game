package kopfgeldjaeger.logic;

public class Combatant
{
    private final String name;
    private int maxHealth;
    private int health;
    private int damage;

    public Combatant(String name, int maxHealth, int damage)
    {
        if (name == null || name.isBlank())
        {
            throw new IllegalArgumentException(
                    "Der Name darf nicht leer sein.");
        }

        if (maxHealth <= 0)
        {
            throw new IllegalArgumentException(
                    "Die maximalen Lebenspunkte müssen größer als 0 sein.");
        }

        if (damage < 0)
        {
            throw new IllegalArgumentException(
                    "Der Schaden darf nicht negativ sein.");
        }

        this.name = name;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.damage = damage;
    }

    public String getName()
    {
        return name;
    }

    public int getMaxHealth()
    {
        return maxHealth;
    }

    public int getHealth()
    {
        return health;
    }

    public int getDamage()
    {
        return damage;
    }

    protected void increaseDamage(int amount)
    {
        if (amount < 0)
        {
            throw new IllegalArgumentException(
                    "Die Schadenserhöhung darf nicht negativ sein.");
        }

        damage += amount;
    }

    protected void increaseMaxHealth(int amount)
    {
        if (amount < 0)
        {
            throw new IllegalArgumentException(
                    "Die Erhöhung der maximalen Lebenspunkte darf nicht negativ sein.");
        }

        maxHealth += amount;
    }

    public boolean isDefeated()
    {
        return health == 0;
    }

    public void takeDamage(int amount)
    {
        if (amount < 0)
        {
            throw new IllegalArgumentException(
                    "Der Schaden darf nicht negativ sein.");
        }

        health = Math.max(0, health - amount);
    }

    // Neue Heilfunktion
    public void heal(int amount)
    {
        if (amount < 0)
        {
            throw new IllegalArgumentException(
                    "Die Heilung darf nicht negativ sein.");
        }

        health = Math.min(
                maxHealth,
                health + amount
        );
    }
}