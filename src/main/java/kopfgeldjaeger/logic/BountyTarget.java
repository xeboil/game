package kopfgeldjaeger.logic;

public class BountyTarget extends Combatant
{
    private final int bounty;
    private final int timeLimitSeconds;
    private boolean delivered;

    public BountyTarget(
            String name,
            int maxHealth,
            int damage,
            int bounty,
            int timeLimitSeconds)
    {
        super(name, maxHealth, damage);

        if (bounty < 0)
        {
            throw new IllegalArgumentException("Das Kopfgeld darf nicht negativ sein.");
        }

        if (timeLimitSeconds <= 0)
        {
            throw new IllegalArgumentException("Das Zeitlimit muss größer als 0 sein.");
        }

        this.bounty = bounty;
        this.timeLimitSeconds = timeLimitSeconds;
    }

    public int getBounty()
    {
        return bounty;
    }

    public int getTimeLimitSeconds()
    {
        return timeLimitSeconds;
    }
    public boolean isDelivered()
    {
        return delivered;
    }

    public void markDelivered()
    {
        if (!isDefeated())
        {
            throw new IllegalStateException(
                    "Ein lebendes Ziel kann nicht abgeliefert werden.");
        }

        if (delivered)
        {
            throw new IllegalStateException(
                    "Dieses Ziel wurde bereits abgeliefert.");
        }

        delivered = true;
    }
}