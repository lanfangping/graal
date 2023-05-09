/*
 * Copyright (C) Inria Sophia Antipolis - Méditerranée / LIRMM
 * (Université de Montpellier & CNRS) (2014 - 2018)
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
package fr.lirmm.graphik.graal.store.rdbms.homomorphism;

import java.sql.ResultSet;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Query;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.api.core.Variable;
import fr.lirmm.graphik.graal.api.homomorphism.Homomorphism;
import fr.lirmm.graphik.graal.api.homomorphism.HomomorphismException;
import fr.lirmm.graphik.graal.core.DefaultVariableGenerator;
import fr.lirmm.graphik.graal.core.FreshVarSubstitution;
import fr.lirmm.graphik.graal.core.Substitutions;
import fr.lirmm.graphik.graal.homomorphism.AbstractHomomorphism;
import fr.lirmm.graphik.graal.store.rdbms.RdbmsStore;
import fr.lirmm.graphik.graal.store.rdbms.util.ResultSetCloseableIterator;
import fr.lirmm.graphik.graal.store.rdbms.util.SQLQuery;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.Iterators;
import fr.lirmm.graphik.util.stream.converter.ConverterCloseableIterator;

/**
 * @author Clément Sipieter (INRIA) <clement@6pi.fr>
 * 
 */
public final class SqlHomomorphism extends AbstractHomomorphism<Query, RdbmsStore>
                                   implements Homomorphism<Query, RdbmsStore> {

	private static final Logger LOGGER = LoggerFactory.getLogger(SqlHomomorphism.class);

	private static SqlHomomorphism instance;

	private SqlHomomorphism() {
	}

	public static synchronized SqlHomomorphism instance() {
		if (instance == null)
			instance = new SqlHomomorphism();

		return instance;
	}

	// /////////////////////////////////////////////////////////////////////////
	// METHODS
	// /////////////////////////////////////////////////////////////////////////

	@Override
	public CloseableIterator<Substitution> execute(Query query, RdbmsStore store, Substitution s)
	    throws HomomorphismException {
		ConjunctiveQuery cquery = (ConjunctiveQuery) query;
		SQLQuery sqlQuery;
		// be aware to does not overwrite fixed variables (variable in s)
		FreshVarSubstitution varMap = new FreshVarSubstitution(new DefaultVariableGenerator("VAR"),
		                                                       CollectionUtils.removeAll(cquery.getAnswerVariables(),
		                                                           s.getTerms()));
		// prevent variable from initial substitution to be substitut by a fresh variable
		for (Variable v : s.getTerms()) {
			varMap.put(v, v);
		}

		try {
			sqlQuery = store.getConjunctiveQueryTranslator().translate(varMap.createImageOf(cquery), s);
//			System.out.println("GENERATED SQL QUERY: \n" + cquery + "\n" + sqlQuery);
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("GENERATED SQL QUERY: \n" + cquery + "\n" + sqlQuery);
		} catch (AtomSetException e) {
			throw new HomomorphismException("Error during query translation to SQL", e);
		}
		if (sqlQuery.hasSchemaError()) {
			return Iterators.<Substitution> emptyIterator();
		} else if (sqlQuery.isEmpty()) {
			return Iterators.<Substitution> singletonIterator(Substitutions.emptySubstitution());
		} else {
			try {
//				System.out.println("sqlQuery.toString():"+sqlQuery.toString());
				ResultSet results = store.getDriver().createStatement().executeQuery(sqlQuery.toString());
				CloseableIterator<ResultSet> resultsIt = new ResultSetCloseableIterator(results);
				return new ConverterCloseableIterator<ResultSet, Substitution>(resultsIt,
				                                                               new ResultSet2SubstitutionConverter(store.getConjunctiveQueryTranslator(),
				                                                                                                   cquery.getAnswerVariables(), varMap, s));
			} catch (Exception e) {
				throw new HomomorphismException("Error while evaluating the following query: [" + cquery + "] translated to SQL as: " + sqlQuery, e);
			}
		}
	}

}
