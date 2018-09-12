# Role-Service
Provides Endpoints for Determining User Roles

## Neo4j DB Creation Script

CREATE CONSTRAINT ON (n:User) ASSERT n.username IS UNIQUE

MERGE (:Role {role : "Admin"})
MERGE (:Role {role : "Staff"})
MERGE (:Role {role : "Tutor"})
MERGE (:Role {role : "Student"})

MERGE (:User {username : "keinslc"})

MATCH (me :User {username : "keinslc"})
MATCH (admin :Role {role : "Admin"})
MERGE (me) - [:IsA] ->(admin)

MERGE (:User {username : "boylecj"})

MATCH (me :User {username : "boylecj"})
MATCH (admin :Role {role : "Admin"})
MERGE (me) - [:IsA] ->(admin)
