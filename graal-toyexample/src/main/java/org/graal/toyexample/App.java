package org.graal.toyexample;

import java.sql.SQLException;
import java.util.LinkedList;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.forward_chaining.Chase;
import fr.lirmm.graphik.graal.api.forward_chaining.ChaseException;
import fr.lirmm.graphik.graal.forward_chaining.BasicChase;
import fr.lirmm.graphik.graal.forward_chaining.BreadthFirstChase;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.store.rdbms.RdbmsStore;
import fr.lirmm.graphik.graal.store.rdbms.driver.PostgreSQLDriver;
import fr.lirmm.graphik.graal.store.rdbms.natural.NaturalRDBMSStore;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.IteratorException;

/**
 * Hello world!
 *
 */
public class App 
{
	
	public static void restrictedChaseTest2() throws AtomSetException, SQLException, ChaseException, IteratorException {
		// example on the paper "benchmarking the chase"
		NaturalRDBMSStore atomSet = new NaturalRDBMSStore(new PostgreSQLDriver("localhost", "graal", "postgres", "postgres"));
		
		atomSet.addAll(DlgpParser.parseAtomSet("<R>(a,b). <R>(b, b)."));

		LinkedList<Rule> ruleSet = new LinkedList<>();
		
		ruleSet.add(DlgpParser.parseRule("<R>(X1,Y), <A>(Y), <A>(X2) :- <R>(X1,X2)."));


//		Chase chase = new BasicChase<RdbmsStore>(ruleSet, atomSet);
		Chase chase = new BreadthFirstChase(ruleSet, atomSet);
		chase.execute();
		
		System.out.println("Chase done\n");
		int size = 0;
		
		CloseableIterator<Atom> it = atomSet.iterator();
		
		while(it.hasNext()) {
			System.out.println("results: "+it.next().toString());
			size++;
		}
	}
	
	public static void toy() throws AtomSetException, SQLException, ChaseException, IteratorException {
		NaturalRDBMSStore atomSet = new NaturalRDBMSStore(new PostgreSQLDriver("localhost", "graal", "postgres", "postgres"));
		
//		atomSet.addAll(DlgpParser.parseAtomSet("f(x_f, 12, 13, 12, 1). f(x_f, 12, 13, 1, 2). f(x_f, 12, 13, 2, 13)."));
		atomSet.add(DlgpParser.parseAtom("<F>(x_f, 12, 13, 12, 1)."));
		atomSet.add(DlgpParser.parseAtom("<F>(x_f, 12, 13, 1, 2)."));
		atomSet.add(DlgpParser.parseAtom("<F>(x_f, 12, 13, 2, 13)."));

		LinkedList<Rule> ruleSet = new LinkedList<>();
		
		ruleSet.add(DlgpParser.parseRule("<F>(X_f, 11, 13, 1, 2) :- <F>(X_f, 12, 13, 1, 2)."));
		ruleSet.add(DlgpParser.parseRule("<F>(X_f, 11, 14, 2, 14) :- <F>(X_f, 11, 13, 2, 13)."));
		ruleSet.add(DlgpParser.parseRule("<F>(X_f, X_s1, X_d, X_x1, X_x2) :- <F>(X_f, X_s1, X_d, X_n, X_x1), <F>(X_f, X_s2, X_d, X_x1, X_x2)."));
//		ruleSet.add(DlgpParser.parseRule("X_f1=X_f2 :- <F>(X_f1, 12, 13, 1, 2), <F>(X_f2, 11, 13, 1, 2)."));
//		ruleSet.add(DlgpParser.parseRule("X_f1=X_f2 :- <F>(X_f1, 11, 13, 2, 13), <F>(X_f2, 11, 14, 2, 14)."));

//		Chase chase = new BasicChase<RdbmsStore>(ruleSet, atomSet);
		Chase chase = new BreadthFirstChase(ruleSet, atomSet);
		chase.execute();
		
		System.out.println("Chase done\n");
		int size = 0;
		
		CloseableIterator<Atom> it = atomSet.iterator();
		
		while(it.hasNext()) {
			System.out.println("results: "+it.next().toString());
			size++;
		}
	}
	
    public static void main( String[] args ) throws IteratorException, AtomSetException, SQLException, ChaseException
    {
        System.out.println( "Hello World!" );
        
//        restrictedChaseTest2();
        toy();
    }
}
