package info.interactivesystems.gamificationengine.entities.rewards;

import javax.persistence.MappedSuperclass;

/**
 * A volatile reward is a reward that can be awarded more than one time. So the
 * player can reach a goal and earn the connected rewards again. Such a reward
 * is for example a specific amount of points which is added to the player's
 * current points.
 */
@MappedSuperclass
public abstract class VolatileReward extends Reward {

}
