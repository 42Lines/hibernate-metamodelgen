package model;

import javax.persistence.Entity;
import javax.persistence.Access;
import javax.persistence.AccessType;

/**
 * @author Emmanuel Bernard
 */
@Entity
@Access(javax.persistence.AccessType.FIELD)
public class Hominidae extends Mammals {
	private int intelligence;

	public int getIntelligence() {
		return intelligence;
	}

	public void setIntelligence(int intelligence) {
		this.intelligence = intelligence;
	}

	public int getNonPersistent() {
		return 0;
	}
}
