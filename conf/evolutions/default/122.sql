# --- !Ups
ALTER TABLE NOTIFICATION ADD COLUMN VERSION TINYINT DEFAULT 1 AFTER TYPE;

# --- !Downs
ALTER TABLE NOTIFICATION DROP COLUMN VERSION;