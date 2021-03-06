# --- !Ups
CREATE TABLE PEER_CREDIT(
  ID BIGINT(20) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  BRAND_ID BIGINT NOT NULL,
  RECEIVER_ID BIGINT NOT NULL,
  GIVER_ID BIGINT NOT NULL,
  AMOUNT INT NOT NULL,
  REASON VARCHAR(254) NOT NULL,
  CREATED DATETIME NOT NULL);

ALTER TABLE BRAND_SETTINGS ADD COLUMN CREDITS TINYINT(1) DEFAULT 0;
ALTER TABLE BRAND_SETTINGS ADD COLUMN CREDIT_LIMIT INT DEFAULT 100;
ALTER TABLE FACILITATOR ADD COLUMN CREDITS_GIVEN INT DEFAULT 0;
ALTER TABLE FACILITATOR ADD COLUMN CREDITS_RECEIVED INT DEFAULT 0;
ALTER TABLE USER_ACCOUNT ADD COLUMN CREDITS INT DEFAULT NULL;

# --- !Downs
ALTER TABLE USER_ACCOUNT DROP COLUMN CREDITS;
DELETE FROM NOTIFICATION WHERE TYPE = 'credit-received';
ALTER TABLE FACILITATOR DROP COLUMN CREDITS_GIVEN;
ALTER TABLE FACILITATOR DROP COLUMN CREDITS_RECEIVED;
ALTER TABLE BRAND_SETTINGS DROP COLUMN CREDIT_LIMIT;
ALTER TABLE BRAND_SETTINGS DROP COLUMN CREDITS;
DROP TABLE PEER_CREDIT;