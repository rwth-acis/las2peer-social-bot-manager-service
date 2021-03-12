package i5.las2peer.services.socialBotManagerService.parser;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.DialogueGoal;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.MultiValueNode;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.Node;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.RepetitionNode;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.RootNode;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.SelectionNode;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.SequenceNode;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.ValueNode;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.WrapperNode;
import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.Slot;

public class Visualizer {

	public void draw(Frame frame) {

		try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("frame.dot")))) {
			out.write("digraph {");
			out.newLine();
			for (Slot slot: frame.getSlots().values()) {
				out.write(frame.getName() + " -> " + clean(slot.getAPIName()));
				out.newLine();
				draw(slot, out);
			}
			out.write("}");
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void draw(Slot slot, BufferedWriter out) throws IOException {

		for (Slot child : slot.getChildren()) {

			out.write(clean(slot.getAPIName()) + " -> " + clean(child.getAPIName()));
			out.newLine();
			draw(child, out);
		}

	}

	public void draw(DialogueGoal goal) {

		try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("goal.dot")))) {
			out.write("digraph {");
			out.newLine();
			draw(goal.getRoot(), out);
			out.write("}");
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	}
	
	public void draw(Node node, BufferedWriter out) throws IOException {
		
		if(node instanceof RootNode) {
			RootNode ver = (RootNode) node;		
			out.newLine();
			
			for (Node child : ver.getChildren()) {				
				out.write("Root" + " -> " + clean(child.getDisplayName()));
				out.newLine();
				draw(child, out);
			}			
		}
		
		else if(node instanceof ValueNode) {
			ValueNode ver = (ValueNode) node;
			out.write(clean(ver.getDisplayName()) + " [shape=box,label="+ ver.getAPIName() + "]");
			out.newLine();
		}
		
		else if(node instanceof MultiValueNode) {
			MultiValueNode ver = (MultiValueNode) node;
			out.write(clean(ver.getDisplayName()) + " [shape=box,label="+ clean(ver.getAPIName()) + "]");
			out.newLine();
		}
		
		else if(node instanceof SelectionNode) {
			SelectionNode ver = (SelectionNode) node;
			out.write(clean(ver.getDisplayName()) + " [shape=diamond,label="+ clean(ver.getAPIName()) + "]");
			out.newLine();
			
			for (Node child : ver.getChildren().values()) {				
				out.write(clean(ver.getDisplayName()) + " -> " + clean(child.getDisplayName()));
				out.newLine();
				draw(child, out);
			}			
		}
		
		else if(node instanceof RepetitionNode) {
			RepetitionNode ver = (RepetitionNode) node;
			out.write(clean(ver.getDisplayName()) + " [shape=hexagon,label="+ clean(ver.getAPIName()) + "]");
			out.newLine();
			
			for (Node child : ver.getValueChildren()) {				
				out.write(clean(ver.getDisplayName()) + " -> " + clean(child.getDisplayName()));
				out.newLine();
				draw(child, out);
			}			
		}
		
		else if(node instanceof WrapperNode) {
			WrapperNode ver = (WrapperNode) node;	
			out.write(clean(ver.getDisplayName()) + " [shape=ellipse,label="+ clean(ver.getAPIName()) + "]");
			out.newLine();
			
			for (Node child : ver.getChildren()) {				
				out.write(clean(ver.getDisplayName()) + " -> " + clean(child.getDisplayName()));
				out.newLine();
				draw(child, out);
			}			
		}
		
		else if(node instanceof SequenceNode) {
			SequenceNode ver = (SequenceNode) node;	
			out.write(clean(ver.getDisplayName()) + " [shape=ellipse,label="+ clean(ver.getAPIName()) + "]");
			out.newLine();
			
			for (Node child : ver.getChildren()) {				
				out.write(clean(ver.getDisplayName()) + " -> " + clean(child.getDisplayName()));
				out.newLine();
				draw(child, out);
			}			
		}
				
	}
	
	public String clean(String input) {
		if(input == null)
			return null;
		return input.replace(" ", "");
	}

}
