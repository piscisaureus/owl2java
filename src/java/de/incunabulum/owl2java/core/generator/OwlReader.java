package de.incunabulum.owl2java.core.generator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.AllValuesFromRestriction;
import com.hp.hpl.jena.ontology.CardinalityRestriction;
import com.hp.hpl.jena.ontology.HasValueRestriction;
import com.hp.hpl.jena.ontology.IntersectionClass;
import com.hp.hpl.jena.ontology.MaxCardinalityRestriction;
import com.hp.hpl.jena.ontology.MinCardinalityRestriction;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.ontology.SomeValuesFromRestriction;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import de.incunabulum.owl2java.core.model.jenautils.ResourceError;
import de.incunabulum.owl2java.core.model.jmodel.JClass;
import de.incunabulum.owl2java.core.model.jmodel.JModel;
import de.incunabulum.owl2java.core.model.jmodel.JPackage;
import de.incunabulum.owl2java.core.model.jmodel.JProperty;
import de.incunabulum.owl2java.core.model.jmodel.JRestrictionsContainer;
import de.incunabulum.owl2java.core.model.jmodel.utils.DebugUtils;
import de.incunabulum.owl2java.core.model.jmodel.utils.LogUtils;
import de.incunabulum.owl2java.core.model.jmodel.utils.NamingUtils;
import de.incunabulum.owl2java.core.model.ns.NamespaceUtils;

public class OwlReader {

	private static Log log = LogFactory.getLog(OwlReader.class);

	private OntModel ontModel;
	public JModel jmodel;

	private String basePackage;
	private List<String> forbiddenPrefixes = new ArrayList<String>();
	private List<JClass> deferredIntersectionClasses = new ArrayList<JClass>();

	public void addForbiddenPrefix(String prefix) {
		forbiddenPrefixes.add(prefix);
	}

	@SuppressWarnings("unchecked")
	protected void createJPackages() {
		// add a default model for all classes /wo a prefix
		JPackage defaultPkg = new JPackage(jmodel, basePackage);
		this.jmodel.addPackage(basePackage, defaultPkg);

		// find all namespaces, generate prefixes if required
		handleNamespaces();

		// Based on this create the required packages
		List<String> ns = jmodel.listNamespaces();
		for (String uri : ns) {
			String prefix = jmodel.getPrefix(uri);

			// Don't create packages for default namespaces
			if (NamespaceUtils.defaultNs2UriMapping.containsKey(uri))
				continue;

			// abort if we have an invalid prefix
			if (forbiddenPrefixes.contains(prefix)) {
				log
						.error("Prefix " + prefix
								+ " is identical with a system internal prefix (toolspackage?). Aborting!");
				System.exit(1);
			}

			// empty prefix (base uri) > assign to base package
			if (prefix == JModel.BASEPREFIX) {
				log.info("Assigning namespace " + uri + " to base package " + basePackage);
				jmodel.addPackage(uri, basePackage);
				continue;
			}

			String pkgName = NamingUtils.getJavaPackageName(basePackage, prefix);
			log.info("Generating package " + pkgName);
			JPackage p = new JPackage(jmodel, pkgName);
			this.jmodel.addPackage(pkgName, p);
			jmodel.addPackage(uri, pkgName);
		}

	}

	protected void createJRestriction(Restriction res, OntClass cls, OntProperty prop) {
		// one restriction per property and class only!

		JClass jClass = jmodel.getJClass(cls.getURI());
		JProperty jProp = jmodel.getJProperty(prop.getURI());

		// we are reusing existing restriction if possible
		JRestrictionsContainer jRestriction;
		if (jClass.hasDomainRestrictionsContainer(jProp)) {
			log.debug(LogUtils.toLogName(jClass, jProp) + ": Reusing existing restriction");
			jRestriction = jClass.getDomainRestrictionsContainer(jProp);
		} else {
			jRestriction = new JRestrictionsContainer(jClass, jProp);
			log.debug(LogUtils.toLogName(jClass, jProp) + ": Creating new restriction");
		}

		// max. cardinality
		if (res.isMaxCardinalityRestriction()) {
			MaxCardinalityRestriction maxCardinalityRestriction = res.asMaxCardinalityRestriction();
			int maxCardinality = maxCardinalityRestriction.getMaxCardinality();
			jRestriction.getCardinalityRestriction().setMaxCardinality(maxCardinality);
			log.debug(DebugUtils.logPropertyOnClass(cls, prop) + ": Max cardinality set to " + maxCardinality);
		}

		// min cardinality -> skipped, does not make sense
		if (res.isMinCardinalityRestriction()) {
			MinCardinalityRestriction minCardinalityRestriction = res.asMinCardinalityRestriction();
			int minCardinality = minCardinalityRestriction.getMinCardinality();
			jRestriction.getCardinalityRestriction().setMinCardinality(minCardinality);
			log.debug(DebugUtils.logPropertyOnClass(cls, prop) + ": Min cardinality set to " + minCardinality);
		}

		// exactly -> only useful for exactly = 1
		if (res.isCardinalityRestriction()) {
			CardinalityRestriction cardinalityRestriction = res.asCardinalityRestriction();
			int cardinality = cardinalityRestriction.getCardinality();
			jRestriction.getCardinalityRestriction().setCardinality(cardinality);
			log.debug(DebugUtils.logPropertyOnClass(cls, prop) + ": Cardinality (min=max) set to " + cardinality);
		}

		// All values
		if (res.isAllValuesFromRestriction()) {
			// TODO: currently, only allValues from ObjectProperties can be handled
			if (prop.isDatatypeProperty()) {
				log.warn(LogUtils.toLogName(prop) + ": Not creating allValues restriction on datatype property");
				return;
			}
			AllValuesFromRestriction allValuesRestriction = res.asAllValuesFromRestriction();
			Resource allValuesResource = allValuesRestriction.getAllValuesFrom();
			JClass allValuesJClass = jmodel.getJClass(allValuesResource.getURI());
			jRestriction.addAllValuesRestriction(allValuesJClass);
			log.debug(DebugUtils.logPropertyOnClass(cls, prop) + ": Added allValues restriction:"
					+ LogUtils.toLogName(allValuesJClass));
		}

		if (res.isHasValueRestriction()) {
			// TODO: currently, only allValues from ObjectProperties can be handled
			if (prop.isDatatypeProperty()) {
				log.warn(LogUtils.toLogName(prop) + ": Not creating hasValue restriction on datatype property");
				return;
			}
			HasValueRestriction hasValueRestriction = res.asHasValueRestriction();
			RDFNode hasValueResource = hasValueRestriction.getHasValue();
			jRestriction.getOtherRestriction().addHasValue(hasValueResource.toString());
			log.debug(DebugUtils.logPropertyOnClass(cls, prop) + ": Added hasValue restriction:"
					+ hasValueResource.toString());
			log.warn(DebugUtils.logPropertyOnClass(cls, prop) + ": HasValueRestriction currently ignored");
		}

		if (res.isSomeValuesFromRestriction()) {
			// TODO: currently, only allValues from ObjectProperties can be handled
			if (prop.isDatatypeProperty()) {
				log.warn(LogUtils.toLogName(prop) + ": Not creating someValues restriction on datatype property");
				return;
			}
			SomeValuesFromRestriction someValuesRestriction = res.asSomeValuesFromRestriction();
			Resource someValuesResource = someValuesRestriction.getSomeValuesFrom();
			JClass someValuesJClass = jmodel.getJClass(someValuesResource.getURI());
			jRestriction.getOtherRestriction().addSomeValues(someValuesJClass);
			log.debug(DebugUtils.logPropertyOnClass(cls, prop) + ": Added someValues restriction:"
					+ LogUtils.toLogName(someValuesJClass));
			log.warn(DebugUtils.logPropertyOnClass(cls, prop) + ": SomeValuesRestriction currently ignored");
		}
	}

	@SuppressWarnings("unchecked")
	protected void createProperty(OntProperty ontProperty) {
		log.info(LogUtils.toLogName(ontProperty) + ": Found property");
		log.debug(DebugUtils.logProperty(ontProperty));

		// create the property if not already present
		if (!jmodel.hasJProperty(ontProperty.getURI()))
			jmodel.createJProperty(ontProperty);
		JProperty jProperty = jmodel.getJProperty(ontProperty.getURI());
		jProperty.setOntProperty(ontProperty);

		// set the type of the property
		if (ontProperty.isDatatypeProperty())
			jProperty.setPropertyType(JProperty.DataTypeProperty);
		else
			jProperty.setPropertyType(JProperty.ObjectProperty);

		// property has domain -> add it to the correct domain class
		boolean domainProp = false;
		ExtendedIterator dIt = ontProperty.listDomain();
		while (dIt.hasNext()) {
			OntResource domain = (OntResource) dIt.next();
			log.debug(LogUtils.toLogName(ontProperty) + ": Found domain " + LogUtils.toLogName(domain));

			if (domain.isAnon()) {
				// anonymous classes as domain > done in handleAnonClasses
				log.debug(LogUtils.toLogName(ontProperty) + ": Domain is Anonymous class. Ignored.");
				continue;
			}

			JClass domainCls = jmodel.getJClass(domain.getURI());
			domainCls.addDomainProperty(jProperty);
			domainProp = true;
			log.debug(LogUtils.toLogName(ontProperty) + ": Registering as domain property in  class "
					+ LogUtils.toLogName(domainCls));
		}

		// not a domain property > add it to the base thing (root) object
		if (!domainProp) {
			JClass domainCls = jmodel.getJClass(jmodel.getBaseThingUri());
			domainCls.addDomainProperty(jProperty);
			log.debug(LogUtils.toLogName(ontProperty) + ": Registering property without domain in "
					+ LogUtils.toLogName(domainCls));
		}

		// property has range -> add range to property
		Iterator rIt = ontProperty.listRange();
		while (rIt.hasNext()) {
			OntResource range = (OntResource) rIt.next();
			log.debug(LogUtils.toLogName(ontProperty) + ": Found range " + LogUtils.toLogName(range));

			// Range points to a valid class -> object property
			if (jmodel.hasJClass(range.getURI())) {
				jProperty.setPropertyType(JProperty.ObjectProperty);
				jProperty.addRange(jmodel.getJClass(range.getURI()));

				log.debug(LogUtils.toLogName(ontProperty) + ": Registering class " + LogUtils.toLogName(range)
						+ " as range");

			} else if (ontProperty.isDatatypeProperty()) {
				// data type property
				jProperty.setPropertyType(JProperty.DataTypeProperty);
				jProperty.addRange(range.getURI());
				log.debug(LogUtils.toLogName(ontProperty) + ": Registering " + LogUtils.toLogName(range) + " as range");
			}
		}

		// property is functional -> set to functional
		if (ontProperty.isFunctionalProperty()) {
			log.debug(LogUtils.toLogName(ontProperty) + ": Is a functional property. Marking it. ");
			jProperty.setFunctional(true);
		}

		// property is inverse functional -> we do not handle this case
		if (ontProperty.isInverseFunctionalProperty()) {
			log.warn(LogUtils.toLogName(ontProperty) + ": Is a inverse functional property. Ignored");
			jProperty.setInverseFunctional(true);
		}

		// property is symetric -> this is handled via owl:inverseOf,
		// rdfs:range, rdfs:domain
		if (ontProperty.isSymmetricProperty()) {
			log.debug(LogUtils.toLogName(ontProperty) + ": Is a symmetric property: handled elsewhere");
			jProperty.setSymetric(true);
		}

		if (ontProperty.isTransitiveProperty()) {
			log.warn(LogUtils.toLogName(ontProperty) + ": Is a transitive property: IGNORED");
			jProperty.setTransitive(true);
		}

		// property has inverses; mark accordingly
		if (ontProperty.hasInverse()) {
			Iterator inverseIt = ontProperty.listInverse();
			while (inverseIt.hasNext()) {
				OntProperty inverseProp = (OntProperty) inverseIt.next();

				// property already present?
				if (!jmodel.hasJProperty(inverseProp.getURI()))
					jmodel.createJProperty(inverseProp);
				JProperty iProp = jmodel.getJProperty(inverseProp.getURI());
				iProp.setOntProperty(inverseProp);
				iProp.setPropertyType(JProperty.ObjectProperty);

				// mark inverse
				if (!iProp.hasInverseProperty(jProperty)) {
					iProp.addInverseProperty(jProperty);
					log.info(LogUtils.toLogName(ontProperty) + ": Marked as inverse of " + LogUtils.toLogName(iProp));
				} else {
					log.debug(LogUtils.toLogName(ontProperty) + ": Already defined as inverse of "
							+ LogUtils.toLogName(iProp));
				}
			}
		}

		// property has parent properties -> create and mark
		Iterator superIt = ontProperty.listSuperProperties(true);
		while (superIt.hasNext()) {
			OntProperty superProp = (OntProperty) superIt.next();
			log
					.debug(LogUtils.toLogName(ontProperty) + ": Registering super property "
							+ LogUtils.toLogName(superProp));

			// property already present?
			if (!jmodel.hasJProperty(superProp.getURI()))
				jmodel.createJProperty(superProp);
			JProperty sProp = jmodel.getJProperty(superProp.getURI());
			if (superProp.isDatatypeProperty())
				sProp.setPropertyType(JProperty.DataTypeProperty);
			else
				sProp.setPropertyType(JProperty.ObjectProperty);
			sProp.setOntProperty(superProp);
			sProp.addSubProperty(jProperty);
		}

		// property has equivalent properties -> create and link
		Iterator equivalentIt = ontProperty.listEquivalentProperties();
		while (equivalentIt.hasNext()) {
			OntProperty equProp = (OntProperty) equivalentIt.next();

			// property already present?
			if (!jmodel.hasJProperty(equProp.getURI()))
				jmodel.createJProperty(equProp);
			JProperty eProp = jmodel.getJProperty(equProp.getURI());
			if (equProp.isDatatypeProperty())
				eProp.setPropertyType(JProperty.DataTypeProperty);
			else
				eProp.setPropertyType(JProperty.ObjectProperty);

			eProp.setOntProperty(equProp);

			// mark as equivalent
			if (eProp.hasEquivalentProperty(jProperty)) {
				log.debug(LogUtils.toLogName(ontProperty) + ": Registering inverse property "
						+ LogUtils.toLogName(equProp));
				eProp.addEquivalentProperty(jProperty);
			} else {
				log.debug(LogUtils.toLogName(ontProperty) + ": Alredy defined as inverse property of "
						+ LogUtils.toLogName(equProp));
			}
		}

	}

	protected boolean hasBaseThingURI(OntClass ontClass) {
		// check if ontClass.localName = Thing.... This is currently not
		// supported
		String ontClassUri = ontClass.getURI();
		String baseThingUri = jmodel.getBaseNamespace() + JModel.getBaseThingName();

		if (ontClassUri == null)
			return false;

		if (ontClassUri.equals(baseThingUri)) {
			return true;
		}
		return false;
	}

	protected void createJClassish(OntClass ontClass) {
		log.info(LogUtils.toLogName(ontClass) + ": Found owl/rdf class");
		log.debug(DebugUtils.logClass(ontClass));

		// ignore base classes
		if (NamespaceUtils.defaultNs2UriMapping.containsKey(ontClass.getNameSpace())) {
			log.debug(LogUtils.toLogName(ontClass) + ": Is a base owl/rdfs class. Ignored");
			return;
		}

		// abort if name clash with baseThing
		if (hasBaseThingURI(ontClass)) {
			log.error(LogUtils.toLogName(ontClass) + ": An unprefixed class named 'Thing' in "
					+ "the BaseURI namespace is not allowed");
			System.exit(1);
			log.error("Aborting");
		}

		// get or create the JClass
		if (!jmodel.hasJClass(ontClass.getURI()))
			jmodel.createJClass(ontClass, basePackage);
		JClass cls = jmodel.getJClass(ontClass.getURI());
		cls.setOntClass(ontClass);

		// find (or create, if not present) super class and update classes
		ExtendedIterator superIt = ontClass.listSuperClasses(true);
		while (superIt.hasNext()) {
			OntClass superCls = (OntClass) superIt.next();
			hasBaseThingURI(superCls);

			if (superCls.isAnon()) {
				// Anonymous classes (restrictions ...) are handled in
				// handleAnonymousClasses()
				continue;
			}

			JClass superClass;
			if (!jmodel.hasJClass(superCls.getURI()))
				jmodel.createJClass(superCls, basePackage);
			superClass = jmodel.getJClass(superCls.getURI());
			superClass.setOntClass(superCls);

			superClass.addSubClass(cls);
			log.debug(LogUtils.toLogName(ontClass) + ": Registering super class "
					+ NamingUtils.getJavaFullName(superClass.getPackage(), superClass.getName()));
		}

		// if no superclasses are given, register as subclass of basething
		if (!cls.hasSuperClasses()) {
			log.debug(LogUtils.toLogName(ontClass) + ": No parent class given.");
			JClass superClass = jmodel.getJClass(jmodel.getBaseThingUri());
			superClass.addSubClass(cls);
			log.debug(LogUtils.toLogName(ontClass) + ": Registering super class "
					+ NamingUtils.getJavaFullName(superClass.getPackage(), superClass.getName()));
		}

		// find (or create, if not present) equivalent class definitions
		ExtendedIterator equIt = ontClass.listEquivalentClasses();
		while (equIt.hasNext()) {
			OntClass equCls = (OntClass) equIt.next();

			if (equCls.isAnon()) {
				log.warn("Currently, only primitive equivalent class definitions are used (OWL Lite)");
				// Currently, only primite equivalent classes are accepted
				continue;
			}

			// get or create the equivalent class.
			JClass equClass;
			if (!jmodel.hasJClass(equCls.getURI()))
				jmodel.createJClass(equCls, basePackage);
			equClass = jmodel.getJClass(equCls.getURI());
			equClass.setOntClass(equCls);

			equClass.addEquivalentClass(cls);
			log.debug(LogUtils.toLogName(ontClass) + ": Registering equivalent class "
					+ NamingUtils.getJavaFullName(equClass.getPackage(), equClass.getName()));
		}

	}

	public JModel generateJModel(OntModel model) {
		this.jmodel = new JModel();
		this.jmodel.setOntModel(model);
		this.ontModel = model;

		// find namespaces and define the corresponding packages
		createJPackages();

		// find all defined classes and populate the JModel with the
		// corresponding JClass instances
		handleClassishObjects();

		// find all anonymous classes that are not restrictions.
		// if possible create the corresponding JModel classes.
		handleAnonymousClasses();

		// Dito for properties
		handleProperties();

		// now we handle the properties of any intersection class
		handleDeferredIntersectionClasses();

		// handle multiple ranges for Properties
		handlePropertyRanges();

		// Finally, we handle the restrictions etc... and add them
		// to the JModel
		handleRestrictions();

		return this.jmodel;
	}

	protected void handleDeferredIntersectionClasses() {
		for (JClass cls : deferredIntersectionClasses) {
			handleIntersectionClassProperties(cls);
		}
	}

	@SuppressWarnings("unchecked")
	protected void handlePropertyRanges() {
		log.info("");
		log.info("Checking for multiple ranges of object properties");
		Iterator propertyIt = jmodel.getUri2property().keySet().iterator();
		while (propertyIt.hasNext()) {
			String propUri = (String) propertyIt.next();
			JProperty prop = jmodel.getUri2property().get(propUri);

			if (prop.getPropertyType() == JProperty.DataTypeProperty)
				continue;

			if (prop.listObjectPropertyRange().size() < 2)
				continue;

			// XXX Multiple property range should use a intersection class instead of a union class
			// -> not implemented, yet code present
			// -> see the owl standard
			log.info(LogUtils.toLogName(prop) + ": Found multiple range. Replacing with UnionClass ");

			List<JClass> operandClasses = prop.listObjectPropertyRange();
			JClass cls = jmodel.getAnonymousJClass(operandClasses);

			// an identical anonymous class exists > we use it
			if (cls != null) {
				log.info("Reusing existing anonymous class " + LogUtils.toLogName(cls));
			} else {
				String anonClassName = NamingUtils.createUnionClassName(cls, operandClasses);
				String namespace = jmodel.getBaseNamespace();
				String anonClassUri = namespace + anonClassName;

				// create class
				if (!jmodel.hasJClass(anonClassUri))
					jmodel.createJClass(anonClassName, anonClassUri, basePackage);
				cls = jmodel.getJClass(anonClassUri);
				cls.setAnonymous(true);
			}

			// register range as super classes in class
			for (JClass superCls : prop.listObjectPropertyRange()) {
				superCls.addSubClass(cls);
				log.debug(LogUtils.toLogName(cls) + ": Registering super class "
						+ NamingUtils.getJavaFullName(superCls.getPackage(), superCls.getName()));
			}

			// reset range for property to new union class
			prop.listObjectPropertyRange().clear();
			prop.addRange(cls);
			log.debug(LogUtils.toLogName(prop) + ": Setting range to " + cls.getName());
		}

	}

	public JModel getJModel() {
		return jmodel;
	}

	@SuppressWarnings("unchecked")
	protected void handleAnonymousClasses() {
		// > loop over all anonymous classes and do some magic
		log.info("");
		log.info("Analyzing anonymous classes");

		List list = ontModel.listClasses().toList();
		for (int i = 0; i < list.size(); i++) {
			OntClass ontClass = (OntClass) list.get(i);

			// skip all non anonymous classes
			if (!ontClass.isAnon())
				continue;

			// dito for restrictions -> this is done in handleRestrictions()
			if (ontClass.isRestriction())
				continue;

			// handle the union class
			if (ontClass.isUnionClass())
				createUnionClass(ontClass.asUnionClass());

			if (ontClass.isComplementClass()) {
				jmodel.addOntResourceError(new ResourceError(ontClass, "ComplementClass ignored"));
				log.warn("Found non restriction anonymous class: " + "ComplementClass ignored");
			}

			if (ontClass.isIntersectionClass()) {
				log.info(LogUtils.toLogName(ontClass)
						+ ": Is intersection class. Handled as multiple subClassOf definitions.");
				log.info(LogUtils.toLogName(ontClass) + ": ---> This should be done by a reasoner!");
				
			// TODO createIntersectionClass currently unused; use pellet for this
			// if (ontClass.canAs(IntersectionClass.class))
			//  	createIntersectionClass(ontClass.asIntersectionClass());
			}

			if (ontClass.isEnumeratedClass()) {
				jmodel.addOntResourceError(new ResourceError(ontClass, "Enumerated class handled as simple class"));
				log.warn("Found non restriction anonymous class: " + "EnumeratedClass handled " + "as simple class");
			}

		}

	}

	@SuppressWarnings("unchecked")
	protected JClass createIntersectionClass(IntersectionClass intersectionClass) {
		// do we have anonymous operands in this class? this, we can not handle
		// right now. -> we skip it

		// get or create the intersection class
		JClass cls = jmodel.getAnonymousJClassIntersection(intersectionClass);
		// an identical intersection class exists > we use it
		if (cls != null)
			log.info("Reusing existing anonymous intersection class " + LogUtils.toLogName(cls));
		else {
			// no identical intersection class exist > create new
			String anonClassName = NamingUtils.createIntersectionClassName(intersectionClass);
			String namespace = jmodel.getBaseNamespace();
			String anonClassUri = namespace + anonClassName;

			// rename the anon. class to a named class,
			log.info("Renaming anonymous intersection class to :" + anonClassUri);
			ResourceUtils.renameResource(intersectionClass, anonClassUri);
			intersectionClass = ontModel.getOntClass(anonClassUri).asIntersectionClass();

			// create class
			if (!jmodel.hasJClass(intersectionClass.getURI()))
				jmodel.createJClass(intersectionClass, basePackage);
			cls = jmodel.getJClass(intersectionClass.getURI());
			cls.setAnonymous(true);
			cls.setOntClass(intersectionClass);
		}

		// -> we can handle the intersection class; do so now but skip anonymous
		// operands

		// register new intersection class as parent of this class
		Iterator subIt = intersectionClass.listSubClasses();
		while (subIt.hasNext()) {
			OntClass ontCls = (OntClass) subIt.next();
			String ontUri = ontCls.getURI();
			JClass subCls = jmodel.getJClass(ontUri);
			subCls.addSuperClass(cls);
			log.debug(LogUtils.toLogName(intersectionClass) + ": Registering sub class "
					+ NamingUtils.getJavaFullName(subCls.getPackage(), subCls.getName()));
		}

		// register operand classes as child classes of new intersection
		// class
		Iterator operandsIt = intersectionClass.listOperands();
		while (operandsIt.hasNext()) {
			OntClass ontCls = (OntClass) operandsIt.next();

			if (ontCls.isAnon())
				continue;

			String ontUri = ontCls.getURI();
			JClass operandCls = jmodel.getJClass(ontUri);

			operandCls.addSuperClass(cls);
			log.debug(LogUtils.toLogName(intersectionClass) + ": Registering sub class "
					+ LogUtils.toLogName(operandCls));
		}

		// -> move all parent classes up one level to new intersection class

		// register super classes of intersection class as super class of this
		// store reassigned super classes in a list
		List<JClass> reassignedSuperClasses = new ArrayList<JClass>();
		operandsIt = intersectionClass.listOperands();
		while (operandsIt.hasNext()) {
			OntClass operandCls = (OntClass) operandsIt.next();
			Iterator superIt = operandCls.listSuperClasses();
			while (superIt.hasNext()) {
				OntClass ontCls = (OntClass) superIt.next();
				String ontUri = ontCls.getURI();
				JClass superCls = jmodel.getJClass(ontUri);

				// add new intersection class as subclass of super class
				log.debug(LogUtils.toLogName(intersectionClass) + ": Registering super class "
						+ LogUtils.toLogName(superCls));
				superCls.addSubClass(cls);
				reassignedSuperClasses.add(superCls);
			}
		}

		// remove reassigned super class relationships from operand classes
		// operand classes are now sub class of intersection class
		operandsIt = intersectionClass.listOperands();
		while (operandsIt.hasNext()) {
			OntClass ontCls = (OntClass) operandsIt.next();
			// skip any anonymous class
			if (ontCls.isAnon())
				continue;
			String ontUri = ontCls.getURI();
			JClass operandCls = jmodel.getJClass(ontUri);

			// remove all operandClass.superclass in reassignedSuperClasses
			for (JClass oldSuperClass : reassignedSuperClasses) {
				if (operandCls.hasSuperClass(oldSuperClass, false)) {
					log.debug(LogUtils.toLogName(operandCls) + ": Removing old super class relation "
							+ LogUtils.toLogName(oldSuperClass));
					operandCls.removeSuperClassRelation(oldSuperClass);
				}
			}
		}

		// add the class for deferred handling of properties
		deferredIntersectionClasses.add(cls);
		return cls;
	}

	@SuppressWarnings("unchecked")
	protected void handleIntersectionClassProperties(JClass cls) {
		IntersectionClass intersectionClass = cls.getOntClass().asIntersectionClass();
		Iterator operandsIt;

		// reassign property domain for common properties to intersection class
		// find all properties
		List<JProperty> properties = new ArrayList<JProperty>();
		List<JProperty> propertiesToRemove = new ArrayList<JProperty>();
		operandsIt = intersectionClass.listOperands();
		while (operandsIt.hasNext()) {
			OntClass ontCls = (OntClass) operandsIt.next();
			if (ontCls.isAnon())
				continue;
			String ontUri = ontCls.getURI();
			JClass operandCls = jmodel.getJClass(ontUri);
			for (JProperty p : operandCls.listDomainProperties())
				properties.add(p);
		}
		// mark any property not present in at least one class for removal
		for (JProperty p : properties) {
			operandsIt = intersectionClass.listOperands();
			while (operandsIt.hasNext()) {
				OntClass ontCls = (OntClass) operandsIt.next();
				String ontUri = ontCls.getURI();
				JClass operandCls = jmodel.getJClass(ontUri);

				if (!operandCls.hasDomainProperty(p))
					// property p not present in operandCls -> mark for removal
					propertiesToRemove.add(p);
			}
		}
		// remove all properties marked for removal
		for (JProperty p : propertiesToRemove)
			properties.remove(p);

		// now we have a list of all common properties -> we can reassign them
		for (JProperty p : properties) {
			// add intersection class as domain
			cls.addDomainProperty(p);
			log.debug(LogUtils.toLogName(p) + ": Reassigning to intersection class " + LogUtils.toLogName(cls));

			// remove operand class from domain of p
			operandsIt = intersectionClass.listOperands();
			while (operandsIt.hasNext()) {
				OntClass ontCls = (OntClass) operandsIt.next();
				String ontUri = ontCls.getURI();
				JClass operandCls = jmodel.getJClass(ontUri);

				operandCls.removeDomainProperty(p);
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected JClass createUnionClass(UnionClass unionClass) {
		JClass cls = jmodel.getAnonymousJClassUnion(unionClass);
		// an identical anonymous class exists > we use it
		if (cls != null) {
			log.info("Reusing existing anonymous union class " + LogUtils.toLogName(cls));
		} else {
			// create a new anonymous class
			String anonClassName = NamingUtils.createUnionClassName(unionClass);
			String namespace = jmodel.getBaseNamespace();
			String anonClassUri = namespace + anonClassName;

			// rename the anon. class to a named class,
			log.info("Renaming anonymous union class to :" + anonClassUri);
			ResourceUtils.renameResource(unionClass, anonClassUri);
			unionClass = ontModel.getOntClass(anonClassUri).asUnionClass();

			// create class
			if (!jmodel.hasJClass(unionClass.getURI()))
				jmodel.createJClass(unionClass, basePackage);
			cls = jmodel.getJClass(unionClass.getURI());
			cls.setAnonymous(true);
			cls.setOntClass(unionClass);
		}

		// register the new union class as parent of this class
		Iterator subIt = unionClass.listSubClasses();
		while (subIt.hasNext()) {
			OntClass ontCls = (OntClass) subIt.next();
			String ontUri = ontCls.getURI();
			JClass subCls = jmodel.getJClass(ontUri);
			subCls.removeSuperClassRelation(jmodel.getBaseThing());
			subCls.addSuperClass(cls);
			log.debug(LogUtils.toLogName(unionClass) + ": Registering sub class "
					+ NamingUtils.getJavaFullName(subCls.getPackage(), subCls.getName()));
		}
		

		// get super classes and register them
		Iterator operandIt = unionClass.listOperands();
		while (operandIt.hasNext()) {
			OntClass superClass = (OntClass) operandIt.next();

			JClass superCls;
			if (!jmodel.hasJClass(superClass.getURI()))
				jmodel.createJClass(superClass, basePackage);
			superCls = jmodel.getJClass(superClass.getURI());
			superCls.setOntClass(superClass);
			superCls.addSubClass(cls);
			log.debug(LogUtils.toLogName(unionClass) + ": Registering super class "
					+ NamingUtils.getJavaFullName(superCls.getPackage(), superCls.getName()));
		}
		return cls;
	}

	@SuppressWarnings("unchecked")
	protected void handleClassishObjects() {
		// create our base object "Thing" and add as class
		createBaseJClassish();

		// add all named classes to the JModel
		log.info("");
		log.info("Found " + ontModel.listNamedClasses().toList().size() + " named classes");

		// handle the ontology classes
		Iterator<OntClass> it = ontModel.listNamedClasses();
		while (it.hasNext()) {
			OntClass ontClass = (OntClass) it.next();
			createJClassish(ontClass);
		}
	}

	protected void createBaseJClassish() {
		// create our base object "Thing" and add as class
		String thingUri;
		String thingName = JModel.getBaseThingName();
		thingUri = ontModel.getNsPrefixURI("owl") + thingName;

		jmodel.setBaseThingUri(thingUri);
		jmodel.createJClass(thingName, thingUri, basePackage);
	}

	@SuppressWarnings("unchecked")
	protected void handleNamespaces() {
		// find the namespaces and add them to our namespace2prefix mapping
		Iterator it = this.ontModel.getNsPrefixMap().keySet().iterator();
		while (it.hasNext()) {
			String prefix = (String) it.next();
			String uri = this.ontModel.getNsPrefixURI(prefix);

			jmodel.addNamespacePrefix(uri, prefix);
			log.info("Adding prefix " + prefix + " for namespace " + uri);
		}

		// handle name spaces without prefix
		Iterator importedUriIt = ontModel.listImportedOntologyURIs(true).iterator();
		while (importedUriIt.hasNext()) {
			String importedUri = (String) importedUriIt.next() + "#";
			if (!jmodel.hasNamespace(importedUri)) {
				String ns = importedUri;
				log.info("Found namespace without prefix: " + ns);
				String prefix = jmodel.createNewPrefix();
				jmodel.addNamespacePrefix(ns, prefix);
				ontModel.setNsPrefix(prefix, ns);
				log.info("Adding auto-generated prefix " + prefix + " for namespace " + ns);
			}
		}

	}

	@SuppressWarnings("unchecked")
	protected void handleProperties() {
		Iterator it;
		log.info("");

		log.info("Found " + ontModel.listObjectProperties().toList().size() + " object properties");
		it = ontModel.listObjectProperties();
		handleProperties(it);

		log.info("Found " + ontModel.listTransitiveProperties().toList().size() + " object properties");
		it = ontModel.listTransitiveProperties();
		handleProperties(it);


		log.info("Found " + ontModel.listFunctionalProperties().toList().size() + " functional properties");
		it = ontModel.listFunctionalProperties();
		handleProperties(it);

		log.info("Found " + ontModel.listInverseFunctionalProperties().toList().size()
				+ " inverse functional properties");
		it = ontModel.listInverseFunctionalProperties();
		handleProperties(it);

		log.info("Found " + ontModel.listSymmetricProperties().toList().size() + " symetrical properties");
		it = ontModel.listSymmetricProperties();
		handleProperties(it);

		log.info("Found " + ontModel.listDatatypeProperties().toList().size() + " datatype properties");
		it = ontModel.listDatatypeProperties();
		handleProperties(it);
	}

	@SuppressWarnings("unchecked")
	protected void handleProperties(Iterator propertiesIt) {
		while (propertiesIt.hasNext()) {
			OntProperty ontProperty = (OntProperty) propertiesIt.next();
			createProperty(ontProperty);
		}
	}

	@SuppressWarnings("unchecked")
	protected void handleRestrictions() {

		// > loop over all restrictions and do some magic
		log.info("");
		log.info("Analyzing restriction classes");

		Iterator<OntClass> it = ontModel.listClasses();
		while (it.hasNext()) {
			OntClass cls = it.next();

			// skip all non anonymous classes
			if (!cls.isAnon())
				continue;
			
			// we have a restriction
			if (cls.isRestriction()) {
				// find the restriction and the property it acts on
				Restriction ontRestriction = cls.asRestriction();
				OntProperty ontProperty = ontRestriction.getOnProperty();

				log.info("Found Restriction on Property " + LogUtils.toLogName(ontRestriction.getOnProperty()));
				log.debug(DebugUtils.logRestriction(ontRestriction));

				// find the classes it acts on
				Iterator subClassIt = ontRestriction.listSubClasses();
				while (subClassIt.hasNext()) {
					OntClass ontClass = (OntClass) subClassIt.next();

					// Any anonymous class as subject of restriction is ignored. Not supported
					// this can be union or intersection etc.
					if (ontClass.getURI() == null) {
						log.info(LogUtils.toLogName(ontClass) + ": Anonymous restriction class. Ignored");
						continue;
					}

					// owl:Thing -> ignore
					if (NamespaceUtils.defaultNs2UriMapping.containsKey(ontClass.getNameSpace())) {
						log.debug(LogUtils.toLogName(ontClass) + ": Is a base class. Ignored");
						continue;
					}

					createJRestriction(ontRestriction, ontClass, ontProperty);
				}
			}
		}
	}

	public void setBasePackage(String basePackage) {
		this.basePackage = basePackage;
	}

}
