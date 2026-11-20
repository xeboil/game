package kopfgeldjaeger.logic;

public enum TargetType
{
    PETTY_CRIMINAL(
            "Kleinkrimineller",
            30,
            5,
            200,
            30
    ),

    THIEF(
            "Dieb",
            50,
            8,
            400,
            25
    ),

    ROBBER(
            "Räuber",
            80,
            12,
            800,
            20
    ),

    DANGEROUS_FUGITIVE(
            "Gefährlicher Flüchtiger",
            120,
            18,
            1500,
            20
    ),

    SPECIAL_TARGET(
            "Sonderziel",
            160,
            22,
            2000,
            18
    );


    private final String displayName;
    private final int health;
    private final int damage;
    private final int bounty;
    private final int timeLimitSeconds;


    TargetType(
            String displayName,
            int health,
            int damage,
            int bounty,
            int timeLimitSeconds)
    {
        this.displayName =
                displayName;

        this.health =
                health;

        this.damage =
                damage;

        this.bounty =
                bounty;

        this.timeLimitSeconds =
                timeLimitSeconds;
    }


    public BountyTarget createTarget()
    {
        return new BountyTarget(
                displayName,
                health,
                damage,
                bounty,
                timeLimitSeconds
        );
    }
}