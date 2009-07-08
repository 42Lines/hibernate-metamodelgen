package org.hibernate.jpa.metamodel.ap.annotation;

import org.hibernate.jpa.metamodel.ap.IMetaSingleAttribute;

import javax.lang.model.element.Element;

public class MetaSingleAttribute extends MetaAttribute implements IMetaSingleAttribute {

	public MetaSingleAttribute(MetaEntity parent, Element element, String type) {
		super(parent, element, type);
	}

	@Override
	public String getMetaType() {
		return "javax.persistence.metamodel.SingularAttribute";
	}

}
