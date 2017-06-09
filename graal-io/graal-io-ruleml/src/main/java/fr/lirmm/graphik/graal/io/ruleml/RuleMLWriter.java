/*
 * Copyright (C) Inria Sophia Antipolis - Méditerranée / LIRMM
 * (Université de Montpellier & CNRS) (2014 - 2017)
 *
 * Contributors :
 *
 * Clément SIPIETER <clement.sipieter@inria.fr>
 * Mélanie KÖNIG
 * Swan ROCHER
 * Jean-François BAGET
 * Michel LECLÈRE
 * Marie-Laure MUGNIER <mugnier@lirmm.fr>
 *
 *
 * This file is part of Graal <https://graphik-team.github.io/graal/>.
 *
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */
 /**
 * 
 */
package fr.lirmm.graphik.graal.io.ruleml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Literal;
import fr.lirmm.graphik.graal.api.core.NegativeConstraint;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.api.core.Variable;
import fr.lirmm.graphik.graal.api.io.AbstractGraalWriter;
import fr.lirmm.graphik.graal.core.factory.DefaultAtomFactory;
import fr.lirmm.graphik.util.Prefix;
import fr.lirmm.graphik.util.URI;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;

/**
 * @author Clément Sipieter (INRIA) <clement@6pi.fr>
 *
 */
public class RuleMLWriter extends AbstractGraalWriter {
	
	private static final Logger LOGGER = LoggerFactory
			.getLogger(RuleMLWriter.class);

	private final String indentStyle;
	private transient int currentIndentSize = 0;
	private boolean inFact = false;

	// /////////////////////////////////////////////////////////////////////////
	// CONSTRUCTOR
	// /////////////////////////////////////////////////////////////////////////

	public RuleMLWriter(Writer out) {
		super(out, DefaultAtomFactory.instance());
		indentStyle = "  ";
		init();
	}

	public RuleMLWriter() {
		this(new OutputStreamWriter(System.out));
	}
	
	public RuleMLWriter(OutputStream out) {
		this(new OutputStreamWriter(out));
	}
	
	public RuleMLWriter(File file) throws IOException {
		this(new FileWriter(file));
	}
	
	public RuleMLWriter(String path) throws IOException {
		 this(new FileWriter(path));
	}
	
	private void init() {
		try {
			this.writeln("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			this.writeln("<?xml-model href=\"http://deliberation.ruleml.org/1.01/relaxng/datalogplus_min_relaxed.rnc\"?>");
			this.openBalise("RuleML xmlns=\"http://ruleml.org/spec\""
					+ "\n        xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
					+ "\n        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
			this.writeComment("This file was generated by Graal");
		} catch (IOException e) {
			LOGGER.error("Error on init RuleML file", e);
		}
	}

	// /////////////////////////////////////////////////////////////////////////
	// METHODS
	// /////////////////////////////////////////////////////////////////////////
	
	@Override
	public RuleMLWriter writeComment(String str) throws IOException {
		this.write("\n\n<!-- ");
		this.write(str);
		this.writeln(" -->");

		return this;
	}

	@Override
	public RuleMLWriter write(Atom atom) throws IOException {
		this.openBalise("Assert");
		this.inFact = true;
		this.writeAtom(atom);
		this.inFact = false;
		this.closeBaliseWithReturnLine("Assert");

		return this;
	}

	@Override
	public RuleMLWriter write(AtomSet atomset) throws IOException {
		this.openBalise("Assert");
		this.inFact = true;
		this.writeAtomSet(atomset.iterator());
		this.inFact = false;
		this.closeBaliseWithReturnLine("Assert");

		return this;
	}
	
	@Override
	public RuleMLWriter write(Rule rule) throws IOException {
		Set<Variable> existVar = rule.getExistentials();
		Set<Variable> universalVar = rule.getVariables();
		universalVar.removeAll(existVar);

		CloseableIteratorWithoutException<Atom> it = rule.getHead().iterator();
		if (it.hasNext()) {
			it.next();
		}
		boolean isAtomicHead = !it.hasNext();

		this.openBalise("Assert");
		this.writeLabel(rule.getLabel());
		this.openBalise("Forall");
		for (Term t : universalVar) {
			this.writeTerm(t);
		}
		this.openBalise("Implies");
		this.openBalise("if");
		this.openBalise("And");
		this.writeAtomSet(rule.getBody().iterator());
		this.closeBaliseWithReturnLine("And");
		this.closeBaliseWithReturnLine("if");
		this.openBalise("then");
		if (!existVar.isEmpty()) {
			this.openBalise("Exists");
			for (Term t : existVar) {
				this.writeTerm(t);
			}
		}
		if (!isAtomicHead) {
			this.openBalise("And");
		}
		this.writeAtomSet(rule.getHead().iterator());
		if (!isAtomicHead) {
			this.closeBaliseWithReturnLine("And");
		}
		if (!existVar.isEmpty()) {
			this.closeBaliseWithReturnLine("Exists");
		}
		this.closeBaliseWithReturnLine("then");
		this.closeBaliseWithReturnLine("Implies");
		this.closeBaliseWithReturnLine("Forall");
		this.closeBaliseWithReturnLine("Assert");

		return this;
	}
	
	@Override
	public RuleMLWriter write(ConjunctiveQuery query) throws IOException {
		Set<Variable> existVar = query.getAtomSet().getVariables();
		existVar.removeAll(query.getAnswerVariables());

		this.openBalise("Query");
		if(!query.getLabel().isEmpty()) {
			this.writeLabel(query.getLabel());
		}
		this.openBalise("Exists");
		for (Term t : existVar) {
			this.writeTerm(t);
		}
		this.openBalise("And");
		this.writeAtomSet(query.getAtomSet().iterator());
		this.closeBaliseWithReturnLine("And");
		this.closeBaliseWithReturnLine("Exists");
		this.closeBaliseWithReturnLine("Query");

		return this;
	}
	
	@Override
	public RuleMLWriter write(Prefix prefix) throws IOException {
		LOGGER.warn("Prefix not supported: " + prefix.toString());

		return this;
	}

	@Override
	public RuleMLWriter write(NegativeConstraint constraint) throws IOException {
		LOGGER.warn("NegativeConstraint not yet implemented");

		return this;
	}

	// /////////////////////////////////////////////////////////////////////////
	// OVERRIDE METHODS
	// /////////////////////////////////////////////////////////////////////////

	@Override
	public void close() throws IOException {
		this.closeBaliseWithReturnLine("RuleML");
		this.write("\n");
		super.close();
	}
	
	// /////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	// /////////////////////////////////////////////////////////////////////////
	
	protected void writeLabel(String label) throws IOException {
		if(!label.isEmpty()) {
			this.writeIndent();
			this.write("<!-- ");
			this.write(label);
			this.write(" -->");
		}	
	}
	
	protected void writeAtomSet(CloseableIterator<Atom> it) throws IOException {
		while (it.hasNext()) {
			Atom a = it.next();
			this.writeAtom(a);
		}
		it.close();
	}
	
	@Override
	protected void writeStandardAtom(Atom atom) throws IOException {
		this.openBalise("Atom");
		this.writePredicate(atom.getPredicate());

		for (Term t : atom.getTerms()) {
			this.writeTerm(t);
		}
		this.closeBaliseWithReturnLine("Atom");
	}

	@Override
	protected void writeEquality(Term term, Term term2) throws IOException {
		this.openBalise("Equal");
		this.writeTerm(term);
		this.writeTerm(term2);
		this.closeBaliseWithReturnLine("Equal");
	}

	@Override
	protected void writeBottom() throws IOException {
		this.writeIndent();
		this.write("<Or/>");
	}

	protected void writeTerm(Term t) throws IOException {
		if(t.isVariable()) {
			if (this.inFact) {
				// Facts does not allow existential variables
				this.writeInd(t.getIdentifier());
			} else {
				this.openBalise("Var");
				this.write(t.getIdentifier().toString());
				this.closeBalise("Var");
			}
		} else if(t.isLiteral()) {
			Literal l = (Literal) t;
			this.writeIndent();
			this.write("<Data xsi:type=\"");
			if (l.getDatatype().getPrefix()
					.equals(Prefix.XSD.getPrefix())) {
				this.write("xsd:" + l.getDatatype().getLocalname());
			} else {
				LOGGER.warn("Unmanaged datatype: " + l.getDatatype());
				this.write(l.getDatatype().toString());
			}
			this.write("\">");
			this.write(l.getValue().toString());
			this.write("</Data>");
		} else { // CONSTANT
			this.writeInd(t.getIdentifier());
		}
	}
	
	protected void writeInd(Object identifier) throws IOException {
		if (identifier instanceof URI) {
			this.writeIndent();
			this.write("<Ind iri=\"");
			this.write(identifier.toString());
			this.write("\"/>");
		} else {
			this.openBalise("Ind");
			this.write(identifier.toString());
			this.closeBalise("Ind");
		}
	}

	protected void writePredicate(Predicate p) throws IOException {
		if (p.getIdentifier() instanceof URI) {
			this.writeIndent();
			this.write("<Rel iri=\"");
			this.write(p.getIdentifier().toString());
			this.write("\"/>");
		} else {
			this.openBalise("Rel");
			this.write(p.getIdentifier().toString());
			this.closeBalise("Rel");
		}
	}
	

	////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	// /////////////////////////////////////////////////////////////////////////

	private void incrIndent() {
		++this.currentIndentSize;
	}

	private void decrIndent() {
		--this.currentIndentSize;
	}

	private void writeIndent() throws IOException {
		this.write("\n");
		for (int i = 0; i < this.currentIndentSize; ++i) {
			this.write(indentStyle);
		}
	}

	private void openBalise(String baliseName) throws IOException {
		this.writeIndent();
		this.write('<');
		this.write(baliseName);
		this.write('>');
		this.incrIndent();
	}

	private void closeBalise(String baliseName) throws IOException {
		this.decrIndent();
		this.write("</");
		this.write(baliseName);
		this.write('>');
	}

	private void closeBaliseWithReturnLine(String baliseName)
			throws IOException {
		this.decrIndent();
		this.writeIndent();
		this.write("</");
		this.write(baliseName);
		this.write(">");
	}

	// /////////////////////////////////////////////////////////////////////////
	// STATIC METHODS
	////////////////////////////////////////////////////////////////////////////

	public static String writeToString(Object o) {
		StringWriter s = new StringWriter();
		RuleMLWriter w = new RuleMLWriter(s);
		try {
			w.write(o);
			w.close();
		} catch (IOException e) {
			
		}
		return s.toString();
	}
	
};

