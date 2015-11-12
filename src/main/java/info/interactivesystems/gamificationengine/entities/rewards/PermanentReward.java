package info.interactivesystems.gamificationengine.entities.rewards;

import javax.persistence.Entity;

/**
 * Permanent rewards are rewards that can be earned only once. These are for
 * example an achievement or a badge. If a player has such an reward she/he 
 * may reach the goal again but doesn't get the reward one more time.
 */
@Entity
public abstract class PermanentReward extends Reward {

}
