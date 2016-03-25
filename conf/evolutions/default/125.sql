# --- !Ups
CREATE TABLE API_CONFIG(
  ID BIGINT(20) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  BRAND_ID BIGINT NOT NULL,
  TOKEN CHAR(64) NOT NULL,
  WRITE_CALLS TINYINT(1) NOT NULL DEFAULT 0,
  ACTIVE TINYINT(1) NOT NULL DEFAULT 0,
  EVENTS VARCHAR(254),
  EVENT VARCHAR(254),
  FACILITATORS VARCHAR(254),
  FACILITATOR VARCHAR(254),
  GENERAL_EVALUATION_FORM VARCHAR(254),
  SPECIFIC_EVENT_EVALUATION_FORM VARCHAR(254)
);
ALTER TABLE API_CONFIG ADD CONSTRAINT API_CONFIG_FK FOREIGN KEY (BRAND_ID) references BRAND(ID) on update NO ACTION on delete CASCADE;

# --- !Downs
DROP TABLE API_CONFIG;