# --- !Ups
CREATE TABLE CREDIT_CARD(
  ID BIGINT(20) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  CUSTOMER_ID BIGINT(20) NOT NULL,
  REMOTE_ID VARCHAR(32) NOT NULL,
  BRAND VARCHAR(30) NOT NULL,
  NUMBER CHAR(4) NOT NULL,
  EXP_MONTH TINYINT NOT NULL,
  EXP_YEAR SMALLINT NOT NULL,
  ACTIVE TINYINT(1) NOT NULL DEFAULT 1,
  CREATED TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE CREDIT_CARD ADD CONSTRAINT CREDIT_CARD_FK FOREIGN KEY (CUSTOMER_ID) references CUSTOMER(ID) on update NO ACTION on delete CASCADE;

ALTER TABLE MEMBER ADD COLUMN NEW_FEE DECIMAL(13,3) DEFAULT NULL AFTER FEE;
ALTER TABLE MEMBER DROP COLUMN FEE_CURRENCY;

# --- !Downs
ALTER TABLE MEMBER ADD COLUMN FEE_CURRENCY CHAR(3) DEFAULT 'EUR';
ALTER TABLE MEMBER DROP COLUMN NEW_FEE;
DROP TABLE CREDIT_CARD;