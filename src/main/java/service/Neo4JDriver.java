package service;

import exceptions.OnlyOneAdminException;
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

    private static final String EDIT_USER_TEMPLATE = "merge (u:User {username: \"%s\", name: \"%s\"}) return u";
    private static final String DELETE_USER_QUERY = "match (u:User {username: \"%s\"}) detach delete u";
    private static final String ADMINS_QUERY = "match (u:User) - [:IsA] - (:Role {role: \"Admin\"}) return u";
    private static final String DELETE_ASSOCIATED_ROLES_QUERY = "match (:User {username: \"%s\"}) - [r:IsA] - () delete r";
    private static final String ADD_ROLE_QUERY = "match (u:User {username: \"%s\"}) match (r:Role {role: \"%s\"}) merge (u) - [:IsA] - (r) return u";

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

    private List<UserAndRoles> getUserAndRolesList(Transaction tx, StatementResult result) {
        List<UserAndRoles> users = new ArrayList<>();
        if (result == null) {
            return users;
        }

        result.forEachRemaining(userNodeGraph -> userNodeGraph.values().forEach(userNode -> {
            Map<String, Object> nodeFieldMap = userNode.asNode().asMap();
            String username = nodeFieldMap.get("username").toString();
            String fullName = nodeFieldMap.getOrDefault("name", "NO NAME").toString();
            String[] rolesList = getRoles(tx, String.format(ROLES_QUERY_TEMPLATE, username));
            User user = new User(username, fullName);
            UserAndRoles userAndRoles = new UserAndRoles(rolesList, user);
            users.add(userAndRoles);
        }));
        return users;
    }

    public List<UserAndRoles> getAll() {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                StatementResult result = tx.run(ALL_USERS_QUERY);
                return getUserAndRolesList(tx, result);
            });
        }
    }

    public List<UserAndRoles> editUser(String username, String name, List<String> roles) {
        try (Session session = driver.session(AccessMode.WRITE)) {
            session.writeTransaction(tx -> {
                tx.run(String.format(DELETE_ASSOCIATED_ROLES_QUERY, username));
                return null;
            });
            session.writeTransaction(tx -> {
                tx.run(String.format(EDIT_USER_TEMPLATE, username, name));
                return null;
            });
            return session.writeTransaction(tx -> {
                StatementResult roleResult = null;

                for (String role : roles) {
                    roleResult = tx.run(String.format(ADD_ROLE_QUERY, username, role));
                }
                return getUserAndRolesList(tx, roleResult);
            });
        }
    }

    public boolean deleteUser(String username) {
        try (Session session = driver.session()) {
            return session.writeTransaction(tx -> {
                StatementResult adminsResult = tx.run(ADMINS_QUERY);
                if (adminsResult.list().size() <= 1) {
                    throw new OnlyOneAdminException("Cannot Delete Last Admin");
                }

                tx.run(String.format(DELETE_USER_QUERY, username));
                return true;
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
