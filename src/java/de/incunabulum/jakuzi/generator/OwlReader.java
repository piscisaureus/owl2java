package de.incunabulum.jakuzi.generator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.AllValuesFromRestriction;
import com.hp.hpl.jena.ontology.CardinalityRestriction;
import com.hp.hpl.jena.ontology.MaxCardinalityRestriction;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import de.incunabulum.jakuzi.jmodel.JClass;
import de.incunabulum.jakuzi.jmodel.JClassRestriction;
import de.incunabulum.jakuzi.jmodel.JModel;
import de.incunabulum.jakuzi.jmodel.JPackage;
import de.incunabulum.jakuzi.jmodel.JProperty;
import de.incunabulum.jakuzi.jmodel.utils.DebugUtils;
import de.incunabulum.jakuzi.jmodel.utils.NamingUtils;
import de.incunabulum.jakuzi.model.NamespaceUtils;
import de.incunabulum.jakuzi.model.ResourceError;

public class OwlReader {

	// TODO: equivalent classes, properties ignored

	private static Log log = LogFactory.getLog(OwlReader.class);

	private OntModel ontModel;

	private String basePackage;
	public JModel jmodel;
	private boolean reasignDomainlessProperties;

	private List<String> forbiddenPrefixes = new ArrayList<String>();

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
		Iterator<String> nsIt = jmodel.ns2prefix.keySet().iterator();
		while (nsIt.hasNext()) {
			String uri = nsIt.next();
			String prefix = jmodel.ns2prefix.get(uri);

			// Don't create packages for default namespaces
			if (NamespaceUtils.defaultNs2UriMapping.containsKey(uri))
				continue;

			if (forbiddenPrefixes.contains(prefix)) {
				log.error("Prefix " + prefix + " is identical with a system internal prefix (toolspackage?). Aborting!");
				System.exit(1);
			}

			// empty prefix (base uri) > assign to base package
			if (prefix == JModel.getBasePrefix()) {
				log.info("Assigning namespace " + uri + " to base package " + basePackage);
				jmodel.ns2javaPkgName.put(uri, basePackage);
				continue;
			}

			String pkgName = NamingUtils.getJavaPackageName(basePackage, prefix);
			log.info("Generating package " + pkgName);
			JPackage p = new JPackage(jmodel, pkgName);
			this.jmodel.addPackage(pkgName, p);
			jmodel.ns2javaPkgName.put(uri, pkgName);
		}

	}

	protected void createJRestriction(Restriction res, OntClass cls, OntProperty prop) {
		JClass jclass = jmodel.getJClass(cls.getURI());
		JProperty jprop = jmodel.getJProperty(prop.getURI());
		JClassRestriction jRestriction = new JClassRestriction(jmodel, jclass, jprop);

		// max. cardinality
		if (res.isMaxCardinalityRestriction()) {
			MaxCardinalityRestriction maxCardinalityRestriction = res.asMaxCardinalityRestriction();
			int maxCardinality = maxCardinalityRestriction.getMaxCardinality();
			jRestriction.setMaxCardinality(maxCardinality);
			log.debug(DebugUtils.logPropertyOnClass(cls, prop) + ": Max cardinality set to " + maxCardinality);
		}

		// min cardinality -> skipped, does not make sense
		if (res.isMinCardinalityRestriction()) {
			log.debug(DebugUtils.logPropertyOnClass(cls, prop) + ": Min cardinality ignored");
		}

		// exactly -> only useful for exactly = 1
		if (res.isCardinalityRestriction()) {
			CardinalityRestriction cardinalityRestriction = res.asCardinalityRestriction();
			int cardinality = cardinalityRestriction.getCardinality();
			if (cardinality == 1) {
				jRestriction.setMaxCardinality(1);
				jRestriction.setMinCardinality(1);
				log.debug(DebugUtils.logPropertyOnClass(cls, prop) + ": Cardinality (min=max) set to " + cardinality);
			} else {
				jRestriction.setMaxCardinality(cardinality);
				log.debug(DebugUtils.logPropertyOnClass(cls, prop) + ": Cardinality set to max=" + cardinality + ", min="
						+ cardinality + " is ignored");
			}
		}

		// All values
		if (res.isAllValuesFromRestriction()) {
			AllValuesFromRestriction allValuesRestriction = res.asAllValuesFromRestriction();
			Resource allValuesResource = allValuesRestriction.getAllValuesFrom();
			JClass allValuesJClass = jmodel.getJClass(allValuesResource.getURI());
			jRestriction.setAllValuesDomain(allValuesJClass);
			log.debug(DebugUtils.logPropertyOnClass(cls, prop) + ": All values restriction set to:"
					+ NamingUtils.toLogName(allValuesJClass));
		}

		if (res.isHasValueRestriction()) {
			jmodel.ontResourceErrors.add(new ResourceError(res, "HasValueRestriction ignored"));
			log.warn(DebugUtils.logPropertyOnClass(cls, prop) + ": HasValueRestriction ignored");
		}

		if (res.isSomeValuesFromRestriction()) {
			jmodel.ontResourceErrors.add(new ResourceError(res, "SomeValuesRestriction ignored"));
			log.warn(DebugUtils.logPropertyOnClass(cls, prop) + ": SomeValuesRestriction ignored");
		}
	}

	@SuppressWarnings("unchecked")
	protected void createProperty(OntProperty ontProperty) {
		log.debug(NamingUtils.toLogName(ontProperty) + ": Found property");
		log.info(DebugUtils.logProperty(ontProperty));

		// create the property if not already present
		if (!jmodel.hasJProperty(ontProperty.getURI()))
			jmodel.createJProperty(ontProperty);
		JProperty prop = jmodel.getJProperty(ontProperty.getURI());
		prop.setOntProperty(ontProperty);

		if (ontProperty.isDatatypeProperty())
			prop.setPropertyType(JProperty.DataTypeProperty);
		else
			prop.setPropertyType(JProperty.ObjectProperty);

		// property has domain -> add it to the correct domain class
		boolean domainProp = false;
		ExtendedIterator dIt = ontProperty.listDomain();
		while (dIt.hasNext()) {
			OntResource domain = (OntResource) dIt.next();
			log.debug(NamingUtils.toLogName(ontProperty) + ": Found domain " + NamingUtils.toLogName(domain));

			if (domain.isAnon()) {
				// anonymous classes as domain > handleAnonClasses
				log.debug(NamingUtils.toLogName(ontProperty) + ": Domain is Anonymous class. Ignored.");
				continue;
			}

			JClass domainCls = jmodel.getJClass(domain.getURI());
			domainCls.addDomainProperty(prop);
			domainProp = true;
			log.debug(NamingUtils.toLogName(ontProperty) + ": Registering as domain property in  class "
					+ NamingUtils.toLogName(domainCls));
		}

		// not a domain property > add it to the base thing (root) object
		if (!domainProp) {
			JClass domainCls = jmodel.getJClass(jmodel.baseThingUri);
			domainCls.addDomainProperty(prop);
			log.debug(NamingUtils.toLogName(ontProperty) + ": Registering property without domain in "
					+ NamingUtils.toLogName(domainCls));
		}

		// property has range -> add range to property
		Iterator rIt = ontProperty.listRange();
		while (rIt.hasNext()) {
			OntResource range = (OntResource) rIt.next();
			log.debug(NamingUtils.toLogName(ontProperty) + ": Found range " + NamingUtils.toLogName(range));

			// Range points to a valid class -> object property
			if (jmodel.uri2class.containsKey(range.getURI())) {
				prop.setPropertyType(JProperty.ObjectProperty);
				prop.addRange(jmodel.getJClass(range.getURI()));

				log.debug(NamingUtils.toLogName(ontProperty) + ": Registering class " + NamingUtils.toLogName(range)
						+ " as range");
				// data type property
			} else if (ontProperty.isDatatypeProperty()) {
				prop.setPropertyType(JProperty.DataTypeProperty);
				prop.addRange(range.getURI());
				log.debug(NamingUtils.toLogName(ontProperty) + ": Registering " + NamingUtils.toLogName(range) + " as range");
			}

		}

		// property is functional -> set max cardinality = 1
		if (ontProperty.isFunctionalProperty()) {
			log.debug(NamingUtils.toLogName(ontProperty) + ": Is a functional property. Marking it. ");
			prop.setFunctional(true);
		}

		// property is inverse functional -> set max caridinality = 1;
		// inverse handled below
		if (ontProperty.isInverseFunctionalProperty()) {
			log.debug(NamingUtils.toLogName(ontProperty) + ": Is a inverse functional property. Marking it functional");
			prop.setFunctional(true);
		}

		// property is symetric -> this is handled via owl:inverseOf,
		// rdfs:range, rdfs:domain
		if (ontProperty.isSymmetricProperty()) {
			log.debug(NamingUtils.toLogName(ontProperty) + ": Is a symmetric property: handled elsewhere");
		}

		if (ontProperty.isTransitiveProperty()) {
			log.warn(NamingUtils.toLogName(ontProperty) + ": Is a transitive property: IGNORED");
		}

		// property has inverses; mark accordingly
		if (ontProperty.hasInverse()) {
			Iterator inverseIt = ontProperty.listInverse();
			while (inverseIt.hasNext()) {
				OntProperty inverseProp = (OntProperty) inverseIt.next();

				// property already present?
				if (!jmodel.uri2property.containsKey(inverseProp.getURI()))
					jmodel.createJProperty(inverseProp);
				JProperty iProp = jmodel.getJProperty(inverseProp.getURI());
				iProp.setOntProperty(inverseProp);

				// mark inverse
				if (!iProp.hasInverseProperty(prop)) {
					iProp.addInverse(prop);
					log.debug(NamingUtils.toLogName(ontProperty) + ": Marked as inverse of " + NamingUtils.toLogName(iProp));
				} else {
					log.debug(NamingUtils.toLogName(ontProperty) + ": Already defined as inverse of "
							+ NamingUtils.toLogName(iProp));
				}
			}
		}

		// property has parent properties -> create and mark
		Iterator superIt = ontProperty.listSuperProperties(true);
		while (superIt.hasNext()) {
			OntProperty superProp = (OntProperty) superIt.next();
			log.debug(NamingUtils.toLogName(ontProperty) + ": Registering super property " + NamingUtils.toLogName(superProp));

			// property already present?
			if (!jmodel.uri2property.containsKey(superProp.getURI()))
				jmodel.createJProperty(superProp);
			JProperty sProp = jmodel.getJProperty(superProp.getURI());
			sProp.setOntProperty(superProp);
			sProp.addSubProperty(prop);
		}
	}

	protected void abortIfIdenticalWithBaseThing(OntClass ontClass) {
		// check if ontClass.localName = Thing.... This is currently not
		// supported
		String ontClassUri = ontClass.getURI();
		String baseThingUri = jmodel.getBaseNamespace() + JModel.getBaseThingName();

		if (ontClassUri == null)
			return;

		if (ontClassUri.equals(baseThingUri)) {
			log.error(NamingUtils.toLogName(ontClass)
					+ ": An unprefixed class named 'Thing' in the BaseURI namespace is not allowed");
			System.exit(1);
			log.error("Aborting");
		}
	}

	protected void createJClassish(OntClass ontClass) {
		log.info(NamingUtils.toLogName(ontClass) + ": Found owl/rdf class");
		log.debug(DebugUtils.logClass(ontClass));

		if (NamespaceUtils.defaultNs2UriMapping.containsKey(ontClass.getNameSpace())) {
			log.debug(NamingUtils.toLogName(ontClass) + ": Is a base owl/rdfs class. Ignored");
			return;
		}

		abortIfIdenticalWithBaseThing(ontClass);

		if (!jmodel.hasJClass(ontClass.getURI()))
			jmodel.createJClass(ontClass, basePackage);
		JClass cls = jmodel.getJClass(ontClass.getURI());
		cls.setOntClass(ontClass);

		// find (or create, if not present) super class and update
		// classes
		ExtendedIterator superIt = ontClass.listSuperClasses(true);
		while (superIt.hasNext()) {
			OntClass superCls = (OntClass) superIt.next();

			abortIfIdenticalWithBaseThing(superCls);

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
			log.debug(NamingUtils.toLogName(ontClass) + ": Registering super class "
					+ NamingUtils.getJavaFullName(superClass.getPackage(), superClass.getName()));
		}

		// if no superclasses are given, register as subclass of basething
		if (ontClass.listSuperClasses().toList().size() == 0) {
			log.debug(NamingUtils.toLogName(ontClass) + ": No parent class given.");
			JClass superClass = jmodel.getJClass(jmodel.baseThingUri);
			superClass.addSubClass(cls);
			log.debug(NamingUtils.toLogName(ontClass) + ": Registering super class "
					+ NamingUtils.getJavaFullName(superClass.getPackage(), superClass.getName()));
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

		// handle multiple ranges for Properties
		handlePropertyRanges();

		// Finally, we handle the restrictions etc... and add them
		// to the JModel
		handleRestrictions();

		if (reasignDomainlessProperties) {
			// TODO: parse restrictions; assign domainless properties to
			// restricting class
		}

		return this.jmodel;
	}

	@SuppressWarnings("unchecked")
	protected void handlePropertyRanges() {
		log.info("");
		log.info("Checking for multiple ranges of object properties");

		Iterator propertyIt = jmodel.uri2property.keySet().iterator();
		while (propertyIt.hasNext()) {
			String propUri = (String) propertyIt.next();
			JProperty prop = jmodel.uri2property.get(propUri);

			if (prop.getPropertyType() == JProperty.DataTypeProperty)
				continue;

			if (prop.listRange().size() < 2)
				continue;

			// TODO: this is not valid (owl standard), should use intersection instead
			log.info(NamingUtils.toLogName(prop) + ": Found multiple range. Replacing with UnionClass ");

			List<JClass> operandClasses = prop.listRange();
			JClass cls = jmodel.getAnonymousJClass(operandClasses);
			// an identical anonymous class exists > we use it
			if (cls != null) {
				log.info("Reusing existing anonymous class " + NamingUtils.toLogName(cls));
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
			for (JClass superCls : prop.listRange()) {
				superCls.addSubClass(cls);
				log.debug(NamingUtils.toLogName(cls) + ": Registering super class "
						+ NamingUtils.getJavaFullName(superCls.getPackage(), superCls.getName()));
			}

			// reset range for property to new union class
			prop.listRange().clear();
			prop.addRange(cls);
			log.debug(NamingUtils.toLogName(prop) + ": Setting range to " + cls.getName());
		}

	}

	public JModel getJModel() {
		return jmodel;
	}

	@SuppressWarnings("unchecked")
	protected void handleAnonymousClasses() {
		// > loop over all anonymous classes and do some magic
		log.info("");
		log.info("Analyzing ananymous classes");

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
			if (ontClass.isUnionClass()) {
				JClass cls = jmodel.getAnonymousJClass(ontClass.asUnionClass());
				// an identical anonymous class exists > we use it
				if (cls != null) {
					log.info("Reusing existing anonymous class " + NamingUtils.toLogName(cls));
				} else {
					String anonClassName = NamingUtils.createUnionClassName(ontClass.asUnionClass());
					String namespace = jmodel.getNamespace("");
					String anonClassUri = namespace + anonClassName;

					// rename the anon. class to a named class,
					log.info("Renaming anonymous union class to :" + anonClassUri);
					ResourceUtils.renameResource(ontClass, anonClassUri);
					ontClass = ontModel.getOntClass(anonClassUri);

					// create class
					if (!jmodel.hasJClass(ontClass.getURI()))
						jmodel.createJClass(ontClass, basePackage);
					cls = jmodel.getJClass(ontClass.getURI());
					cls.setAnonymous(true);
					cls.setOntClass(ontClass);
				}

				// register the new anonymous class as parent of this class
				Iterator subIt = ontClass.asUnionClass().listSubClasses();
				while (subIt.hasNext()) {
					OntClass ontCls = (OntClass) subIt.next();
					String ontUri = ontCls.getURI();
					JClass subCls = jmodel.getJClass(ontUri);
					subCls.addSuperClass(cls);
					log.debug(NamingUtils.toLogName(ontClass) + ": Registering sub class "
							+ NamingUtils.getJavaFullName(subCls.getPackage(), subCls.getName()));
				}

				// get super classes and register them
				Iterator operandIt = ontClass.asUnionClass().listOperands();
				while (operandIt.hasNext()) {
					OntClass superClass = (OntClass) operandIt.next();

					JClass superCls;
					if (!jmodel.hasJClass(superClass.getURI()))
						jmodel.createJClass(superClass, basePackage);
					superCls = jmodel.getJClass(superClass.getURI());
					superCls.setOntClass(superClass);

					superCls.addSubClass(cls);
					log.debug(NamingUtils.toLogName(ontClass) + ": Registering super class "
							+ NamingUtils.getJavaFullName(superCls.getPackage(), superCls.getName()));
				}

			}

			if (ontClass.isComplementClass()) {
				jmodel.ontResourceErrors.add(new ResourceError(ontClass, "ComplementClass ignored"));
				log.warn("Found non restriction anonymous class: " + "ComplementClass ignored");
			}

			if (ontClass.isIntersectionClass()) {
				jmodel.ontResourceErrors.add(new ResourceError(ontClass, "Non restriction anonymous class registered "
						+ "as sub class of OwlThing"));
				log.warn("Found non restriction anonymous class: " + "Subclasses rergistered " + "as sub classes of OwlThing");
				JClass thingCls = jmodel.getJClass(jmodel.baseThingUri);
				Iterator<OntClass> it = ontClass.listSubClasses(false);
				while (it.hasNext()) {
					OntClass cls = (OntClass) it.next();
					JClass subIfc = jmodel.getJClass(cls.getURI());
					thingCls.addSubClass(subIfc);
				}

			}
			if (ontClass.isEnumeratedClass()) {
				// TODO: enumerated classes ignored
				jmodel.ontResourceErrors.add(new ResourceError(ontClass, "Enumerated class handled as simple class"));
				log.warn("Found non restriction anonymous class: " + "EnumeratedClass handled " + "as simple class");
			}

		}

	}

	@SuppressWarnings("unchecked")
	protected void handleClassishObjects() {
		// create our base object "Thing" and add as class
		createBaseClassishObjects();

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

	protected void createBaseClassishObjects() {
		// create our base object "Thing" and add as class
		String thingUri;
		String thingName = JModel.getBaseThingName();
		thingUri = ontModel.getNsPrefixURI("owl") + thingName;

		jmodel.baseThingUri = thingUri;
		jmodel.createJClass(thingName, thingUri, basePackage);
	}

	@SuppressWarnings("unchecked")
	protected void handleNamespaces() {
		// find the namespaces and add them to our namespace2prefix mapping
		Iterator it = this.ontModel.getNsPrefixMap().keySet().iterator();
		while (it.hasNext()) {
			String prefix = (String) it.next();
			String uri = this.ontModel.getNsPrefixURI(prefix);

			jmodel.ns2prefix.put(uri, prefix);
			log.info("Adding prefix " + prefix + " for namespace " + uri);
		}

		// handle name spaces without prefix
		Iterator importedUriIt = ontModel.listImportedOntologyURIs(true).iterator();
		while (importedUriIt.hasNext()) {
			String importedUri = (String) importedUriIt.next() + "#";
			if (!jmodel.ns2prefix.containsKey(importedUri)) {
				String ns = importedUri;
				log.info("Found namespace without prefix: " + ns);
				String prefix = jmodel.createNewPrefix();
				jmodel.ns2prefix.put(ns, prefix);
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

		log.info("");
		log.info("Found " + ontModel.listTransitiveProperties().toList().size() + " object properties");
		it = ontModel.listTransitiveProperties();
		handleProperties(it);

		log.info("");
		log.info("Found " + ontModel.listFunctionalProperties().toList().size() + " functional properties");
		it = ontModel.listFunctionalProperties();
		handleProperties(it);

		log.info("");
		log.info("Found " + ontModel.listInverseFunctionalProperties().toList().size() + " inverse functional properties");
		it = ontModel.listInverseFunctionalProperties();
		handleProperties(it);

		log.info("");
		log.info("Found " + ontModel.listSymmetricProperties().toList().size() + " symetrical properties");
		it = ontModel.listSymmetricProperties();
		handleProperties(it);

		log.info("");
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
				log.info("Found Restriction on Property" + NamingUtils.toLogName(cls.asRestriction().getOnProperty()));
				log.debug(DebugUtils.logRestriction(cls.asRestriction()));

				// find the restriction and the property it acts on
				Restriction ontRestriction = cls.asRestriction();
				OntProperty ontProperty = ontRestriction.getOnProperty();

				// find the classes it acts on
				Iterator subClassIt = ontRestriction.listSubClasses();
				while (subClassIt.hasNext()) {
					OntClass ontClass = (OntClass) subClassIt.next();

					// anonymous class -> ignore
					if (ontClass.getURI() == null) {
						log.debug(NamingUtils.toLogName(ontClass) + ": Anonymous class. Ignored");
						continue;
					}

					// owl:Thing and Co -> ignore
					if (NamespaceUtils.defaultNs2UriMapping.containsKey(ontClass.getNameSpace())) {
						log.debug(NamingUtils.toLogName(ontClass) + ": Is a base class. Ignored");
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
