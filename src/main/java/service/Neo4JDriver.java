package service;

import models.User;
import models.UserAndRoles;
import org.apache.log4j.Logger;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Neo4JDriver {
    private static final String ROLES_QUERY_TEMPLATE = "match (:User {username: \"%s\"}) - [*] -> (r:Role) return r";
    private static final String ALL_ROLES_QUERY = "match (r:Role) return r";
    private static final String ALL_USERS_QUERY = "match (n:User) return n";
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
            return session.readTransaction(tx -> {
                String[] rolesList = getRoles(tx, String.format(ROLES_QUERY_TEMPLATE, username));
                close();
                return rolesList;
            });
        }
    }


    public String[] getAllRoles() {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                String[] rolesList = getRoles(tx, ALL_ROLES_QUERY);
                close();
                return rolesList;
            });
        }
    }

    public List<UserAndRoles> getAll() {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                StatementResult result = tx.run(ALL_USERS_QUERY);
                List<UserAndRoles> users = new ArrayList<>();

                result.forEachRemaining(userNodeGraph -> userNodeGraph.values().forEach(userNode -> {
                    Map<String, Object> nodeFieldMap = userNode.asNode().asMap();
                    String username = nodeFieldMap.get("username").toString();
                    String fullName = nodeFieldMap.getOrDefault("name", "NO NAME").toString();
                    String[] rolesList = getRoles(tx, String.format(ROLES_QUERY_TEMPLATE, username));
                    User user = new User(username, fullName);
                    UserAndRoles userAndRoles = new UserAndRoles(rolesList, user);
                    users.add(userAndRoles);
                }));
                close();
                return users;
            });
        }
    }

    private String[] getRoles(Transaction tx, String query) {
        List<String> rolesList = new ArrayList<>();
        StatementResult result = tx.run(query);
        result.forEachRemaining(record -> rolesList.add(extractRoleNameFromNode(record.values().get(0).asNode())));
        return rolesList.toArray(new String[0]);
    }

    private String extractRoleNameFromNode(Node node) {
        return node.get("role").toString().replace("\"", "");
    }
}
