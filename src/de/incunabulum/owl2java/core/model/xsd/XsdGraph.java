package de.incunabulum.owl2java.core.model.xsd;

import java.util.ArrayList;
import java.util.List;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;

import de.incunabulum.owl2java.core.utils.GraphPathUtils;
public class XsdGraph {

	private static final DirectedGraph<String, DefaultEdge> xsdGraph = new DefaultDirectedGraph<String, DefaultEdge>(
			DefaultEdge.class);

	static {
		// add vertexes
		xsdGraph.addVertex(XsdSchema.xsdENTITY);
		xsdGraph.addVertex(XsdSchema.xsdID);
		xsdGraph.addVertex(XsdSchema.xsdIDREF);
		xsdGraph.addVertex(XsdSchema.xsdNCName);
		xsdGraph.addVertex(XsdSchema.xsdNMTOKEN);
		xsdGraph.addVertex(XsdSchema.xsdNOTATION);
		xsdGraph.addVertex(XsdSchema.xsdName);
		xsdGraph.addVertex(XsdSchema.xsdQName);
		xsdGraph.addVertex(XsdSchema.xsdanyURI);
		xsdGraph.addVertex(XsdSchema.xsdbase64Binary);
		xsdGraph.addVertex(XsdSchema.xsdboolean);
		xsdGraph.addVertex(XsdSchema.xsdbyte);
		xsdGraph.addVertex(XsdSchema.xsddate);
		xsdGraph.addVertex(XsdSchema.xsddateTime);
		xsdGraph.addVertex(XsdSchema.xsddecimal);
		xsdGraph.addVertex(XsdSchema.xsddouble);
		xsdGraph.addVertex(XsdSchema.xsdduration);
		xsdGraph.addVertex(XsdSchema.xsdfloat);
		xsdGraph.addVertex(XsdSchema.xsdgDay);
		xsdGraph.addVertex(XsdSchema.xsdgMonth);
		xsdGraph.addVertex(XsdSchema.xsdgMonthDay);
		xsdGraph.addVertex(XsdSchema.xsdgYear);
		xsdGraph.addVertex(XsdSchema.xsdgYearMonth);
		xsdGraph.addVertex(XsdSchema.xsdhexBinary);
		xsdGraph.addVertex(XsdSchema.xsdint);
		xsdGraph.addVertex(XsdSchema.xsdinteger);
		xsdGraph.addVertex(XsdSchema.xsdlanguage);
		xsdGraph.addVertex(XsdSchema.xsdlong);
		xsdGraph.addVertex(XsdSchema.xsdnegativeInteger);
		xsdGraph.addVertex(XsdSchema.xsdnonNegativeInteger);
		xsdGraph.addVertex(XsdSchema.xsdnonPositiveInteger);
		xsdGraph.addVertex(XsdSchema.xsdnormalizedString);
		xsdGraph.addVertex(XsdSchema.xsdpositiveInteger);
		xsdGraph.addVertex(XsdSchema.xsdshort);
		xsdGraph.addVertex(XsdSchema.xsdstring);
		xsdGraph.addVertex(XsdSchema.xsdtime);
		xsdGraph.addVertex(XsdSchema.xsdtoken);
		xsdGraph.addVertex(XsdSchema.xsdunsignedByte);
		xsdGraph.addVertex(XsdSchema.xsdunsignedInt);
		xsdGraph.addVertex(XsdSchema.xsdunsignedLong);
		xsdGraph.addVertex(XsdSchema.xsdunsignedShort);
		xsdGraph.addVertex(XsdSchema.xsdLiteral);

		// add edges -> first level
		xsdGraph.addEdge(XsdSchema.xsdLiteral, XsdSchema.xsdduration);
		xsdGraph.addEdge(XsdSchema.xsdLiteral, XsdSchema.xsddateTime);
		xsdGraph.addEdge(XsdSchema.xsdLiteral, XsdSchema.xsdtime);
		xsdGraph.addEdge(XsdSchema.xsdLiteral, XsdSchema.xsddate);
		xsdGraph.addEdge(XsdSchema.xsdLiteral, XsdSchema.xsdgYearMonth);
		xsdGraph.addEdge(XsdSchema.xsdLiteral, XsdSchema.xsdgYear);
		xsdGraph.addEdge(XsdSchema.xsdLiteral, XsdSchema.xsdgMonthDay);
		xsdGraph.addEdge(XsdSchema.xsdLiteral, XsdSchema.xsdgDay);
		xsdGraph.addEdge(XsdSchema.xsdLiteral, XsdSchema.xsdgMonth);
		xsdGraph.addEdge(XsdSchema.xsdLiteral, XsdSchema.xsdboolean);
		xsdGraph.addEdge(XsdSchema.xsdLiteral, XsdSchema.xsdbase64Binary);
		xsdGraph.addEdge(XsdSchema.xsdLiteral, XsdSchema.xsdhexBinary);
		xsdGraph.addEdge(XsdSchema.xsdLiteral, XsdSchema.xsdfloat);
		xsdGraph.addEdge(XsdSchema.xsdLiteral, XsdSchema.xsddouble);
		xsdGraph.addEdge(XsdSchema.xsdLiteral, XsdSchema.xsdanyURI);
		xsdGraph.addEdge(XsdSchema.xsdLiteral, XsdSchema.xsdQName);
		xsdGraph.addEdge(XsdSchema.xsdLiteral, XsdSchema.xsdNOTATION);
		xsdGraph.addEdge(XsdSchema.xsdLiteral, XsdSchema.xsdstring);
		xsdGraph.addEdge(XsdSchema.xsdLiteral, XsdSchema.xsddecimal);

		// sub elements of string
		xsdGraph.addEdge(XsdSchema.xsdstring, XsdSchema.xsdnormalizedString);
		xsdGraph.addEdge(XsdSchema.xsdnormalizedString, XsdSchema.xsdtoken);
		xsdGraph.addEdge(XsdSchema.xsdtoken, XsdSchema.xsdlanguage);
		xsdGraph.addEdge(XsdSchema.xsdtoken, XsdSchema.xsdName);
		xsdGraph.addEdge(XsdSchema.xsdtoken, XsdSchema.xsdNMTOKEN);
		xsdGraph.addEdge(XsdSchema.xsdName, XsdSchema.xsdNCName);
		xsdGraph.addEdge(XsdSchema.xsdNCName, XsdSchema.xsdID);
		xsdGraph.addEdge(XsdSchema.xsdNCName, XsdSchema.xsdIDREF);
		xsdGraph.addEdge(XsdSchema.xsdNCName, XsdSchema.xsdENTITY);

		// sub elements of decimal
		xsdGraph.addEdge(XsdSchema.xsddecimal, XsdSchema.xsdinteger);
		xsdGraph.addEdge(XsdSchema.xsdinteger, XsdSchema.xsdnonPositiveInteger);
		xsdGraph.addEdge(XsdSchema.xsdinteger, XsdSchema.xsdlong);
		xsdGraph.addEdge(XsdSchema.xsdinteger, XsdSchema.xsdnonNegativeInteger);
		xsdGraph.addEdge(XsdSchema.xsdnonPositiveInteger, XsdSchema.xsdnegativeInteger);
		xsdGraph.addEdge(XsdSchema.xsdlong, XsdSchema.xsdint);
		xsdGraph.addEdge(XsdSchema.xsdnonNegativeInteger, XsdSchema.xsdunsignedLong);
		xsdGraph.addEdge(XsdSchema.xsdnonNegativeInteger, XsdSchema.xsdpositiveInteger);
		xsdGraph.addEdge(XsdSchema.xsdint, XsdSchema.xsdshort);
		xsdGraph.addEdge(XsdSchema.xsdshort, XsdSchema.xsdbyte);
		xsdGraph.addEdge(XsdSchema.xsdunsignedLong, XsdSchema.xsdunsignedInt);
		xsdGraph.addEdge(XsdSchema.xsdunsignedInt, XsdSchema.xsdunsignedShort);
		xsdGraph.addEdge(XsdSchema.xsdunsignedShort, XsdSchema.xsdunsignedByte);
	}

	public static List<DefaultEdge> getShortestPath(String from, String to) {
		List<DefaultEdge> path = DijkstraShortestPath.findPathBetween(xsdGraph, from, to);
		return path;
	}

	public static String findBestSuperXsdType(List<String> xsdTypeUris) {
		List<GraphPath<String, DefaultEdge>> paths = new ArrayList<GraphPath<String, DefaultEdge>>();

		// for all given XSD types find a path from xsdRootNode to the XSD type
		for (String xsdTypeUri : xsdTypeUris) {
			// abort if one type == xsdRootNode (primitive case)
			if (xsdTypeUri.equals(XsdSchema.xsdLiteral))
				return XsdSchema.xsdLiteral;
			// otherwise, find the shortest path and add it to the paths list
			GraphPathUtils<String, DefaultEdge> graphUtils = new GraphPathUtils<String, DefaultEdge>(xsdGraph);
			GraphPath<String, DefaultEdge> graphPath = graphUtils.findShortestPath(XsdSchema.xsdLiteral, xsdTypeUri);
			paths.add(graphPath);
		}

		// find the last common element in the paths
		int noOfPaths = paths.size();
		GraphPathUtils<String, DefaultEdge> graphUtils = new GraphPathUtils<String, DefaultEdge>(xsdGraph);

		String type = XsdSchema.xsdLiteral;
		String subType;

		while (true) {
			// for first graph, find sub type
			subType = graphUtils.getNextVertex(paths.get(0), type);
			// no further sub types -> end of path -> return type
			if (subType == null)
				return type;
			// for all other graphs test if this node exists
			for (int i = 1; i < noOfPaths; i++) {
				if (!graphUtils.hasVertex(paths.get(i), subType))
					return type;
			}
			// go down another level
			type = subType;
		}
	}

}
