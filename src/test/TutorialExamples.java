package test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.DatasetImpl;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.ntriples.NTriplesWriter;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLWriter;

import com.franz.agraph.repository.AGBooleanQuery;
import com.franz.agraph.repository.AGCatalog;
import com.franz.agraph.repository.AGGraphQuery;
import com.franz.agraph.repository.AGRepository;
import com.franz.agraph.repository.AGRepositoryConnection;
import com.franz.agraph.repository.AGServer;
import com.franz.agraph.repository.AGTupleQuery;
import com.franz.agraph.repository.AGValueFactory;

public class TutorialExamples {
	private static final String SERVER_URL = "http://localhost:10035";
	private static final String CATALOG_ID = "/";
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

		System.out.println(
				"Available repositories in catalog " + (catalog.getCatalogName()) + ": " + catalog.listRepositories());

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

		AGValueFactory vf = conn.getRepository().getValueFactory();
		System.out.println("Starting example example2().");

		IRI alice = vf.createIRI("http://example.org/people/alice");
		IRI bob = vf.createIRI("http://example.org/people/bob");
		// Name attribute
		IRI name = vf.createIRI("http://example.org/ontology/name");

		IRI person = vf.createIRI("http://example.org/ontology/Person");

		// literal values
		Literal bobsName = vf.createLiteral("Bob");
		Literal alicesName = vf.createLiteral("Alice");
		
		conn.clear();
		
		System.out.println("Triple count before inserts: " + (conn.size()));
		// Alice's name is "Alice" 
		conn.add(alice, name, alicesName); 
		// Alice is a person
		conn.add(alice, RDF.TYPE, person); // Bob's name is "Bob"
		conn.add(bob, name, bobsName); // Bob is a person, too.
		conn.add(bob, RDF.TYPE, person);

		/*System.out.println("Triple count after inserts: " + (conn.size()));

		RepositoryResult<Statement> result = conn.getStatements(null, null, null, false);
		while (result.hasNext()) {
			Statement st = result.next();
			System.out.println(st);
		}

		conn.remove(bob, name, bobsName);

		System.out.println("Removed one triple.");
		System.out.println("Triple count after deletion: " + (conn.size()));*/

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

		// STRING MATCHING

		/*
		 * URI favoriteColor = f.createURI(exns, "favoriteColor"); Literal UCred =
		 * f.createLiteral("Red"); Literal LCred = f.createLiteral("red"); Literal
		 * RedPlain = f.createLiteral("Red"); Literal rouge = f.createLiteral("rouge",
		 * XMLSchema.STRING); Literal Rouge = f.createLiteral("Rouge",
		 * XMLSchema.STRING); Literal RougePlain = f.createLiteral("Rouge"); Literal
		 * FrRouge = f.createLiteral("Rouge", "fr");
		 * 
		 * conn.add(alice, favoriteColor, UCred); conn.add(bob, favoriteColor, LCred);
		 * conn.add(carol, favoriteColor, RedPlain); conn.add(dave, favoriteColor,
		 * rouge); conn.add(eric, favoriteColor, Rouge); conn.add(fred, favoriteColor,
		 * RougePlain); conn.add(greg, favoriteColor, FrRouge);
		 * 
		 * RepositoryResult<Statement> statements = conn.getStatements(null,
		 * favoriteColor, null, false); try { while (statements.hasNext()) {
		 * System.out.println(statements.next()); } } finally { statements.close(); }
		 * 
		 * // SPARQL direct match SELECT ?s ?p WHERE {?s ?p "Red"} String queryString =
		 * "SELECT ?s ?p WHERE {?s ?p \"Red\"}"; AGTupleQuery tupleQuery =
		 * conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString); TupleQueryResult
		 * result = tupleQuery.evaluate();
		 * 
		 * try { System.out.
		 * println("\nSPARQL direct match 	SELECT ?s ?p WHERE {?s ?p \"Red\"}"); while
		 * (result.hasNext()) { BindingSet bindingSet = result.next(); Value s =
		 * bindingSet.getValue("s"); Value p = bindingSet.getValue("p"); Value o =
		 * bindingSet.getValue("o"); System.out.println(s + " " + p + " " + o); } }
		 * finally { result.close(); }
		 * 
		 * // SPARQL filter match SELECT ?s ?p ?o WHERE {?s ?p ?o . filter (?o = "Red")}
		 * queryString = "SELECT ?s ?p ?o WHERE {?s ?p ?o . filter (?o = \"Red\")}";
		 * tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		 * result = tupleQuery.evaluate();
		 * 
		 * try { System.out.
		 * println("\nSELECT ?s ?p ?o WHERE {?s ?p ?o . filter (?o = \"Red\")}"); while
		 * (result.hasNext()) { BindingSet bindingSet = result.next(); Value s =
		 * bindingSet.getValue("s"); Value p = bindingSet.getValue("p"); Value o =
		 * bindingSet.getValue("o"); System.out.println(s + " " + p + " " + o); } }
		 * finally { result.close(); }
		 * 
		 * // SPARQL direct match SELECT ?s ?p WHERE {?s ?p "Rouge"} queryString =
		 * "SELECT ?s ?p WHERE {?s ?p \"Rouge\"}"; tupleQuery =
		 * conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString); result =
		 * tupleQuery.evaluate();
		 * 
		 * try { System.out.println("\nSELECT ?s ?p WHERE {?s ?p \"Rouge\"}"); while
		 * (result.hasNext()) { BindingSet bindingSet = result.next(); Value s =
		 * bindingSet.getValue("s"); Value p = bindingSet.getValue("p"); Value o =
		 * bindingSet.getValue("o"); System.out.println(s + " " + p + " " + o); } }
		 * finally { result.close(); }
		 * 
		 * // SPARQL filter match SELECT ?s ?p ?o WHERE {?s ?p ?o . filter (?o =
		 * "Rouge")} queryString =
		 * "SELECT ?s ?p ?o WHERE {?s ?p ?o . filter (?o = \"Rouge\")}"; tupleQuery =
		 * conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString); result =
		 * tupleQuery.evaluate();
		 * 
		 * try { System.out.
		 * println("\nSELECT ?s ?p ?o WHERE {?s ?p ?o . filter (?o = \"Rouge\")}");
		 * while (result.hasNext()) { BindingSet bindingSet = result.next(); Value s =
		 * bindingSet.getValue("s"); Value p = bindingSet.getValue("p"); Value o =
		 * bindingSet.getValue("o"); System.out.println(s + " " + p + " " + o); } }
		 * finally { result.close(); }
		 * 
		 * // lowercase() function eliminates case issues: fn:lower-case(), str()
		 * queryString =
		 * "PREFIX fn: <http://www.w3.org/2005/xpath-functions#> SELECT ?s ?p ?o WHERE {?s ?p ?o . filter (fn:lower-case(str(?o)) = \"rouge\")}"
		 * ; tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		 * result = tupleQuery.evaluate();
		 * 
		 * try { System.out.
		 * println("\nPREFIX fn: <http://www.w3.org/2005/xpath-functions#> SELECT ?s ?p ?o WHERE {?s ?p ?o . filter (fn:lower-case(str(?o)) = \\\"rouge\\\")}"
		 * ); while (result.hasNext()) { BindingSet bindingSet = result.next(); Value s
		 * = bindingSet.getValue("s"); Value p = bindingSet.getValue("p"); Value o =
		 * bindingSet.getValue("o"); System.out.println(s + " " + p + " " + o); } }
		 * finally { result.close(); }
		 */

		// BOOLEAN MATCHING

		/*
		 * URI senior = f.createURI(exns, "senior");
		 * 
		 * Literal trueValue = f.createLiteral("true", XMLSchema.BOOLEAN); Literal
		 * falseValue = f.createLiteral("false", XMLSchema.BOOLEAN);
		 * 
		 * conn.add(alice, senior, trueValue); conn.add(bob, senior, falseValue);
		 * 
		 * RepositoryResult<Statement> statements = conn.getStatements(null, senior,
		 * trueValue, false);
		 * 
		 * try { System.out.println("\nBoolean matching using getStatements()");
		 * while(statements.hasNext()) { System.out.println(statements.next()); } }
		 * finally { statements.close(); }
		 * 
		 * // SPARQL direct match SELECT ?s ?p WHERE {?s ?p true} String queryString =
		 * "SELECT ?s ?p WHERE {?s ?p true}"; AGTupleQuery tupleQuery =
		 * conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString); TupleQueryResult
		 * result = tupleQuery.evaluate();
		 * 
		 * try {
		 * System.out.println("\nSPARQL direct match 	SELECT ?s ?p WHERE {?s ?p true}"
		 * ); while(result.hasNext()) { BindingSet bindingSet = result.next(); Value s =
		 * bindingSet.getValue("s"); Value p = bindingSet.getValue("p"); Value o =
		 * bindingSet.getValue("o"); System.out.println(s + " " + p + " " + o); } }
		 * finally { result.close(); }
		 * 
		 * 
		 * // SPARQL direct match SELECT ?s ?p WHERE {?s ?p
		 * "true"^^<http://www.w3.org/2001/XMLSchema#boolean> queryString =
		 * "SELECT ?s ?p WHERE {?s ?p \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean>}"
		 * ; tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		 * result = tupleQuery.evaluate();
		 * 
		 * try { System.out.
		 * println("\nSPARQL direct match 	SELECT ?s ?p WHERE {?s ?p \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean>"
		 * ); while(result.hasNext()) { BindingSet bindingSet = result.next(); Value s =
		 * bindingSet.getValue("s"); Value p = bindingSet.getValue("p"); Value o =
		 * bindingSet.getValue("o"); System.out.println(s + " " + p + " " + o); } }
		 * finally { result.close(); }
		 * 
		 * 
		 * // SPARQL filter match SELECT ?s ?p ?o WHERE {?s ?p ?o . filter (?o = true
		 * queryString = "SELECT ?s ?p ?o WHERE {?s ?p ?o . filter (?o = true)}";
		 * tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		 * result = tupleQuery.evaluate();
		 * 
		 * try { System.out.
		 * println("\nSPARQL filter match 	SELECT ?s ?p ?o WHERE {?s ?p ?o . filter (?o = true)}"
		 * ); while(result.hasNext()) { BindingSet bindingSet = result.next(); Value s =
		 * bindingSet.getValue("s"); Value p = bindingSet.getValue("p"); Value o =
		 * bindingSet.getValue("o"); System.out.println(s + " " + p + " " + o); } }
		 * finally { result.close(); }
		 * 
		 * // SPARQL filter match SELECT ?s ?p ?o WHERE {?s ?p ?o . filter (?o =
		 * "true"^^<http://www.w3.org/2001/XMLSchema#boolean>} queryString =
		 * "SELECT ?s ?p ?o WHERE {?s ?p ?o . filter (?o = \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean>)}"
		 * ; tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		 * result = tupleQuery.evaluate();
		 * 
		 * try { System.out.
		 * println("\nSPARQL filter match 	SELECT ?s ?p ?o WHERE {?s ?p ?o . filter (?o = \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean>}"
		 * ); while(result.hasNext()) { BindingSet bindingSet = result.next(); Value s =
		 * bindingSet.getValue("s"); Value p = bindingSet.getValue("p"); Value o =
		 * bindingSet.getValue("o"); System.out.println(s + " " + p + " " + o); } }
		 * finally { result.close(); }
		 */

		// DATE MATCHING

		URI birthdate = f.createURI(exns, "birthdate");
		// SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		// Date today = new Date();
		// Literal date = f.createLiteral(dateFormat.format(today), XMLSchema.DATE);
		//
		// conn.add(alice, birthDate, date);
		//
		// System.out.println("Retreive triples matching DATE Object");
		// RepositoryResult<Statement> statements = conn.getStatements(null, null, date,
		// false);
		//
		// try {
		// while(statements.hasNext()) {
		// System.out.println(statements.next());
		// }
		// }
		// finally {
		// statements.close();
		// }

		Literal date = f.createLiteral("1984-12-06", XMLSchema.DATE);
		Literal datetime = f.createLiteral("1984-12-06T09:00:00", XMLSchema.DATETIME);
		Literal time = f.createLiteral("09:00:00", XMLSchema.TIME);
		Literal datetimeOffset = f.createLiteral("1984-12-06T09:00:00+01:00", XMLSchema.DATETIME);

		/*
		 * System.out.println(date); System.out.println(datetime);
		 * System.out.println(time); System.out.println(datetimeOffset);
		 */

		conn.add(alice, birthdate, date);
		conn.add(bob, birthdate, datetime);
		conn.add(carol, birthdate, time);
		conn.add(dave, birthdate, datetimeOffset);

		/*
		 * RepositoryResult<Statement> statements = conn.getStatements(null, birthdate,
		 * null, false);
		 * 
		 * try { while(statements.hasNext()) { System.out.println(statements.next()); }
		 * } finally { statements.close(); }
		 */

		// getStatements() conn.getStatements(null, birthdate, date, false)
		RepositoryResult<Statement> statements = conn.getStatements(null, birthdate, date, false);

		try {
			System.out.println("getStatements() 	conn.getStatements(null, birthdate, date, false)");
			while (statements.hasNext()) {
				System.out.println(statements.next());
			}
		} finally {
			statements.close();
		}

		// SELECT ?s ?p WHERE {?s ?p
		// '1984-12-06'^^<http://www.w3.org/2001/XMLSchema#date>
		String queryString = "SELECT ?s ?p ?o WHERE {?s ?p '1984-12-06'^^<http://www.w3.org/2001/XMLSchema#date>}";
		AGTupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		TupleQueryResult result = tupleQuery.evaluate();

		try {
			System.out.println("\nSELECT ?s ?p WHERE {?s ?p '1984-12-06'^^<http://www.w3.org/2001/XMLSchema#date>}");
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

		// SPARQL filter match SELECT ?s ?p ?o WHERE {?s ?p ?o . filter (?o
		// ='1984-12-06'^^<http://www.w3.org/2001/XMLSchema#date>)}
		queryString = "SELECT ?s ?p ?o WHERE {?s ?p ?o . filter (?o ='1984-12-06'^^<http://www.w3.org/2001/XMLSchema#date>)}";
		tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		result = tupleQuery.evaluate();

		try {
			System.out.println(
					"\nSPARQL filter match 	SELECT ?s ?p ?o WHERE {?s ?p ?o . filter (?o ='1984-12-06'^^<http://www.w3.org/2001/XMLSchema#date>)}");
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

		// SPARQL direct match SELECT ?s ?p WHERE {?s ?p
		// '1984-12-06'^^<http://www.w3.org/2001/XMLSchema#date>}
		queryString = "SELECT ?s ?p ?o WHERE {?s ?p '1984-12-06'^^<http://www.w3.org/2001/XMLSchema#date>}";
		tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		result = tupleQuery.evaluate();

		try {
			System.out.println(
					"\nSPARQL direct match 	SELECT ?s ?p WHERE {?s ?p '1984-12-06'^^<http://www.w3.org/2001/XMLSchema#date>}");
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

		// SPARQL filter match SELECT ?s ?p ?o WHERE {?s ?p ?o . filter (?o =
		// '1984-12-06'^^<http://www.w3.org/2001/XMLSchema#date>)}
		queryString = "SELECT ?s ?p ?o WHERE {?s ?p ?o . filter (?o = '1984-12-06'^^<http://www.w3.org/2001/XMLSchema#date>)}";
		tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		result = tupleQuery.evaluate();

		try {
			System.out.println(
					"\nSELECT ?s ?p ?o WHERE {?s ?p ?o . filter (?o = '1984-12-06'^^<http://www.w3.org/2001/XMLSchema#date>)}");
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

	public static AGRepositoryConnection example6() throws RDFParseException, RepositoryException, IOException {
		AGRepositoryConnection conn = AGServer.createRepositoryConnection(REPOSITORY_ID, CATALOG_ID, SERVER_URL,
				USERNAME, PASSWORD);
		conn.clear();
		conn.begin();
		conn.setAutoCommit(false);
		ValueFactory f = conn.getValueFactory();

		final File path1 = new File(DATA_DIR, "java-vcards.rdf");
		final File path2 = new File(DATA_DIR, "java-kennedy.ntriples");

		String baseURI = "http://example.org/example/local";

		URI context = f.createURI("http://example.org#vcards");
		conn.add(path1, baseURI, RDFFormat.RDFXML, context);
		conn.add(path2, baseURI, RDFFormat.NTRIPLES);

		System.out.println("After loading, repository contains " + conn.size(context) + " vcard triples in context '"
				+ context + "'\n and " + conn.size((Resource) null) + " kennedy triples in context 'null'.");

		return conn;
	}

	public static void example7() throws RDFParseException, RepositoryException, IOException {
		AGRepositoryConnection conn = example6();
		System.out.println("\nMatch all anf print subjects and contexts");
		RepositoryResult<Statement> result = conn.getStatements(null, null, null, false);
		for (int i = 0; i < 25 && result.hasNext(); i++) {
			Statement stmt = result.next();
			System.out.println(stmt.getSubject() + " " + stmt.getContext());
		}
		result.close();

		System.out.println("\nSame thing with SPARQL query (cant' retrieve triples in the null context)");
		String queryString = "SELECT DISTINCT ?s ?c WHERE {graph ?c {?s ?p ?o .} }";
		AGTupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		TupleQueryResult qresult = tupleQuery.evaluate();
		while (qresult.hasNext()) {
			BindingSet bindingSet = qresult.next();
			System.out.println(bindingSet.getBinding("s") + " " + bindingSet.getBinding("c"));
		}
		qresult.close();
		conn.close();
	}

	public static void example8() throws RDFParseException, RepositoryException, IOException {
		AGRepositoryConnection conn = example6();
		Repository myRepository = conn.getRepository();

		URI context = myRepository.getValueFactory().createURI("http://example.org#vcards");

		String outputFile = "/tmp/temp.nt";

		if (outputFile == null) {
			System.out.println("\nWriting-triples to Standard Out instead of to a file");
		} else {
			System.out.println("\nWriting n-triples to: " + outputFile);
		}

		OutputStream output = (outputFile != null) ? new FileOutputStream(outputFile) : System.out;
		NTriplesWriter nTriplesWriter = new NTriplesWriter(output);
		conn.export(nTriplesWriter, context);

		String outputFile2 = "/tmp/temp.rdf";
		// outputFile2 = null;
		if (outputFile2 == null) {
			System.out.println("\nWriting RDF to Standard out instead of to a file!");
		} else {
			System.out.println("\nWriting RDF to: " + outputFile2);
		}
		output = (outputFile2 != null) ? new FileOutputStream(outputFile2) : System.out;
		RDFXMLWriter rdfxmlWriter = new RDFXMLWriter(output);
		conn.export(rdfxmlWriter, context);
		output.write('\n');

		conn.close();
	}

	public static void example9() throws RDFParseException, RepositoryException, IOException {
		AGRepositoryConnection conn = example6();
		conn.exportStatements(null, RDF.TYPE, null, false, new RDFXMLWriter(System.out));

	}

	public static void example10() {
		AGRepositoryConnection conn = example1(false);
		ValueFactory f = conn.getValueFactory();
		String exns = "http://example.org/people/";

		// Create URIS for resources, predicates and classes
		IRI alice = f.createIRI(exns, "alice");
		IRI bob = f.createIRI(exns, "bob");
		IRI ted = f.createIRI(exns, "ted");
		IRI person = f.createIRI("http://example.org/ontology/Person");
		IRI name = f.createIRI("http://example.org/ontology/name");

		// Create literal name values
		Literal alicesName = f.createLiteral("Alice");
		Literal bobsName = f.createLiteral("Bob");
		Literal tedsName = f.createLiteral("Ted");

		// create URIs to identify the named contexts
		IRI context1 = f.createIRI(exns, "context1");
		IRI context2 = f.createIRI(exns, "context2");

		// assemble new statements and add them to the contexts
		conn.clear();
		conn.add(alice, RDF.TYPE, person, context1);
		conn.add(alice, name, alicesName, context1);
		conn.add(bob, RDF.TYPE, person, context2);
		conn.add(bob, name, bobsName, context2);
		conn.add(ted, RDF.TYPE, person);
		conn.add(ted, name, tedsName);

		// getStatements()
		// all
		RepositoryResult<Statement> statements = conn.getStatements(null, null, null, false);
		System.out.println("\nAll triples in all contexts: " + conn.size());
		while (statements.hasNext()) {
			System.out.println(statements.next());
		}

		// context1 and context2
		statements = conn.getStatements(null, null, null, false, context1, context2);
		System.out.println("\nTriples in contexts 1 or 2: " + (conn.size(context1) + conn.size(context2)));
		while (statements.hasNext()) {
			System.out.println(statements.next());
		}

		// null and context2
		statements = conn.getStatements(null, null, null, false, null, context2);
		System.out.println("\nTriples in contexts 1 or 2: " + (conn.size((Resource) null) + conn.size(context2)));
		while (statements.hasNext()) {
			System.out.println(statements.next());
		}

		// SPARQL using FROM and FROM NAMED
		// using from default
		String queryString = "select ?s ?p ?p from default where {?s ?p ?o . }";
		AGTupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		TupleQueryResult result = tupleQuery.evaluate();

		System.out.println("\nSelect from unamed graph.");
		while (result.hasNext()) {
			System.out.println(result.next());
		}
		result.close();

		// using from [name]
		queryString = "select ?s ?p ?o from <http://example.org/people/context1> where {?s ?p ?o . }";
		tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		result = tupleQuery.evaluate();

		System.out.println("\nSelect from named grpah context1");
		while (result.hasNext()) {
			System.out.println(result.next());
		}

		result.close();

		// using from named
		queryString = "select ?s ?p ?o from named <http://example.org/people/context1> where {graph ?g {?s ?p ?o . }}";
		tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		result = tupleQuery.evaluate();

		System.out.println("\nusing from named to find triples from context 1");
		while (result.hasNext()) {
			System.out.println(result.next());
		}
		result.close();

		// mixed query
		queryString = "select ?s ?p ?o ?g from default " + "from <http://example.org/people/context1> "
				+ "from named <http://example.org/people/context2> "
				+ "where {{graph ?g {?s ?p ?o . }} union {?s ?p ?o . }} ";
		tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		result = tupleQuery.evaluate();

		System.out.println("\nusing mixed query");
		while (result.hasNext()) {
			System.out.println(result.next());
		}
		result.close();

		// wide-open search
		queryString = "select ?s ?p ?o where {?s ?p ?o . }";
		tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		result = tupleQuery.evaluate();
		System.out.println("\n" + queryString);

		while (result.hasNext()) {
			System.out.println(result.next());
		}
		result.close();

		// USING DATASET
		// select from null context
		queryString = "select ?s ?p ?o where {?s ?p ?o . }";
		DatasetImpl ds = new DatasetImpl();
		ds.addDefaultGraph(null);
		tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		tupleQuery.setDataset(ds);
		result = tupleQuery.evaluate();

		System.out.println("\nQuery over the null context.");
		while (result.hasNext()) {
			System.out.println(result.next());
		}

		// select from context1
		queryString = "SELECT ?s ?p ?o ?c WHERE { graph ?c {?s ?p ?o . } }";
		ds = new DatasetImpl();
		ds.addNamedGraph(context1);
		tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		tupleQuery.setDataset(ds);
		result = tupleQuery.evaluate();
		System.out.println("\n" + queryString + " Dataset for context 1, using Graph.");
		while (result.hasNext()) {
			BindingSet bindingSet = result.next();
			System.out.println(bindingSet.getBinding("s") + " " + bindingSet.getBinding("p") + " "
					+ bindingSet.getBinding("o") + " " + bindingSet.getBinding("c"));
		}
		result.close();

		// without any data set
		queryString = "select ?s ?p ?o ?c where {graph ?c {?s ?p ?o . } }";
		tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		result = tupleQuery.evaluate();
		System.out.println("\n" + queryString + "No dataset SPARQL GRAPH query only.");
		while (result.hasNext()) {
			System.out.println(result.next());
		}
		result.close();

	}

	public static void example11() {
		AGRepositoryConnection conn = example1(false);
		Repository myRepository = conn.getRepository();
		ValueFactory f = myRepository.getValueFactory();

		String exns = "http://example.org/people/";
		IRI alice = f.createIRI(exns, "alice");
		IRI person = f.createIRI(exns, "Person");

		conn.clear();
		conn.add(alice, RDF.TYPE, person);
		conn.setNamespace("ex", exns);
		String queryString = "select ?s ?p ?o " + "where {?s ?p ?o . filter ((?p = rdf:type) && (?o = ex:Person)) }";
		AGTupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		TupleQueryResult result = tupleQuery.evaluate();
		while (result.hasNext()) {
			System.out.println(result.next());
		}
		result.close();

		// using constants
		queryString = "select ?s where {?s rdf:type ex:person }";
		tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		result = tupleQuery.evaluate();
		while (result.hasNext()) {
			System.out.println(result.next());
		}
		result.close();
	}

	public static void example12() {
		AGRepositoryConnection conn = example1(false);
		ValueFactory f = conn.getValueFactory();
		String exns = "http://example.org/people/";
		conn.setNamespace("ex", exns);
		conn.createFreetextIndex("index1", new IRI[] { f.createIRI(exns, "fullname") });

		IRI alice = f.createIRI(exns, "alice1");
		IRI persontype = f.createIRI(exns, "Person");
		IRI fullname = f.createIRI(exns, "fullname");
		Literal alicename = f.createLiteral("Alice B. Toklas");
		IRI book = f.createIRI(exns, "book");
		IRI booktype = f.createIRI(exns, "Book");
		IRI booktitle = f.createIRI(exns, "title");
		Literal wonderland = f.createLiteral("Alice in Wonderland");

		conn.clear();
		conn.add(alice, RDF.TYPE, persontype);
		conn.add(alice, fullname, alicename);

		conn.add(book, RDF.TYPE, booktype);
		conn.add(book, booktitle, wonderland);

		String queryString = "select ?s ?p ?o where {?s ?p ?o . ?s fti:match 'Alice' . }";
		System.out.println("\n" + queryString);
		AGTupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		TupleQueryResult result = tupleQuery.evaluate();
		int count = 0;
		while (result.hasNext()) {
			BindingSet bindingSet = result.next();
			if (count < 5) {
				System.out.println(bindingSet);
			}
			count++;
		}

		queryString = "select ?s ?p ?o where {?s ?p ?o . ?s fti:match 'Ali*' . }";
		System.out.println("\n" + queryString);
		tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		result = tupleQuery.evaluate();
		count = 0;
		while (result.hasNext()) {
			BindingSet bindingSet = result.next();
			if (count < 5) {
				System.out.println(bindingSet);
			}
			count++;
		}

		queryString = "select ?s ?p ?o where {?s ?p ?o . ?s fti:match '?l?c?' . }";
		System.out.println("\n" + queryString);
		tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		result = tupleQuery.evaluate();
		count = 0;
		while (result.hasNext()) {
			BindingSet bindingSet = result.next();
			if (count < 5) {
				System.out.println(bindingSet);
			}
			count++;
		}

		queryString = "select ?s ?p ?o where {?s ?p ?o . filter regex(?o, \"lic\") }";
		System.out.println("\n" + queryString);
		tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		result = tupleQuery.evaluate();
		count = 0;
		while (result.hasNext()) {
			BindingSet bindingSet = result.next();
			if (count < 5) {
				System.out.println(bindingSet);
			}
			count++;
		}

	}

	public static void example13() throws RDFParseException, RepositoryException, IOException {
		AGRepositoryConnection conn = example6();
		conn.setNamespace("kdy", "http://www.franz.com/simple#");
		ValueFactory vf = conn.getValueFactory();
		IRI context = vf.createIRI("http://example.org#vcards");
		conn.remove((Resource) null, (IRI) null, (Value) null, context);

		String queryString = "select ?s where { ?s rdf:type kdy:person} limit 5";
		AGTupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		TupleQueryResult result = tupleQuery.evaluate();
		System.out.println("\nSelect some people: ");
		while (result.hasNext()) {
			System.out.println(result.next());
		}
		result.close();

		// ask
		queryString = "ask { ?s kdy:first-name 'John' }";
		AGBooleanQuery booleanQuery = conn.prepareBooleanQuery(QueryLanguage.SPARQL, queryString);
		boolean truth = booleanQuery.evaluate();
		System.out.println("\nask: is there anyone named John? " + truth);

		queryString = "ask { ?s kdy:first-name 'Alice' }";
		AGBooleanQuery booleanQuery2 = conn.prepareBooleanQuery(QueryLanguage.SPARQL, queryString);
		boolean truth2 = booleanQuery2.evaluate();
		System.out.println("\nask: is there anyone named Alice? " + truth2);

		// construct
		queryString = "construct {?a kdy:has-grandchild ?c} " + "where { ?a kdy:has-child ?b . "
				+ "?b kdy:has-child ?c . }";
		AGGraphQuery contructQuery = conn.prepareGraphQuery(QueryLanguage.SPARQL, queryString);
		GraphQueryResult gresult = contructQuery.evaluate();

		while (gresult.hasNext()) {
			conn.add(gresult.next());
		}

		long count = contructQuery.count();
		System.out.println("count: " + count);

		// describe
		queryString = "describe ?s ?o where { ?s kdy:has-grandchild ?o . } limit 1";
		AGGraphQuery describeQuery = conn.prepareGraphQuery(QueryLanguage.SPARQL, queryString);
		gresult = describeQuery.evaluate();
		System.out.println("\nDescribe one grandparent and one grandchild: ");
		while (gresult.hasNext()) {
			System.out.println(gresult.next());
		}

		// update
		// String updateString = "";
		String updateString = "PREFIX dc: <http://purl.org/dc/elements/1.1/> \n"
				+ "INSERT DATA { GRAPH <http://example/bookStore> { <http://example/book1>  dc:title  \"Fundamentals of Compiler Desing\" } }";
		System.out.println("\nPerforming SPARQL update:\n" + updateString);
		conn.prepareUpdate(QueryLanguage.SPARQL, updateString).execute();

		queryString = "PREFIX dc: <http://purl.org/dc/elements/1.1/> \n"
				+ "ASK { GRAPH <http://example/bookStore> { <http://example/book1>  dc:title  \"Fundamentals of Compiler Desing\" } }";
		System.out.println("\nPerforming query:\n" + queryString);
		System.out.println("Result: " + conn.prepareBooleanQuery(QueryLanguage.SPARQL, queryString).evaluate());

		updateString = "PREFIX dc: <http://purl.org/dc/elements/1.1/> \n"
				+ "DELETE DATA { GRAPH <http://example/bookStore> { <http://example/book1>  dc:title  \"Fundamentals of Compiler Desing\" } } ; \n"
				+ "\n" + "PREFIX dc: <http://purl.org/dc/elements/1.1/> \n"
				+ "INSERT DATA { GRAPH <http://example/bookStore> { <http://example/book1>  dc:title  \"Fundamentals of Compiler Design\" } }";

		System.out.println(
				"\nPerforming a sequence of SPARQL updates in one request (to correct the title):\n" + updateString);
		conn.prepareUpdate(QueryLanguage.SPARQL, updateString).execute();

		queryString = "PREFIX dc: <http://purl.org/dc/elements/1.1/> \n"
				+ "ASK { GRAPH <http://example/bookStore> { <http://example/book1>  dc:title  \"Fundamentals of Compiler Desing\" } }";
		System.out.println("\nPerforming query:\n" + queryString);
		System.out.println("Result: " + conn.prepareBooleanQuery(QueryLanguage.SPARQL, queryString).evaluate());

		queryString = "PREFIX dc: <http://purl.org/dc/elements/1.1/> \n"
				+ "ASK { GRAPH <http://example/bookStore> { <http://example/book1>  dc:title  \"Fundamentals of Compiler Design\" } }";
		System.out.println("\nPerforming the query:\n" + queryString);
		System.out.println("Result: " + conn.prepareBooleanQuery(QueryLanguage.SPARQL, queryString).evaluate());

	}

	public static void example14() {
		AGRepositoryConnection conn = example2(false);
		ValueFactory f = conn.getValueFactory();
		IRI alice = f.createIRI("http://example.org/people/alice");
		IRI bob = f.createIRI("http://example.org/people/bob");

		String queryString = "select ?s ?p ?o where { ?s ?p ?o }";
		AGTupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		tupleQuery.setBinding("s", alice);
		TupleQueryResult result = tupleQuery.evaluate();
		System.out.println("\nFacts about Alice:");
		while (result.hasNext()) {
			System.out.println(result.next());
		}
		result.close();
		
		tupleQuery.setBinding("s", bob);
		result = tupleQuery.evaluate();
		System.out.println("\nFacts about Bob:");
		while (result.hasNext()) {
			System.out.println(result.next());
		}
		result.close();
	}
	
	public static void example15() {
		System.out.println("Starting example example15().");
		AGRepositoryConnection conn = example1(false);
		ValueFactory f = conn.getValueFactory();
		conn.clear();
		
		String exns = "http://example.org/people/";
		conn.setNamespace("ex", exns);
		
		IRI alice = f.createIRI(exns, "alice");
		IRI bob = f.createIRI(exns, "bob");
		IRI carol = f.createIRI(exns, "carol");
		IRI age = f.createIRI(exns, "age");
		
		conn.add(alice, age, f.createLiteral(42));
		conn.add(bob, age, f.createLiteral(45.1));
		conn.add(carol, age, f.createLiteral("39"));
		
		System.out.println("\nRange query for integers and floats.");
		String queryString = "select ?s ?p ?o where { ?s ?p ?o .filter((?o >=30) && (?o <= 50)) }";
		AGTupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		TupleQueryResult result = tupleQuery.evaluate();
		
		while(result.hasNext()) {
			System.out.println(result.next());
		}
		result.close();
		
		
		System.out.println("\nmatching all int, double, string");
		queryString = "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " + 
				"select ?s ?p ?o where { ?s ?p ?o . filter ((xsd:integer(?o) >= 30) && (xsd:integer(?o) <= 50)) }";
		tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		result = tupleQuery.evaluate();
		
		while(result.hasNext()) {
			System.out.println(result.next());
		}
		result.close();
	}

	public static void main(String[] args) {
		try {
			example15();
		} catch (RDFParseException | RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}