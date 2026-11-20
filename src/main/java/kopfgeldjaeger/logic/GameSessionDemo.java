package kopfgeldjaeger.logic;

public class GameSessionDemo
{
    public static void main(String[] args)
    {
        HunterState hunter =
                new HunterState("Kopfgeldjäger", 60, 12, 0);
        GameSession game = new GameSession(hunter);

        game.startCombat(TargetType.THIEF);

        CombatEngine.Result result =
                CombatEngine.Result.IN_PROGRESS;

        while (result == CombatEngine.Result.IN_PROGRESS)
        {
            result = game.playCombatRound();

            System.out.println(
                    "Jäger: " + game.getHunter().getHealth() + " LP");
            System.out.println(
                    "Ziel: " + game.getCurrentTarget().getHealth() + " LP");
            System.out.println("---");
        }

        System.out.println("Kampfergebnis: " + result);

        if (result == CombatEngine.Result.TARGET_CAPTURED)
        {
            // Die Demo bildet hier die Ankunft bei der Behörde ab.
            game.deliverCurrentTarget();

            System.out.println(
                    "Geld nach Ablieferung: "
                            + game.getHunter().getMoney() + " Euro");

            boolean bought =
                    game.purchaseUpgrade(Upgrade.BATON);

            System.out.println("Schlagstock gekauft: " + bought);
            System.out.println(
                    "Schaden danach: "
                            + game.getHunter().getDamage());
            System.out.println(
                    "Geld danach: "
                            + game.getHunter().getMoney() + " Euro");
        }
    }
}