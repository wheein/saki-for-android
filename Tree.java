package mahjong.riichi;

import java.util.ArrayList;

import mahjong.riichi.*;

public class Tree {
	private Node root;
		
	Tree(){
		root = new Node();
	}
	void addChild(Set x){
		Node addMe = new Node(x);
		addMe.myParent = root;
		root.addChild(addMe);
	}
		
	void addChild(Set x, Node parent){
		Node addMe = new Node(x);
		addMe.myParent = parent;
		parent.addChild(addMe);
	}
		
	Node getRoot(){
		return root;
	}
		
}
