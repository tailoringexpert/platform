# keycloak
CREATE USER IF NOT EXISTS  'keycloak' IDENTIFIED BY 'test1234';
CREATE DATABASE IF NOT EXISTS keycloak CHARACTER SET utf8mb4;
GRANT ALL PRIVILEGES ON keycloak.* TO 'keycloak'@'%';

CREATE USER IF NOT EXISTS 'tailoringexpert' IDENTIFIED BY 'test1234';
CREATE DATABASE IF NOT EXISTS tailoringexpert CHARACTER SET utf8mb4;
GRANT ALL PRIVILEGES ON tailoringexpert.* TO 'tailoringexpert'@'%';

CREATE USER IF NOT EXISTS  'tailoringexpert_tenant' IDENTIFIED BY 'test1234';
CREATE DATABASE IF NOT EXISTS tailoringexpert_tenant CHARACTER SET utf8mb4;
GRANT ALL PRIVILEGES ON tailoringexpert_tenant.* TO 'tailoringexpert_tenant'@'%';
