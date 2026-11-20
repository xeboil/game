package kopfgeldjaeger.agents;

import kopfgeldjaeger.logic.CombatEngine;
import kopfgeldjaeger.logic.HunterState;
import kopfgeldjaeger.logic.TargetType;
import kopfgeldjaeger.logic.Upgrade;

public class AgentSimulationDemo
{
    public static void main(String[] args)
    {
        System.out.println("=== AGENTEN-SIMULATION KONSOLENTEST ===");

        // 1. Agenten initialisieren
        CityEnvironmentAgent cityAgent = new CityEnvironmentAgent(1200, 800, 150, 20);
        BountyHunterAgent hunterAgent = new BountyHunterAgent(
                new HunterState("Kopfgeldjäger", 60, 12, 1500),
                new Position(400, 400)
        );
        AuthorityAgent authorityAgent = new AuthorityAgent();
        ShopAgent shopAgent = new ShopAgent();
        PrisonAgent prisonAgent = new PrisonAgent();

        // 2. Umgebungsagent erzeugt Zielpersonenagenten
        TargetAgent targetAgent = cityAgent.spawnSpecificTarget(
                TargetType.ROBBER,
                new Position(430, 400)
        );
        System.out.println("[Umgebungsagent] Ziel gespawnt: "
                + targetAgent.getTarget().getName()
                + " an Position " + targetAgent.getPosition());

        // 3. Kopfgeldjäger bewegt sich zum Ziel
        boolean moved = hunterAgent.moveBy(15, 0, cityAgent);
        System.out.println("[Kopfgeldjäger] Bewegung ausgeführt: " + moved
                + " -> Neue Position: " + hunterAgent.getPosition());

        // 4. Begegnung prüfen & Kampf durchführen
        if (cityAgent.checkEncounter(hunterAgent, targetAgent))
        {
            System.out.println("[Umgebungsagent] Begegnung erkannt! Kampf startet...");
            CombatEngine engine = cityAgent.startCombat(targetAgent);
            CombatEngine.Result result = CombatEngine.Result.IN_PROGRESS;

            while (result == CombatEngine.Result.IN_PROGRESS)
            {
                result = engine.playRound(hunterAgent.getState(), targetAgent.getTarget());
            }

            System.out.println("[Kampf] Ergebnis: " + result);

            if (result == CombatEngine.Result.TARGET_CAPTURED)
            {
                targetAgent.deactivate();
                hunterAgent.captureTarget(targetAgent.getTarget());
                System.out.println("[Kopfgeldjäger] Ziel festgenommen: "
                        + hunterAgent.getCarriedTarget().getName());
            }
        }

        // 5. Übergabe an Behördenagent
        int payout = authorityAgent.processHandover(hunterAgent);
        System.out.println("[Behördenagent] Kopfgeld ausgezahlt: +" + payout + " €"
                + " | Kontostand Jäger: " + hunterAgent.getState().getMoney() + " €");

        // 6. Kauf beim Shopagent
        boolean boughtBaton = shopAgent.sellUpgrade(hunterAgent, Upgrade.BATON);
        System.out.println("[Shopagent] Schlagstock verkauft: " + boughtBaton
                + " | Neuer Schaden: " + hunterAgent.getState().getDamage()
                + " | Restgeld: " + hunterAgent.getState().getMoney() + " €");

        // 7. Gefängnisagent testen (Sonderziel einsperren & passive Einnahmen)
        boolean prisonBought = prisonAgent.buyPrison(hunterAgent);
        System.out.println("[Gefängnisagent] Gefängnis gekauft: " + prisonBought
                + " | Restgeld: " + hunterAgent.getState().getMoney() + " €");

        TargetAgent specialAgent = cityAgent.spawnSpecificTarget(
                TargetType.SPECIAL_TARGET,
                new Position(415, 400)
        );
        CombatEngine specialCombat = cityAgent.startCombat(specialAgent);
        while (specialCombat.playRound(hunterAgent.getState(), specialAgent.getTarget())
                == CombatEngine.Result.IN_PROGRESS)
        {
            // Kampfrunden bis Entscheidung
        }

        if (specialAgent.isDefeated())
        {
            specialAgent.deactivate();
            hunterAgent.captureTarget(specialAgent.getTarget());

            boolean imprisoned = prisonAgent.imprisonSpecialTarget(hunterAgent, specialAgent);
            System.out.println("[Gefängnisagent] Sonderziel eingesperrt: " + imprisoned
                    + " | Belegte Zellen: " + prisonAgent.getPrisonState().getPrisoners());

            int passiveIncome = prisonAgent.collectPeriodicIncome(hunterAgent);
            System.out.println("[Gefängnisagent] Regelmäßige Einnahmen: +" + passiveIncome + " €"
                    + " | Endkontostand: " + hunterAgent.getState().getMoney() + " €");
        }
    }
}