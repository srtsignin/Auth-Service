package service;

import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.Node;

import java.util.ArrayList;
import java.util.List;

public class Neo4JDriver {
    private static final String ROLES_QUERY_TEMPLATE = "match (:User {USERNAME: \"%s\"}) - [*] -> (r:Role) return r";
    private static final String URL = System.getProperty("neo4j.url");
    private static final String USERNAME = System.getProperty("neo4j.username");
    private static final String PASSWORD = System.getProperty("neo4j.password");

    private final Driver driver;
    private static Neo4JDriver singleton = null;

    public static synchronized Neo4JDriver getInstance() {
        if (singleton == null) {
                singleton = new Neo4JDriver();
        }
        return singleton;
    }

    private Neo4JDriver() {
        driver = GraphDatabase.driver(URL, AuthTokens.basic(USERNAME, PASSWORD));
    }

    public void close() {
        driver.close();
    }

    public String[] getRoles(String username) {
        try (Session session = driver.session()) {
            return session.writeTransaction(tx -> {
                List<String> rolesList = new ArrayList<>();
                StatementResult result = tx.run(String.format(ROLES_QUERY_TEMPLATE, username));
                result.forEachRemaining(record -> rolesList.add(extractRoleNameFromNode(record.values().get(0).asNode())));
                return rolesList.toArray(new String[0]);
            });
        }
    }

    private String extractRoleNameFromNode(Node node) {
        return node.get("role").toString().replace("\"", "");
    }
}
