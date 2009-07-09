package model;

import javax.persistence.Entity;

/**
 * @author Emmanuel Bernard
 */
@Entity
public class Human extends Hominidae {
	private int nonPersistent;
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
