package de.incunabulum.jakuzi.utils;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.KShortestPaths;

public class GraphPathUtils<V, E> {
	
	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(GraphPathUtils.class);
	private DirectedGraph<V,E> graph;
	
	public GraphPathUtils(DirectedGraph<V,E> graph) {
		this.graph = graph;
	}
	
	
	public GraphPath<V,E> findShortestPath(V from, V to) {
		KShortestPaths<V, E> kShortestPath = new KShortestPaths<V, E>(graph, from, 1);
		
		List<GraphPath<V,E>> paths = kShortestPath.getPaths(to);
		if (paths == null)
			return null;
		if (paths.isEmpty())
			return null;
		return paths.get(0);
	}
	
	public List<E> findShortestPathDijkstra(V from, V to) {
		return DijkstraShortestPath.findPathBetween(graph, from, to);
	}
	
	public V getNextVertex(GraphPath<V, E> graphPath, V vertex) {
		List<E> edges = graphPath.getEdgeList();
		for (E edge : edges) {
			V source = graph.getEdgeSource(edge);
			V target = graph.getEdgeTarget(edge);
			
			if (source.equals(vertex))
				return target;
		}
		return null;
	}
	
	public boolean hasVertex(GraphPath<V, E> graphPath, V vertex) {
		List<E> edges = graphPath.getEdgeList();
		for (E edge : edges) {
			V source = graph.getEdgeSource(edge);
			V target = graph.getEdgeTarget(edge);
			
			if (source.equals(vertex))
				return true;
			if (target.equals(vertex))
				return true;
		}
		return false;
	}
	
	
}
