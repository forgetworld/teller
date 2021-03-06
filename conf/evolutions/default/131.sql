# --- !Ups
ALTER TABLE USER_ACCOUNT ADD COLUMN SLACK VARCHAR(254) AFTER MAILCHIMP;
CREATE UNIQUE INDEX IDX_SLACK on USER_ACCOUNT (SLACK);

CREATE TABLE SLACK_CHANNEL(
  ID BIGINT PRIMARY KEY AUTO_INCREMENT,
  NAME VARCHAR(254) NOT NULL,
  REMOTE_ID CHAR(32) NOT NULL,
  PUBLIC TINYINT(1) NOT NULL,
  BRAND_ID BIGINT NOT NULL,
  PERSON_ID BIGINT NOT NULL,
  ALL_ATTENDEES TINYINT(1) NOT NULL DEFAULT 1,
  OLD_EVENT_ATTENDEES TINYINT(1) DEFAULT 0
);
ALTER TABLE SLACK_CHANNEL ADD CONSTRAINT SLACK_CHANNEL_BRAND_FK FOREIGN KEY(BRAND_ID) REFERENCES BRAND(ID)
  ON UPDATE NO ACTION ON DELETE CASCADE;
ALTER TABLE SLACK_CHANNEL ADD CONSTRAINT SLACK_CHANNEL_PERSON_FK FOREIGN KEY(PERSON_ID) REFERENCES PERSON(ID)
  ON UPDATE NO ACTION ON DELETE CASCADE;

# --- !Downs
ALTER TABLE SLACK_CHANNEL DROP FOREIGN KEY SLACK_CHANNEL_BRAND_FK;
ALTER TABLE SLACK_CHANNEL DROP FOREIGN KEY SLACK_CHANNEL_PERSON_FK;
DROP TABLE SLACK_CHANNEL;

DROP INDEX IDX_SLACK ON USER_ACCOUNT;
ALTER TABLE USER_ACCOUNT DROP COLUMN SLACK;
