package service;

import org.apache.log4j.Logger;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.Node;

import java.util.ArrayList;
import java.util.List;

public class Neo4JDriver {
    private static final String ROLES_QUERY_TEMPLATE = "match (:User {username: \"%s\"}) - [*] -> (r:Role) return r";
    private static final String ALL_ROLES_QUERY = "match (r:Role) return r";
    private static final String URL = System.getProperty("neo4j.url");
    private static final String USERNAME = System.getProperty("neo4j.username");
    private static final String PASSWORD = System.getProperty("neo4j.password");
    private static final org.apache.log4j.Logger log = Logger.getLogger(Neo4JDriver.class);

    private final Driver driver;

    public static Neo4JDriver getInstance() {
        return new Neo4JDriver();
    }

    private Neo4JDriver() {
        driver = GraphDatabase.driver(URL, AuthTokens.basic(USERNAME, PASSWORD));
    }

    private void close() {
        driver.close();
    }

    public String[] getRoles(String username) {
        try (Session session = driver.session()) {
            return session.writeTransaction(tx -> {
                List<String> rolesList = new ArrayList<>();
                StatementResult result = tx.run(String.format(ROLES_QUERY_TEMPLATE, username));
                result.forEachRemaining(record -> rolesList.add(extractRoleNameFromNode(record.values().get(0).asNode())));
                close();
                return rolesList.toArray(new String[0]);
            });
        }
    }

    private String extractRoleNameFromNode(Node node) {
        return node.get("role").toString().replace("\"", "");
    }

    public String[] getAllRoles() {
        try (Session session = driver.session()) {
            return session.writeTransaction(tx -> {
                List<String> rolesList = new ArrayList<>();
                StatementResult result = tx.run(String.format(ALL_ROLES_QUERY));
                result.forEachRemaining(record -> rolesList.add(extractRoleNameFromNode(record.values().get(0).asNode())));
                close();
                return rolesList.toArray(new String[0]);
            });
        }
    }
}
