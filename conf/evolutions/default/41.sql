# --- !Ups

create table EVENT (
  ID BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  BRAND_ID BIGINT NOT NULL,
  TITLE VARCHAR(254) NOT NULL,
  SPOKEN_LANGUAGE VARCHAR(254) NOT NULL,
  MATERIALS_LANGUAGE VARCHAR(254),
  CITY VARCHAR(254) NOT NULL,
  COUNTRY_CODE VARCHAR(254) NOT NULL,
  DESCRIPTION TEXT,
  SPECIAL_ATTENTION TEXT,
  START_DATE DATE NOT NULL,
  END_DATE DATE NOT NULL,
  HOURS_PER_DAY SMALLINT NOT NULL,
  WEBSITE VARCHAR(254),
  REGISTRATION_PAGE VARCHAR(254),
  IS_PRIVATE BOOLEAN NOT NULL DEFAULT 0,
  IS_ARCHIVED BOOLEAN NOT NULL DEFAULT 0,
  CREATED TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CREATED_BY VARCHAR(254) NOT NULL DEFAULT 'Sergey Kotlov',
  UPDATED TIMESTAMP NOT NULL,
  UPDATED_BY VARCHAR(254) NOT NULL DEFAULT 'Sergey Kotlov'
);

alter table EVENT add constraint EVENT_BRAND_FK foreign key(BRAND_ID) references BRAND(ID) on update NO ACTION on delete NO ACTION;

# --- !Downs

drop table EVENT;