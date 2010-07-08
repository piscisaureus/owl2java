package nl.tudelft.tbm.eeni.owl2java.model.jmodel.utils;

import java.util.List;

import nl.tudelft.tbm.eeni.owl2java.model.jmodel.JClass;
import nl.tudelft.tbm.eeni.owl2java.model.jmodel.JInheritanceGraph;
import nl.tudelft.tbm.eeni.owl2java.model.jmodel.JModel;
import nl.tudelft.tbm.eeni.owl2java.model.jmodel.JPropertyRepresentation;
import nl.tudelft.tbm.eeni.owl2java.model.jmodel.JRestrictionsContainer;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.traverse.BreadthFirstIterator;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.Restriction;


public class DebugUtils {

	public static String logClass(OntClass cls) {
		String log = LogUtils.toLogName(cls) + " -> " + "anon: " + cls.isAnon() + ", complement: "
				+ cls.isComplementClass() + ", intersection: " + cls.isIntersectionClass() + ", union: "
				+ cls.isUnionClass() + ", enumerated: " + cls.isEnumeratedClass();
		return log;
	}

	public static String logProperty(OntProperty prop) {

		String log = LogUtils.toLogName(prop) + " -> " + "functional: " + prop.isFunctionalProperty() + ", inverse: "
				+ prop.isFunctionalProperty() + ", inverse functional: " + prop.isInverseFunctionalProperty()
				+ ", datatype prop: " + prop.isDatatypeProperty() + ", object prop: " + prop.isObjectProperty()
				+ ", symmetric: " + prop.isSymmetricProperty() + ", transitive: " + prop.isTransitiveProperty();
		return log;
	}

	public static String logRestriction(Restriction res) {
		String log = "Restriction -> " + "all values: " + res.isAllValuesFromRestriction() + ", some values: "
				+ res.isSomeValuesFromRestriction() + ", has value: " + res.isHasValueRestriction()
				+ ", max cardinality: " + res.isMaxCardinalityRestriction() + ", min cardinality: "
				+ res.isMinCardinalityRestriction() + ")";
		return log;
	}

	public static String logPropertyOnClass(OntClass cls, OntProperty prop) {
		return LogUtils.toLogName(cls) + "->" + LogUtils.toLogName(prop);
	}

	public static void debugRestrictions(JModel jModel) {
		JClass baseCls = jModel.getBaseThing();
		JInheritanceGraph<JClass, DefaultEdge> classGraph = jModel.getClassGraph();
		BreadthFirstIterator<JClass, DefaultEdge> it = new BreadthFirstIterator<JClass, DefaultEdge>(classGraph,
				baseCls);
		it.setCrossComponentTraversal(true);
		while (it.hasNext()) {
			JClass c = (JClass) it.next();
			List<JRestrictionsContainer> rcs = c.listDomainRestrictionContainers();
			for (JRestrictionsContainer rc : rcs)
				System.err.println(rc.getJModelReport());
		}
	}

	public static void debugDomainRepresentations(JModel jModel) {
		JClass baseCls = jModel.getBaseThing();
		JInheritanceGraph<JClass, DefaultEdge> classGraph = jModel.getClassGraph();
		BreadthFirstIterator<JClass, DefaultEdge> it = new BreadthFirstIterator<JClass, DefaultEdge>(classGraph,
				baseCls);
		it.setCrossComponentTraversal(true);
		while (it.hasNext()) {
			JClass c = (JClass) it.next();
			List<JPropertyRepresentation> pps = c.listDomainPropertyRepresentations();
			for (JPropertyRepresentation pp : pps)
				System.err.println(pp.getJModelReport());

		}
	}
}
