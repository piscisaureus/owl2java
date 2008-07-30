package de.incunabulum.jakuzi.jmodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;

public class JInheritanceGraph<V, E> extends DefaultDirectedGraph<V, E> implements DirectedGraph<V, E> {

	public JInheritanceGraph(Class<? extends E> edgeClass) {
		super(edgeClass);
	}

	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(JInheritanceGraph.class);
	private static final long serialVersionUID = 3988427746724146370L;

	public void addParentVertex(V current, V neww) {
		addVertex(neww);
		addEdge(neww, current);

	}

	public void insertVertex(V source, V target, V neww) {
		addVertex(neww);
		removeEdge(source, target);
		addEdge(source, neww);
		addEdge(neww, target);
	}

	public void addChildVertex(V current, V neww) {
		addVertex(neww);
		addEdge(current, neww);

	}

	public boolean hasDirectParent(V vertex, V parent) {
		Iterator<E> edgeIt = incomingEdgesOf(vertex).iterator();
		while (edgeIt.hasNext()) {
			E edge = edgeIt.next();
			V p = getEdgeSource(edge);

			if (p.equals(parent))
				return true;
		}
		return false;
	}

	public boolean hasDirectChild(V vertex, V child) {
		Iterator<E> edgeIt = outgoingEdgesOf(vertex).iterator();
		while (edgeIt.hasNext()) {
			E edge = edgeIt.next();
			V c = getEdgeTarget(edge);

			if (c.equals(child))
				return true;
		}
		return false;
	}

	public List<V> listDirectParents(V vertex) {
		List<V> parents = new ArrayList<V>();
		Iterator<E> edgeIt = incomingEdgesOf(vertex).iterator();
		while (edgeIt.hasNext()) {
			E edge = (E) edgeIt.next();
			V parent = getEdgeSource(edge);
			parents.add(parent);
		}
		return parents;
	}

	public List<V> listDirectChildren(V vertex) {
		List<V> children = new ArrayList<V>();
		Iterator<E> edgeIt = outgoingEdgesOf(vertex).iterator();
		while (edgeIt.hasNext()) {
			E edge = (E) edgeIt.next();
			V parent = getEdgeTarget(edge);
			children.add(parent);
		}
		return children;
	}

	public boolean hasParent(V vertex, V parent, boolean recursive) {
		// simple case
		if (recursive == false)
			return hasDirectParent(vertex, parent);

		// recursive, yet direct parent
		if (hasDirectParent(vertex, parent))
			return true;

		// no direct parent -> call recursively
		for (V v : listDirectParents(vertex)) {
			boolean hasSC = hasParent(v, parent, recursive);
			if (hasSC)
				return true;
		}
		return false;
	}

	public boolean hasChild(V vertex, V child, boolean recursive) {
		// simple case
		if (recursive == false)
			return hasDirectChild(vertex, child);

		// recursive, yet direct parent
		if (hasDirectChild(vertex, child))
			return true;

		// no direct parent -> call recursively
		List<V> children = listDirectChildren(vertex);
		for (V v : children) {
			boolean hasSC = hasChild(v, child, recursive);
			if (hasSC)
				return true;
		}
		return false;

	}

	public boolean hasAnyParents(V vertex) {
		int parentCount = incomingEdgesOf(vertex).size();
		if (parentCount > 0)
			return true;
		return false;
	}

	public boolean hasAnyChildren(V vertex) {
		int childCount = outgoingEdgesOf(vertex).size();
		if (childCount > 0)
			return true;
		return false;

	}
}
