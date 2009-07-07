package org.hibernate.jpa.metamodel.ap.annotation;

import org.hibernate.jpa.metamodel.ap.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.SimpleTypeVisitor6;
import javax.tools.Diagnostic.Kind;

public class MetaEntity implements IMetaEntity {

    final TypeElement element;
    final protected ProcessingEnvironment pe;

    final ImportContext importContext;

    public MetaEntity(ProcessingEnvironment pe, TypeElement element) {
        this.element = element;
        this.pe = pe;
        importContext = new ImportContextImpl(getPackageName().toString());
    }

    public String getSimpleName() {
        return element.getSimpleName().toString();
    }

    public Element getOriginalElement() {
        return element;
    }

    public String getQualifiedName() {
        return element.getQualifiedName().toString();
    }

    public String getPackageName() {
        PackageElement packageOf = pe.getElementUtils().getPackageOf(element);
        return pe.getElementUtils().getName(packageOf.getQualifiedName() + ".metamodel").toString();
    }

    public List<IMetaMember> getMembers() {

        List<IMetaMember> members = new ArrayList<IMetaMember>();

        if (useFields()) {

            List<? extends Element> myMembers = ElementFilter.fieldsIn(element.getEnclosedElements());

            System.out.println("Scanning " + myMembers.size() + " field s for " + element.toString());

            for (Element mymember : myMembers) {

                MetaMember result = mymember.asType().accept(new TypeVisitor(this), mymember);
                if (result != null) {
                    members.add(result);
                } else {
                    pe.getMessager().printMessage(Kind.WARNING, "Could not find valid info for JPA property", mymember);
                }
            }

        } else {


            List<? extends Element> myMembers = ElementFilter.methodsIn(element.getEnclosedElements());

            System.out.println("Scanning " + myMembers.size() + " methods for " + element.toString());
            for (Element mymember : myMembers) {

                MetaMember result = mymember.asType().accept(new TypeVisitor(this), mymember);
                if (result != null) {
                    members.add(result);
                } else {
                    //pe.getMessager().printMessage(Kind.WARNING, "Not a valid JPA property", mymember);
                }
            }

        }

        if (members.size() == 0) {
            pe.getMessager().printMessage(Kind.WARNING, "No properties found on " + element, element);
        }
        return members;
    }


    //TODO: Find more efficient way to identify wether we should use fields or properties
    private boolean useFields() {
        List<? extends Element> myMembers = element.getEnclosedElements();
        for (Element element : myMembers) {
            List<? extends AnnotationMirror> entityAnnotations =
                    pe.getElementUtils().getAllAnnotationMirrors(element);

            for (Iterator<?> iterator = entityAnnotations.iterator(); iterator
                    .hasNext();) {
                AnnotationMirror annotationMirror = (AnnotationMirror) iterator
                        .next();

                final String annotationType = annotationMirror.getAnnotationType().toString();

                if (annotationType.equals(javax.persistence.Id.class.getName()) ||
                        annotationType.equals(javax.persistence.EmbeddedId.class.getName())) {
                    if (element.getKind() == ElementKind.FIELD) {
                        return true;
                    }
                }
            }
        }

        return false;
    }


    static Map<String, String> COLLECTIONS = new HashMap<String, String>();

    static {
        COLLECTIONS.put("java.util.Collection", "javax.persistence.metamodel.Collection");
        COLLECTIONS.put("java.util.Set", "javax.persistence.metamodel.Set");
        COLLECTIONS.put("java.util.List", "javax.persistence.metamodel.List");
        COLLECTIONS.put("java.util.Map", "javax.persistence.metamodel.Map");
    }

    class TypeVisitor extends SimpleTypeVisitor6<MetaMember, Element> {

        MetaEntity parent;

        TypeVisitor(MetaEntity parent) {
            this.parent = parent;
        }

        @Override
        protected MetaMember defaultAction(TypeMirror e, Element p) {
            return super.defaultAction(e, p);
        }

        @Override
        public MetaMember visitPrimitive(PrimitiveType t, Element p) {
            return new MetaAttribute(parent, p, TypeUtils.toTypeString(t));
        }


        @Override
        public MetaMember visitDeclared(DeclaredType t, Element p) {
            TypeElement e = (TypeElement) pe.getTypeUtils().asElement(t);

            String collection = COLLECTIONS.get(e.getQualifiedName().toString()); // WARNING: .toString() is necessary here since Name equals does not compare to String
            if (collection != null) {
                if (collection.equals("javax.persistence.metamodel.Map")) {
                    return new MetaMap(parent, p, collection, getKeyType(t), getElementType(t));
                } else {
                    return new MetaCollection(parent, p, collection, getElementType(t));
                }
            } else {
                return new MetaAttribute(parent, p, e.getQualifiedName().toString());
            }
        }


        @Override
        public MetaMember visitExecutable(ExecutableType t, Element p) {
            String string = p.getSimpleName().toString();

            // TODO: implement proper property get/is/boolean detection
            if (string.startsWith("get") || string.startsWith("is")) {
                TypeMirror returnType = t.getReturnType();

                return returnType.accept(this, p);
            } else {
                return null;
            }
        }
    }

    public String generateImports() {
        return importContext.generateImports();
    }

    public String importType(String fqcn) {
        return importContext.importType(fqcn);
    }

    public String staticImport(String fqcn, String member) {
        return importContext.staticImport(fqcn, member);
    }

    public String importType(Name qualifiedName) {
        return importType(qualifiedName.toString());
    }

    private String getKeyType(DeclaredType t) {
        System.out.println("key type" + t);
        return t.getTypeArguments().get(0).toString();
    }


    private String getElementType(DeclaredType declaredType) {
        if (declaredType.getTypeArguments().size() == 1) {
            return declaredType.getTypeArguments().get(0).toString();
        } else {
            return declaredType.getTypeArguments().get(1).toString();
        }
    }
}
