package de.incunabulum.owl2java.generator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.traverse.BreadthFirstIterator;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;

import de.incunabulum.owl2java.core.model.jenautils.RestrictionUtils;
import de.incunabulum.owl2java.core.model.jmodel.JClass;
import de.incunabulum.owl2java.core.model.jmodel.JInheritanceGraph;
import de.incunabulum.owl2java.core.model.jmodel.JModel;
import de.incunabulum.owl2java.core.model.jmodel.JProperty;
import de.incunabulum.owl2java.core.model.jmodel.JRestrictionsContainer;
import de.incunabulum.owl2java.core.model.jmodel.utils.LogUtils;

public class ModelPreparation {

	private static Log log = LogFactory.getLog(ModelPreparation.class);

	private boolean reasignDomainlessProperties;
	private JModel jModel;

	JClass baseCls;

	public JModel prepareModel(JModel model) {
		jModel = model;
		baseCls = jModel.getJClass(jModel.getBaseThingUri());
		log.info("");
		log.info("Prepaing model for class writer");

		// assign properties without domain to restrictions
		if (reasignDomainlessProperties)
			reasignProperties();
		
		// createPropertyRepresentations 
		createDomainPropertyRepresentations();
		
		// aggregate properties, restriction containers...
		
		int aggregateActions = 1;
		while (aggregateActions != 0) {
			aggregateActions = aggregateAllOnClass();
		}


		return jModel;
	}

	protected void createDomainPropertyRepresentations() {
		log.info("Creating domain property representations");
		JClass baseCls = jModel.getBaseThing();
		JInheritanceGraph<JClass, DefaultEdge> classGraph = jModel.getClassGraph();
		BreadthFirstIterator<JClass, DefaultEdge> it = new BreadthFirstIterator<JClass, DefaultEdge>(classGraph,
				baseCls);
		it.setCrossComponentTraversal(true);
		while (it.hasNext()) {
			JClass c = (JClass) it.next();
			c.createDomainPropertyRepresentations();
		}
	}

	protected int aggregateAllOnClass() {
		int aggregateActions = 0;
		JClass baseCls = jModel.getBaseThing();
		JInheritanceGraph<JClass, DefaultEdge> classGraph = jModel.getClassGraph();
		BreadthFirstIterator<JClass, DefaultEdge> it = new BreadthFirstIterator<JClass, DefaultEdge>(classGraph,
				baseCls);
		it.setCrossComponentTraversal(true);
		while (it.hasNext()) {
			JClass c = (JClass) it.next();
			int count = c.aggegrateAll();
			aggregateActions = aggregateActions+count;
		}
		log.info("Aggregating all from parent classes: " + aggregateActions + " Actions");
		return aggregateActions;
	}

	protected boolean reasignProperty(JClass cls, JProperty property) {

		boolean success = false;

		// we check for each property, if we have a restriction with the given class
		// as subject
		OntClass ontClass = cls.getOntClass();
		OntProperty ontProperty = property.getOntProperty();

		if (RestrictionUtils.hasRestrictionOnProperty(ontClass, ontProperty)) {
			// baseCls as we are only testing base properties here
			log.debug(LogUtils.toLogName(property) + ": Changing domain from " + LogUtils.toLogName(baseCls) + " to "
					+ LogUtils.toLogName(cls));
			property.addDomain(cls);
			
			// restriction container is is empty for baseClass > we ignore it (remove would be better)
			
			// We no longer have a global property with a local restriction!
			// -> update "enabled" cardinality status based on cardinality 
			// -> for local restriction "aProperty max 1" disable multiple accessors
			JRestrictionsContainer rc = cls.getDomainRestrictionsContainer(property);
			if (rc != null) {
				if (rc.hasCardinalityRestriction()) {
					if (rc.getCardinalityRestriction().getMaxCardinality() == 1) {
						rc.getCardinalityRestriction().setMultipleEnabled(false);
						rc.getCardinalityRestriction().setSingleEnabled(true);
						
						// with maxCardinality = 1 this is identical with a functional
						// property > marking it.
						property.setFunctional(true);
					}
					if (rc.getCardinalityRestriction().getMaxCardinality() == 0) {
						rc.getCardinalityRestriction().setMultipleEnabled(false);
						rc.getCardinalityRestriction().setSingleEnabled(false);
					}
				}
			}
			success = true;
		} else {
			// test all subclasses, calls this method recursively
			for (JClass subCls : cls.listDirectSubClasses()) {
				boolean suc = reasignProperty(subCls, property);
				// if successful for this subclass, set success to true
				if (suc)
					success = true;
			}
		}
		return success;
	}

	protected void reasignProperties() {
		log.info("Reasigning unbound properties to corresponding classes with restrictions");

		// XXX use depth first traversal here? - how to move to next branch upon success?

		// loop over all domainless properties (p's with domain OwlThing)
		// and mark them for removal from domain OwlThing
		List<JProperty> properties = baseCls.listDomainProperties();
		List<JProperty> propsToRemove = new ArrayList<JProperty>();

		for (JProperty property : properties) {
			boolean success = reasignProperty(baseCls, property);
			if (success)
				propsToRemove.add(property);
		}

		// remove the marked properties and the restriction containers
		for (JProperty prop : propsToRemove) {
			prop.removeDomain(baseCls);
			prop.removeClassRestrictions(baseCls);
			baseCls.removeDomainRestrictionsContainer(prop);
			
		}

	}

	public void setReasignDomainlessProperties(boolean reasignDomainlessProperties) {
		this.reasignDomainlessProperties = reasignDomainlessProperties;
	}
}
