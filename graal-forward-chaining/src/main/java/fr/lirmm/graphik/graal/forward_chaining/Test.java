package fr.lirmm.graphik.graal.forward_chaining;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.forward_chaining.ChaseException;
import fr.lirmm.graphik.graal.api.io.ParseException;
import fr.lirmm.graphik.graal.core.atomset.LinkedListAtomSet;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;

public class Test {

	public static void main(String[] args) throws ParseException {
		// TODO Auto-generated method stub
		
		// Rule
				Rule dependency1 = DlgpParser.parseRule("test(X, 11, 13, 11, 1) :- test(X, 12, 13, 12, 1).");
				Rule dependency2 = DlgpParser.parseRule("test(X, 11, 14, 1, 2) :- test(X, 11, 13, 1, 2).");
				
				// Atoms
				Atom a1 = DlgpParser.parseAtom("test(f1, 12, 13, 12, 1).");
				Atom a2 = DlgpParser.parseAtom("test(f1, 12, 13, 1, 2).");
				Atom a3 = DlgpParser.parseAtom("test(f1, 12, 13, 2, 13).");
				Atom a4 = DlgpParser.parseAtom("test(f2, 11, 13, 11, 1).");
				Atom a5 = DlgpParser.parseAtom("test(f2, 11, 13, 1, 2).");
				Atom a6 = DlgpParser.parseAtom("test(f2, 11, 13, 2, 13).");
				
				
				List<Rule> ruleSet = new ArrayList<Rule>();
				ruleSet.add(dependency1);
				ruleSet.add(dependency2);
				
				LinkedList<Atom> linkedListAtoms = new LinkedList<>();
				linkedListAtoms.add(a1);
				linkedListAtoms.add(a2);
				linkedListAtoms.add(a3);
				linkedListAtoms.add(a4);
				linkedListAtoms.add(a5);
				linkedListAtoms.add(a6);
				
				AtomSet atomSet = new LinkedListAtomSet(linkedListAtoms);
				
				BasicChase chase = new BasicChase(ruleSet.iterator(), atomSet);
				
				try {
					System.out.println("here");
					chase.next();
				} catch (ChaseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	}

}
