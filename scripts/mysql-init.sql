-- ScaleMesh Database Initialization Script
-- Ensures all 3 microservice persistence databases exist on MySQL container startup

CREATE DATABASE IF NOT EXISTS scalemesh_telemetry CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS scalemesh_trips CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS scalemesh_incidents CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON scalemesh_telemetry.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON scalemesh_trips.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON scalemesh_incidents.* TO 'root'@'%';
FLUSH PRIVILEGES;
