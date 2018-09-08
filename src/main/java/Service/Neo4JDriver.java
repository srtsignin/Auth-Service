package Service;

import org.neo4j.driver.v1.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Neo4JDriver {
    private final String ROLES_QUERY_TEMPLATE = "match (:User {username: \"%s\"}) - [] - (r:Role) return r";
    private String url = System.getProperty("neo4j.url");
    private String username = System.getProperty("neo4j.username");
    private String password = System.getProperty("neo4j.password");

    private final Driver driver;

    public Neo4JDriver() {
        driver = GraphDatabase.driver(url, AuthTokens.basic(username, password));
    }

    public void close() {
        driver.close();
    }

    public String[] getRoles(String username) {
        try (Session session = driver.session()) {
            String[] roles = session.writeTransaction(tx -> {
                List<String> rolesList = new ArrayList<>();
                StatementResult result = tx.run(String.format(ROLES_QUERY_TEMPLATE, username));
                result.forEachRemaining(record -> rolesList.add(record.values().get(0).asNode().get("role").toString()));
                return rolesList.toArray(new String[0]);
            });
            return roles;
        }
    }
}
