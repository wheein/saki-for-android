package mahjong.riichi;
import java.util.ArrayList;

import mahjong.riichi.*;

public class Node {
	public Set mySet;
	private ArrayList<Node> children;
	public Node myParent;
	private int childIter;
	
	Node(){
		mySet = new Set();
		children = new ArrayList<Node>();
		//The others stay null for now
	}
	
	Node(Set set){
		mySet = set;
		children = new ArrayList<Node>();
	}
	
	void setParent(Node parent){
		myParent = parent;
	}
	
	void addChild(Node child){
		child.myParent = this;
		children.add(child);
	}
	
	void addChild(Set set){
		Node addMe = new Node(set);
		addMe.myParent = this;
		children.add(addMe);
	}
	
	Node getFirst(){
		if(children.size() == 0)
			return null;
		childIter = 0;
		return children.get(0);
	}
	
	Node getNext(){
		childIter++;
		if(childIter >= children.size())
			return null;
		return children.get(childIter);
	}
	
	//We need an alternate way to iterate through the list
	Node getChild(int idx){
		if(idx < children.size())
			return children.get(idx);
		return null;
	}
	
	int getNumChildren(){
		return children.size();
	}
}
