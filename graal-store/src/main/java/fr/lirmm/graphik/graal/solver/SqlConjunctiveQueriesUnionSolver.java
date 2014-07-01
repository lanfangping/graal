/**
 * 
 */
package fr.lirmm.graphik.graal.solver;

import java.sql.SQLException;
import java.util.Iterator;

import fr.lirmm.graphik.graal.core.ConjunctiveQueriesUnion;
import fr.lirmm.graphik.graal.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.core.stream.SubstitutionReader;
import fr.lirmm.graphik.graal.store.rdbms.RdbmsStore;
import fr.lirmm.graphik.graal.store.rdbms.ResultSetSubstitutionReader;

/**
 * @author Clément Sipieter (INRIA) <clement@6pi.fr>
 * 
 */
public class SqlConjunctiveQueriesUnionSolver implements Solver {

	private RdbmsStore store;
	private StringBuilder sqlQuery;
	private ConjunctiveQueriesUnion queries;

	public SqlConjunctiveQueriesUnionSolver(ConjunctiveQueriesUnion queries,
			RdbmsStore store) {
		this.queries = queries;
		this.store = store;
	}

	private void preprocessing() throws SolverException {
		Iterator<ConjunctiveQuery> it = queries.iterator();
		this.sqlQuery = new StringBuilder();
		try {
			if (it.hasNext()) {
				this.sqlQuery.append(this.store.transformToSQL(it.next()));

				while (it.hasNext()) {
					this.sqlQuery.append(" UNION ");
					this.sqlQuery.append(this.store.transformToSQL(it.next()));
				}
			}
		} catch (Exception e) {
			throw new SolverException("Error during query translation to SQL",
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.lirmm.graphik.alaska.solver.ISolver#execute()
	 */
	@Override
	public SubstitutionReader execute() throws SolverException {
		this.preprocessing();
		try {
			return new ResultSetSubstitutionReader(this.store, this.sqlQuery.toString(), queries.isBoolean());
		} catch (Exception e) {
			throw new SolverException(e.getMessage(), e);
		}
	}

}
