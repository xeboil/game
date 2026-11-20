package kopfgeldjaeger.logic;

import java.util.EnumSet;

public class HunterState extends Combatant
{
    private int money;

    private final EnumSet<Upgrade> purchasedUpgrades =
            EnumSet.noneOf(Upgrade.class);

    private double movementMultiplier = 1.0;


    public HunterState(
            String name,
            int maxHealth,
            int damage,
            int startingMoney)
    {
        super(
                name,
                maxHealth,
                damage
        );

        if (startingMoney < 0)
        {
            throw new IllegalArgumentException(
                    "Das Startgeld darf nicht negativ sein."
            );
        }

        this.money =
                startingMoney;
    }


    public int getMoney()
    {
        return money;
    }


    public double getMovementMultiplier()
    {
        return movementMultiplier;
    }


    public boolean hasUpgrade(
            Upgrade upgrade)
    {
        return purchasedUpgrades.contains(
                upgrade
        );
    }


    // -------------------------------------------------
    // GELD HINZUFÜGEN
    // -------------------------------------------------

    public void addMoney(int amount)
    {
        if (amount < 0)
        {
            throw new IllegalArgumentException(
                    "Der Geldbetrag darf nicht negativ sein."
            );
        }

        money += amount;
    }


    // -------------------------------------------------
    // GELD AUSGEBEN
    // -------------------------------------------------

    public boolean spendMoney(int amount)
    {
        if (amount < 0)
        {
            throw new IllegalArgumentException(
                    "Der Geldbetrag darf nicht negativ sein."
            );
        }

        if (money < amount)
        {
            return false;
        }

        money -= amount;

        return true;
    }


    // -------------------------------------------------
    // UPGRADE KAUFEN
    // -------------------------------------------------

    public boolean purchaseUpgrade(
            Upgrade upgrade)
    {
        if (upgrade == null)
        {
            throw new IllegalArgumentException(
                    "Die Verbesserung darf nicht null sein."
            );
        }

        if (purchasedUpgrades.contains(
                upgrade))
        {
            return false;
        }

        if (!spendMoney(
                upgrade.getPrice()))
        {
            return false;
        }

        purchasedUpgrades.add(
                upgrade
        );

        increaseDamage(
                upgrade.getDamageIncrease()
        );

        increaseMaxHealth(
                upgrade.getMaxHealthIncrease()
        );

        movementMultiplier *=
                upgrade.getMovementMultiplier();

        return true;
    }


    // -------------------------------------------------
    // ZIEL AN BEHÖRDE ABLIEFERN
    // -------------------------------------------------

    public void deliverTarget(
            BountyTarget target)
    {
        if (target == null)
        {
            throw new IllegalArgumentException(
                    "Das Ziel darf nicht null sein."
            );
        }

        target.markDelivered();

        addMoney(
                target.getBounty()
        );
    }
}