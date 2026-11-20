package kopfgeldjaeger.logic;

public enum Upgrade
{
    BATON(
            "Schlagstock",
            300,
            4,
            0,
            1.0
    ),

    VEST(
            "Schutzweste",
            300,
            0,
            20,
            1.0
    ),

    BICYCLE(
            "Fahrrad",
            400,
            0,
            0,
            1.30
    );

    private final String displayName;
    private final int price;
    private final int damageIncrease;
    private final int maxHealthIncrease;
    private final double movementMultiplier;

    Upgrade(
            String displayName,
            int price,
            int damageIncrease,
            int maxHealthIncrease,
            double movementMultiplier)
    {
        this.displayName =
                displayName;

        this.price =
                price;

        this.damageIncrease =
                damageIncrease;

        this.maxHealthIncrease =
                maxHealthIncrease;

        this.movementMultiplier =
                movementMultiplier;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public int getPrice()
    {
        return price;
    }

    public int getDamageIncrease()
    {
        return damageIncrease;
    }

    public int getMaxHealthIncrease()
    {
        return maxHealthIncrease;
    }

    public double getMovementMultiplier()
    {
        return movementMultiplier;
    }
}