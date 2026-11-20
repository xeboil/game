package kopfgeldjaeger.agents;

import kopfgeldjaeger.logic.CombatEngine;
import kopfgeldjaeger.logic.TargetType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CityEnvironmentAgent
{
    private static class ObstacleBox
    {
        final double x;
        final double y;
        final double width;
        final double height;

        ObstacleBox(double x, double y, double width, double height)
        {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    private final double width;
    private final double height;
    private final double topMargin;
    private final double actorRadius;

    private final List<ObstacleBox> obstacles = new ArrayList<>();
    private final Random random = new Random();

    private TargetAgent activeTargetAgent;

    public CityEnvironmentAgent(double width, double height, double topMargin, double actorRadius)
    {
        this.width = width;
        this.height = height;
        this.topMargin = topMargin;
        this.actorRadius = actorRadius;
    }

    public void addObstacle(double x, double y, double w, double h)
    {
        obstacles.add(new ObstacleBox(x, y, w, h));
    }

    public void clearObstacles()
    {
        obstacles.clear();
    }

    public TargetAgent getActiveTargetAgent()
    {
        return activeTargetAgent;
    }

    // Bewegung prüfen und ausführen (Kopfgeldjäger <-> Umgebungsagent)
    public boolean requestMove(BountyHunterAgent hunter, double deltaX, double deltaY)
    {
        double newX = hunter.getPosition().getX() + deltaX;
        double newY = hunter.getPosition().getY() + deltaY;

        newX = Math.max(actorRadius, Math.min(width - actorRadius, newX));
        newY = Math.max(topMargin, Math.min(height - actorRadius, newY));

        if (!collidesWithObstacle(newX, newY, actorRadius))
        {
            hunter.getPosition().set(newX, newY);
            return true;
        }

        return false;
    }

    // Zufällige Zielperson in der Stadt erzeugen (Umgebungsagent -> Zielpersonenagent)
    public TargetAgent spawnRandomTarget(BountyHunterAgent hunter, double minDistance)
    {
        TargetType chosenType = chooseRandomTargetType();
        TargetAgent newTarget = new TargetAgent(chosenType);

        double x;
        double y;
        double distance;

        do
        {
            x = 40 + random.nextDouble() * (width - 80);
            y = (topMargin + 20) + random.nextDouble() * (height - topMargin - 60);

            Position candidate = new Position(x, y);
            distance = candidate.distanceTo(hunter.getPosition());
        }
        while (distance < minDistance || collidesWithObstacle(x, y, actorRadius + 5));

        newTarget.spawnAt(new Position(x, y));
        this.activeTargetAgent = newTarget;
        return newTarget;
    }

    // Gezieltes Spawnen (hilfreich für deterministische Tests)
    public TargetAgent spawnSpecificTarget(TargetType type, Position spawnPosition)
    {
        TargetAgent newTarget = new TargetAgent(type);
        newTarget.spawnAt(spawnPosition);
        this.activeTargetAgent = newTarget;
        return newTarget;
    }

    // Begegnung zwischen Jäger und Zielperson prüfen
    public boolean checkEncounter(BountyHunterAgent hunter, TargetAgent target)
    {
        if (hunter == null || target == null || !target.isActive())
        {
            return false;
        }

        double distance = hunter.getPosition().distanceTo(target.getPosition());
        return distance <= (actorRadius * 2);
    }

    // Zeitlich begrenzten Kampf initialisieren
    public CombatEngine startCombat(TargetAgent target)
    {
        if (target == null || !target.isActive())
        {
            throw new IllegalStateException("Kein aktives Ziel für einen Kampf vorhanden.");
        }

        return new CombatEngine(target.getTarget().getTimeLimitSeconds());
    }

    private TargetType chooseRandomTargetType()
    {
        int roll = random.nextInt(100);
        if (roll < 10)
        {
            return TargetType.SPECIAL_TARGET;
        }

        TargetType[] normalTargets =
        {
            TargetType.PETTY_CRIMINAL,
            TargetType.THIEF,
            TargetType.ROBBER,
            TargetType.DANGEROUS_FUGITIVE
        };

        return normalTargets[random.nextInt(normalTargets.length)];
    }

    private boolean collidesWithObstacle(double circleX, double circleY, double radius)
    {
        for (ObstacleBox obs : obstacles)
        {
            double nearestX = Math.max(obs.x, Math.min(circleX, obs.x + obs.width));
            double nearestY = Math.max(obs.y, Math.min(circleY, obs.y + obs.height));

            double dx = circleX - nearestX;
            double dy = circleY - nearestY;

            if (dx * dx + dy * dy < radius * radius)
            {
                return true;
            }
        }

        return false;
    }
}