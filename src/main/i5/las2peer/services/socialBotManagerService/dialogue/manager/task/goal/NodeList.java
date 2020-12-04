package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import i5.las2peer.services.socialBotManagerService.model.Slot;

public class NodeList extends ArrayList<Node> {

	private static final long serialVersionUID = 1L;

	public NodeList() {

	}

	public NodeList(Node node) {
		this.add(node);
	}

	/**
	 * Get nodes that are fillable
	 * 
	 * @return list of nodes that can be filled with a value
	 */
	List<Fillable> getFillableNodes() {

		List<Fillable> res = new ArrayList<Fillable>();
		for (Node node : this) {
			if (node instanceof Fillable)
				res.add((Fillable) node);
		}

		return res;
	}

	/**
	 * Get nodes with filled values
	 * 
	 * @return list of nodes that have a filled value (non-empty nodes)
	 */
	public List<Fillable> getFilledValues() {

		List<Fillable> res = new ArrayList<Fillable>();
		for (Node node : this) {
			if (node instanceof Fillable)
				if (((Fillable) node).isFilled())
					res.add((Fillable) node);
		}

		return res;
	}

	/**
	 * Returns if list contains a node
	 * 
	 * @param name of the slot of the node
	 * @return list contains a node (true) or not (false)
	 */
	public boolean contains(String name) {

		for (Fillable node : getFillableNodes()) {
			Slot slot = node.getSlot();
			if (name.contentEquals(slot.getName()) || slot.getIntents().contains(name))
				return true;
		}

		return false;
	}

	public Slotable get(String name) {
		for (Node nos : this) {
			if (nos instanceof Slotable) {
				Slotable node = (Slotable) nos;
				Slot slot = node.getSlot();
				if (name.contentEquals(slot.getName()) || slot.getIntents().contains(name))
					return node;

			}

		}
		return null;
	}

	public Collection<Fillable> getByEntity(String entityName) {
		
		Collection<Fillable> res = new ArrayList<>();		
		for (Node node : this) {
			if (node instanceof Fillable) {
				Fillable fillable = (Fillable) node;
				if(fillable.hasEntity(entityName))
					res.add(fillable);
			}
		}
		
		return res;		
	}
	
	/**
	 * Get list of intents
	 * 
	 * @return list of intents of nodes
	 */
	public Collection<String> getIntents() {

		Collection<String> intents = new ArrayList<String>();
		for (Fillable node : getFillableNodes()) {
			Slot slot = node.getSlot();
			intents.addAll(slot.getIntents());
		}

		return intents;
	}

	public NodeList Fillables() {

		NodeList res = new NodeList();
		for (Node node : this) {
			if (node instanceof Fillable)
				res.add(node);
		}
		return res;

	}

	public List<Slotable> Slotable() {

		List<Slotable> res = new ArrayList<>();
		for (Node node : this) {
			if (node instanceof Slotable)
				res.add((Slotable) node);
		}
		return res;

	}

	public void print() {
		for (Node node : this) {
			if (node instanceof Fillable)
				System.out.println(node.getClass() + ": " + ((Fillable) node).getSlot().getName());
			else if (node instanceof RepetitionNode)
				System.out.println(node.getClass() + ": " + ((RepetitionNode) node).getSlot().getName());
			else
				System.out.println(node.getClass());
		}
	}

}
