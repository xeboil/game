package kopfgeldjaeger.logic;

public class GameSession
{
    private final HunterState hunter;
    private BountyTarget currentTarget;
    private CombatEngine combatEngine;
    private CombatEngine.Result combatResult;

    public GameSession(HunterState hunter)
    {
        if (hunter == null)
        {
            throw new IllegalArgumentException(
                    "Der Jäger darf nicht null sein.");
        }

        this.hunter = hunter;
    }

    public HunterState getHunter()
    {
        return hunter;
    }

    public BountyTarget getCurrentTarget()
    {
        return currentTarget;
    }

    public CombatEngine.Result getCombatResult()
    {
        return combatResult;
    }

    public void startCombat(TargetType targetType)
    {
        if (hunter.isDefeated())
        {
            throw new IllegalStateException(
                    "Ein besiegter Jäger kann keinen Kampf beginnen.");
        }

        if (combatResult == CombatEngine.Result.IN_PROGRESS)
        {
            throw new IllegalStateException(
                    "Es läuft bereits ein Kampf.");
        }

        if (currentTarget != null
                && combatResult == CombatEngine.Result.TARGET_CAPTURED
                && !currentTarget.isDelivered())
        {
            throw new IllegalStateException(
                    "Das gefangene Ziel muss zuerst abgeliefert werden.");
        }

        if (targetType == null)
        {
            throw new IllegalArgumentException(
                    "Der Zieltyp darf nicht null sein.");
        }

        currentTarget = targetType.createTarget();
        combatEngine = new CombatEngine(
                currentTarget.getTimeLimitSeconds());
        combatResult = CombatEngine.Result.IN_PROGRESS;
    }

    public CombatEngine.Result playCombatRound()
    {
        if (combatEngine == null)
        {
            throw new IllegalStateException(
                    "Es wurde noch kein Kampf gestartet.");
        }

        if (combatResult == CombatEngine.Result.IN_PROGRESS)
        {
            combatResult = combatEngine.playRound(hunter, currentTarget);
        }

        return combatResult;
    }

    public void deliverCurrentTarget()
    {
        if (combatResult != CombatEngine.Result.TARGET_CAPTURED)
        {
            throw new IllegalStateException(
                    "Nur ein gefangenes Ziel kann abgeliefert werden.");
        }

        hunter.deliverTarget(currentTarget);
    }

    public boolean purchaseUpgrade(Upgrade upgrade)
    {
        if (combatResult == CombatEngine.Result.IN_PROGRESS)
        {
            throw new IllegalStateException(
                    "Während eines Kampfes kann nichts gekauft werden.");
        }

        return hunter.purchaseUpgrade(upgrade);
    }
}