package de.incunabulum.jakuzi.generator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.traverse.BreadthFirstIterator;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;

import de.incunabulum.jakuzi.jmodel.JClass;
import de.incunabulum.jakuzi.jmodel.JInheritanceGraph;
import de.incunabulum.jakuzi.jmodel.JModel;
import de.incunabulum.jakuzi.jmodel.JProperty;
import de.incunabulum.jakuzi.jmodel.utils.LogUtils;
import de.incunabulum.jakuzi.model.RestrictionUtils;

public class ModelPreparation {

	private static Log log = LogFactory.getLog(ModelPreparation.class);

	private boolean reasignDomainlessProperties;
	private JModel jModel;

	JClass baseCls;

	public JModel prepareModel(JModel model) {
		this.jModel = model;
		baseCls = jModel.getJClass(jModel.getBaseThingUri());
		log.info("");
		log.info("Prepaing model for class writer");

		// assign properties without domain to restrictions
		if (reasignDomainlessProperties)
			reasignProperties();

		// aggregate properties, restriction containers...
		aggregateAllOnClass();

		// createPropertyRepresentations (top-down) missing
		createPropertyRepresentations();

		return this.jModel;
	}

	protected void createPropertyRepresentations() {
		JClass baseCls = jModel.getBaseThing();
		JInheritanceGraph<JClass, DefaultEdge> classGraph = jModel.getClassGraph();
		BreadthFirstIterator<JClass, DefaultEdge> it = new BreadthFirstIterator<JClass, DefaultEdge>(classGraph,
				baseCls);
		it.setCrossComponentTraversal(true);
		while (it.hasNext()) {
			JClass c = (JClass) it.next();
			c.createPropertyRepresentations();
		}
	}

	protected void aggregateAllOnClass() {
		JClass baseCls = jModel.getBaseThing();
		JInheritanceGraph<JClass, DefaultEdge> classGraph = jModel.getClassGraph();
		BreadthFirstIterator<JClass, DefaultEdge> it = new BreadthFirstIterator<JClass, DefaultEdge>(classGraph,
				baseCls);
		it.setCrossComponentTraversal(true);
		while (it.hasNext()) {
			JClass c = (JClass) it.next();
			c.aggegrateAll();
		}
	}

	protected boolean reasignProperty(JClass cls, JProperty property) {

		boolean success = false;

		// we check for each property, if we have a restriction with the given class
		// as subject
		OntClass ontClass = cls.getOntClass();
		OntProperty ontProperty = property.getOntProperty();

		if (RestrictionUtils.hasRestrictionOnProperty(ontClass, ontProperty)) {
			// baseCls as we are only testing base properties here
			log.info(LogUtils.toLogName(property) + ": Changing domain from " + LogUtils.toLogName(baseCls) + " to "
					+ LogUtils.toLogName(cls));
			property.addDomain(cls);
			// restriction container is ignored as this is empty in our case for baseClass
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

		// TODO use depth first traversal here? - how to move to next branch upon success?

		// loop over all domainless properties (p's with domain OwlThing)
		// and mark them for removal from domain OwlThing
		List<JProperty> properties = baseCls.listDirectDomainProperties();
		List<JProperty> propsToRemove = new ArrayList<JProperty>();

		for (JProperty property : properties) {
			boolean success = reasignProperty(baseCls, property);
			if (success)
				propsToRemove.add(property);
		}

		// remove the marked properties and the restriction containers
		for (JProperty prop : propsToRemove) {
			prop.removeDomain(baseCls);
			baseCls.removeRestrictionsContainer(prop);
		}

	}

	public void setReasignDomainlessProperties(boolean reasignDomainlessProperties) {
		this.reasignDomainlessProperties = reasignDomainlessProperties;
	}
}
