package test;

import java.io.File;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;

import com.franz.agraph.repository.AGCatalog;
import com.franz.agraph.repository.AGRepository;
import com.franz.agraph.repository.AGRepositoryConnection;
import com.franz.agraph.repository.AGServer;
import com.franz.agraph.repository.AGTupleQuery;

public class TutorialExamples {
	private static final String SERVER_URL = "http://localhost:10035";
	private static final String CATALOG_ID = "scratch";
	private static final String REPOSITORY_ID = "javatutorial";
	private static final String USERNAME = "test";
	private static final String PASSWORD = "xyzzy";
	private static final File DATA_DIR = new File(".");

	private static final String FOAF_NS = "http://xmlns.com/foaf/0.1/";

	public static AGRepositoryConnection example1(boolean close) {
		System.out.println("\nStarting example1().");
		AGServer server = new AGServer(SERVER_URL, USERNAME, PASSWORD);
		System.out.println("Available catalogs: " + server.listCatalogs());

		AGCatalog catalog = server.getRootCatalog();
		/*
		 * System.out.println( "Available repositories in catalog " +
		 * (catalog.getCatalogName()) + ": " + catalog.listRepositories());
		 */

		AGRepository myRepository = catalog.createRepository(REPOSITORY_ID);
		/*
		 * System.out.println("Got a repository."); myRepository.initialize();
		 * System.out.println("Initialized repository.");
		 * System.out.println("Repository is writable? " + myRepository.isWritable());
		 */

		AGRepositoryConnection conn = myRepository.getConnection();
		/*
		 * System.out.println("Got a connection."); System.out.println("Repository " +
		 * (myRepository.getRepositoryID()) + " is up! It contains " + (conn.size()) +
		 * " statements."); List<String> indices = conn.listValidIndices();
		 * System.out.println("All valid triple indices: " + indices);
		 * 
		 * indices = conn.listIndices(); System.out.println("Current triple indices: " +
		 * indices);
		 */

		if (close) {
			conn.close();
			myRepository.shutDown();
			return null;
		}

		return conn;
	}

	public static AGRepositoryConnection example2(boolean close) {
		AGRepositoryConnection conn = example1(false);
		/*
		 * AGValueFactory vf = conn.getRepository().getValueFactory();
		 * System.out.println("Starting example example2().");
		 * 
		 * URI alice = vf.createURI("http://example.org/people/alice"); URI bob =
		 * vf.createURI("http://example.org/people/bob");
		 * 
		 * // Name attribute URI name =
		 * vf.createURI("http://example.org/ontology/name");
		 * 
		 * // Person type URI person =
		 * vf.createURI("http://example.org/ontology/Person");
		 * 
		 * // literal values Literal bobsName = vf.createLiteral("Bob"); Literal
		 * alicesName = vf.createLiteral("Alice");
		 * 
		 * System.out.println("Triple count before inserts: " + (conn.size()));
		 * 
		 * // Alice's name is "Alice" conn.add(alice, name, alicesName); // Alice is a
		 * person conn.add(alice, RDF.TYPE, person); // Bob's name is "Bob"
		 * conn.add(bob, name, bobsName); // Bob is a person, too. conn.add(bob,
		 * RDF.TYPE, person);
		 * 
		 * 
		 * System.out.println("Triple count after inserts: " + (conn.size()));
		 * 
		 * RepositoryResult<Statement> result = conn.getStatements(null, null, null,
		 * false); while (result.hasNext()) { Statement st = result.next();
		 * System.out.println(st); }
		 * 
		 * conn.remove(bob, name, bobsName);
		 * 
		 * System.out.println("Removed one triple.");
		 * System.out.println("Triple count after deletion: " + (conn.size()));
		 */

		return conn;
	}

	public static void example3() {
		AGRepositoryConnection conn = example2(false);
		System.out.println("\nStarting example3().");

		try {
			String queryString = "SELECT ?s ?p ?o  WHERE {?s ?p ?o .}";
			AGTupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			TupleQueryResult result = tupleQuery.evaluate();

			try {
				while (result.hasNext()) {
					BindingSet bindingSet = result.next();
					Value s = bindingSet.getValue("s");
					Value p = bindingSet.getValue("p");
					Value o = bindingSet.getValue("o");
					System.out.format("%s %s %s\n", s, p, o);
				}
			} finally {
				result.close();
			}
			long count = tupleQuery.count();
			System.out.println("count: " + count);
		} finally {

		}
	}

	public static void example4() {
		AGRepositoryConnection conn = example2(false);
		Repository myRepository = conn.getRepository();
		URI alice = myRepository.getValueFactory().createURI("http://example.org/people/alice");

		RepositoryResult<Statement> statements = conn.getStatements(alice, null, null, false);

		try {
			statements.enableDuplicateFilter();
			while (statements.hasNext()) {
				System.out.println(statements.next());
			}
		} finally {
			statements.close();
		}
		conn.close();
	}

	public static void example5() {
		AGRepositoryConnection conn = example2(false);
		Repository myRepository = conn.getRepository();
		ValueFactory f = myRepository.getValueFactory();
		System.out.println("\nStarting example5().");
		conn.clear();

		String exns = "http://people/";

		URI alice = f.createURI(exns, "alice");
		URI bob = f.createURI(exns, "bob");
		URI carol = f.createURI("http://people/carol");
		URI dave = f.createURI(exns, "dave");
		URI eric = f.createURI(exns, "eric");
		URI fred = f.createURI(exns, "fred");
		URI greg = f.createURI(exns, "greg");

		URI age = f.createURI(exns, "age");

		Literal fortyTwo = f.createLiteral(42); // creates int
		Literal fortyTwoDecimal = f.createLiteral(42.0); // creates float
		Literal fortyTwoInt = f.createLiteral("42", XMLSchema.INT);
		Literal fortyTwoLong = f.createLiteral("42", XMLSchema.LONG);
		Literal fortyTwoFloat = f.createLiteral("42", XMLSchema.FLOAT);
		Literal fortyTwoString = f.createLiteral("42", XMLSchema.STRING);
		Literal fortyTwoPlain = f.createLiteral("42"); // creates plain literal

		Statement stmt1 = f.createStatement(alice, age, fortyTwo);
		Statement stmt2 = f.createStatement(bob, age, fortyTwoDecimal);
		Statement stmt3 = f.createStatement(carol, age, fortyTwoInt);
		Statement stmt4 = f.createStatement(dave, age, fortyTwoLong);
		Statement stmt5 = f.createStatement(eric, age, fortyTwoFloat);
		Statement stmt6 = f.createStatement(fred, age, fortyTwoString);
		Statement stmt7 = f.createStatement(greg, age, fortyTwoPlain);

		conn.add(stmt1);
		conn.add(stmt2);
		conn.add(stmt3);
		conn.add(stmt4);
		conn.add(stmt5);
		conn.add(stmt6);
		conn.add(stmt7);

		/*
		 * System.out.
		 * println("\nShowing all age triples using getStatements().  Seven matches.");
		 * RepositoryResult<Statement> statements = conn.getStatements(null, age, null,
		 * false);
		 * 
		 * try { while(statements.hasNext()) { System.out.println(statements.next()); }
		 * } finally { statements.close(); }
		 * 
		 * // statements = conn.getStatements(null, age, 42, false);
		 * 
		 * // SPARQL direct match String queryString =
		 * "SELECT ?o ?p WHERE {?o ?p 42 .}"; AGTupleQuery tupleQuery =
		 * conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString); TupleQueryResult
		 * result = tupleQuery.evaluate();
		 * 
		 * try {
		 * System.out.println("\n SPAROL direct match SELECT ?s ?p WHERE {?s ?p 42.0 .}"
		 * ); while(result.hasNext()) { BindingSet bindingSet = result.next(); Value s =
		 * bindingSet.getValue("s"); Value p = bindingSet.getValue("p"); Value o =
		 * bindingSet.getValue("o"); System.out.println(s + " " + p + " " + o); } }
		 * finally { result.close(); }
		 * 
		 * // SPARQL filer match SELECT ?s ?p ?o WHERE {?s ?p ?o . filter (?o = 42)}
		 * queryString = "SELECT ?s ?p ?o WHERE {?s ?p ?o . filter (?o = 42)}";
		 * tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		 * result = tupleQuery.evaluate();
		 * 
		 * try { System.out.
		 * println("\n SPARQL filter SELECT ?s ?p ?o WHERE {?s ?p ?o . filter (?o = 42.0)}"
		 * ); while(result.hasNext()) { BindingSet bindingSet = result.next(); Value s =
		 * bindingSet.getValue("s"); Value p = bindingSet.getValue("p"); Value o =
		 * bindingSet.getValue("o"); System.out.println(s + " " + p + " " + o); } }
		 * finally { result.close(); }
		 * 
		 * // SPARQL direct match SELECT ?s ?p WHERE {?s ?p
		 * "42"^^<http://www.w3.org/2001/XMLSchema#int>} queryString =
		 * "SELECT ?s ?p WHERE {?s ?p \"42\"^^<http://www.w3.org/2001/XMLSchema#int>}";
		 * tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		 * result = tupleQuery.evaluate();
		 * 
		 * try { System.out.
		 * println("\n SPAROL direct match SELECT ?s ?p WHERE {?s ?p \\\"42\\\"^^<http://www.w3.org/2001/XMLSchema#int>}"
		 * ); while(result.hasNext()) { BindingSet bindingSet = result.next(); Value s =
		 * bindingSet.getValue("s"); Value p = bindingSet.getValue("p"); Value o =
		 * bindingSet.getValue("o"); System.out.println(s + " " + p + " " + o); } }
		 * finally { result.close(); }
		 * 
		 * // SPARQL filer match SELECT ?s ?p ?o WHERE {?s ?p ?o . filter (?o =
		 * "42"^^<http://www.w3.org/2001/XMLSchema#int>)} queryString =
		 * "SELECT ?s ?p ?o WHERE {?s ?p ?o . filter (?o = \"42\"^^<http://www.w3.org/2001/XMLSchema#int>)}"
		 * ; tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		 * result = tupleQuery.evaluate();
		 * 
		 * try { System.out.
		 * println("\n SPARQL filter SELECT ?s ?p ?o WHERE {?s ?p ?o . filter (?o = \\\"42\\\"^^<http://www.w3.org/2001/XMLSchema#int>)}"
		 * ); while(result.hasNext()) { BindingSet bindingSet = result.next(); Value s =
		 * bindingSet.getValue("s"); Value p = bindingSet.getValue("p"); Value o =
		 * bindingSet.getValue("o"); System.out.println(s + " " + p + " " + o); } }
		 * finally { result.close(); }
		 */

		URI favoriteColor = f.createURI(exns, "favoriteColor");
		Literal UCred = f.createLiteral("Red");
		Literal LCred = f.createLiteral("red");
		Literal RedPlain = f.createLiteral("Red");
		Literal rouge = f.createLiteral("rouge", XMLSchema.STRING);
		Literal Rouge = f.createLiteral("Rouge", XMLSchema.STRING);
		Literal RougePlain = f.createLiteral("Rouge");
		Literal FrRouge = f.createLiteral("Rouge", "fr");

		conn.add(alice, favoriteColor, UCred);
		conn.add(bob, favoriteColor, LCred);
		conn.add(carol, favoriteColor, RedPlain);
		conn.add(dave, favoriteColor, rouge);
		conn.add(eric, favoriteColor, Rouge);
		conn.add(fred, favoriteColor, RougePlain);
		conn.add(greg, favoriteColor, FrRouge);

		RepositoryResult<Statement> statements = conn.getStatements(null, favoriteColor, null, false);
		try {
			while (statements.hasNext()) {
				System.out.println(statements.next());
			}
		} finally {
			statements.close();
		}

		// SPARQL direct match SELECT ?s ?p WHERE {?s ?p "Red"}
		String queryString = "SELECT ?s ?p WHERE {?s ?p \"Red\"}";
		AGTupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		TupleQueryResult result = tupleQuery.evaluate();

		try {
			System.out.println("\nSPARQL direct match 	SELECT ?s ?p WHERE {?s ?p \"Red\"}");
			while (result.hasNext()) {
				BindingSet bindingSet = result.next();
				Value s = bindingSet.getValue("s");
				Value p = bindingSet.getValue("p");
				Value o = bindingSet.getValue("o");
				System.out.println(s + " " + p + " " + o);
			}
		} finally {
			result.close();
		}

		// SPARQL filter match SELECT ?s ?p ?o WHERE {?s ?p ?o . filter (?o = "Red")}
		queryString = "SELECT ?s ?p ?o WHERE {?s ?p ?o . filter (?o = \"Red\")}";
		tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		result = tupleQuery.evaluate();

		try {
			System.out.println("\nSELECT ?s ?p ?o WHERE {?s ?p ?o . filter (?o = \"Red\")}");
			while (result.hasNext()) {
				BindingSet bindingSet = result.next();
				Value s = bindingSet.getValue("s");
				Value p = bindingSet.getValue("p");
				Value o = bindingSet.getValue("o");
				System.out.println(s + " " + p + " " + o);
			}
		} finally {
			result.close();
		}

		// SPARQL direct match SELECT ?s ?p WHERE {?s ?p "Rouge"}
		queryString = "SELECT ?s ?p WHERE {?s ?p \"Rouge\"}";
		tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		result = tupleQuery.evaluate();

		try {
			System.out.println("\nSELECT ?s ?p WHERE {?s ?p \"Rouge\"}");
			while (result.hasNext()) {
				BindingSet bindingSet = result.next();
				Value s = bindingSet.getValue("s");
				Value p = bindingSet.getValue("p");
				Value o = bindingSet.getValue("o");
				System.out.println(s + " " + p + " " + o);
			}
		} finally {
			result.close();
		}

		// SPARQL filter match SELECT ?s ?p ?o WHERE {?s ?p ?o . filter (?o = "Rouge")}
		queryString = "SELECT ?s ?p ?o WHERE {?s ?p ?o . filter (?o = \"Rouge\")}";
		tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		result = tupleQuery.evaluate();

		try {
			System.out.println("\nSELECT ?s ?p ?o WHERE {?s ?p ?o . filter (?o = \"Rouge\")}");
			while (result.hasNext()) {
				BindingSet bindingSet = result.next();
				Value s = bindingSet.getValue("s");
				Value p = bindingSet.getValue("p");
				Value o = bindingSet.getValue("o");
				System.out.println(s + " " + p + " " + o);
			}
		} finally {
			result.close();
		}

		// lowercase() function eliminates case issues: fn:lower-case(), str()
		queryString = "PREFIX fn: <http://www.w3.org/2005/xpath-functions#> SELECT ?s ?p ?o WHERE {?s ?p ?o . filter (fn:lower-case(str(?o)) = \"rouge\")}";
		tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		result = tupleQuery.evaluate();

		try {
			System.out.println("\nPREFIX fn: <http://www.w3.org/2005/xpath-functions#> SELECT ?s ?p ?o WHERE {?s ?p ?o . filter (fn:lower-case(str(?o)) = \\\"rouge\\\")}");
			while (result.hasNext()) {
				BindingSet bindingSet = result.next();
				Value s = bindingSet.getValue("s");
				Value p = bindingSet.getValue("p");
				Value o = bindingSet.getValue("o");
				System.out.println(s + " " + p + " " + o);
			}
		} finally {
			result.close();
		}
	}

	public static void main(String[] args) {
		example5();
	}
}