package info.interactivesystems.gamificationengine.entities.goal;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.utils.StringUtils;

/**
 * With a GoalRule can be defined which tasks and if all or only one task have to be fulfilled to reach a goal and 
 * to obtain the rewards for it. When a goal rule is fulfilled the goal is added to the player’s list of finished 
 * goals. If the goal can also be done by a group it is also added to its list of finished goals. There are two types
 * of rules that can be defined: a TaskRule or a PointsRule.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "RULE_TYPE", discriminatorType = DiscriminatorType.STRING)
@JsonIgnoreProperties({ "belongsTo" })
public class GoalRule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@NotNull
	@ManyToOne
	private Organisation belongsTo;

	private String name;

	private String description;

	/**
	 * Gets the id of the GoalRule.
	 * 
	 * @return The goal rule's id as int.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id of a goal rule.
	 * 
	 * @param id
	 * 			The id of the goal rule as int.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets the organisation a goal rule belongs to.
	 * 
	 * @return The organisation of the goal rule as an object.
	 */
	public Organisation getBelongsTo() {
		return belongsTo;
	}

	/**
	 * Sets the organisation a goal rule belongs to.
	 * 
	 * @param belongsTo
	 * 				The goal rule's organisation.
	 */
	public void setBelongsTo(Organisation belongsTo) {
		this.belongsTo = belongsTo;
	}

	/**
	 * Gets the name of a goal rule.
	 * 
	 * @return The name of the goal rule as String.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the goal rule. 
	 * 
	 * @param name
	 * 			The name of the goal rule.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the description of a goal rule.
	 * 
	 * @return The description of the goal rule as String.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description of a goal rule.
	 * 
	 * @param description
	 * 			The description of a goal rule as String.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * This method checks if a goal rule belongs to a specific organisation. Therefore
	 * it is tested if the organisation's API key matchs the API key of the goal rule. 
	 * 
	 * @param organisation
	 * 			The organisation object a goal rule may belongs to.
	 * @return Boolean value if the API key of the group is the same 
	 * 			of the tested organisation (true) or not (false).
	 */
	public boolean belongsTo(Organisation organisation) {
		return getBelongsTo().getApiKey().equals(organisation.getApiKey());
	}

	/**
	 * This method gets the ids of rules that have to be deleted before a specific
	 * task can be deleted. These ids are then passed to create a message in the
	 * response to give the user a hint.
	 * 
	 * @param rules
	 * 			List of rules that are associated with the task that should be deleted.
	 */
	public static void checkRulesForTask(List<TaskRule> rules){
		List<Integer> ids = getRuleIds(rules);
		StringUtils.printIdsForDeletion(ids, "task" , "goalrule");
	}
	
	/**
	 * Gets the id each rule that is in the passed List.
	 * 
	 * @param rules
	 * 			List of rules of which the ids are returned.
	 * @return A list of Integers of the passed rules. 
	 */
	public static List<Integer> getRuleIds(List<TaskRule> rules){
		List<Integer> ids = new ArrayList<>();
		for (TaskRule rule : rules) {
			ids.add(rule.getId());
		}
		return ids;
	}
	
}
