package info.interactivesystems.gamificationengine.entities.goal;

import info.interactivesystems.gamificationengine.entities.Organisation;
import info.interactivesystems.gamificationengine.entities.rule.ExpressionNode;
import info.interactivesystems.gamificationengine.entities.task.FinishedTask;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "RULE_TYPE", discriminatorType = DiscriminatorType.STRING)
public class GoalRule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@NotNull
	@ManyToOne
	private Organisation belongsTo;

	private String name;

	private String description;

	@OneToOne(cascade = CascadeType.ALL)
	private ExpressionNode expressionTree;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Organisation getBelongsTo() {
		return belongsTo;
	}

	public void setBelongsTo(Organisation belongsTo) {
		this.belongsTo = belongsTo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ExpressionNode getExpressionTree() {
		return expressionTree;
	}

	public void setExpressionTree(ExpressionNode expressionTree) {
		this.expressionTree = expressionTree;
	}

	public boolean belongsTo(Organisation organisation) {
		return getBelongsTo().getApiKey().equals(organisation.getApiKey());
	}

	public boolean checkRule(List<FinishedTask> finishedPlayerTasks, LocalDateTime lastDate) {
		return expressionTree.evaluate();
	}

	// public abstract Progress getProgress(Player player);

}
