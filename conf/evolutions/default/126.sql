# --- !Ups
ALTER TABLE FACILITATOR ADD COLUMN POST_EVENT_TEMPLATE TEXT;
ALTER TABLE EVENT ADD COLUMN POST_EVENT_TEMPLATE TEXT;

# --- !Downs
ALTER TABLE FACILITATOR DROP COLUMN POST_EVENT_TEMPLATE;
ALTER TABLE FACILITATOR DROP COLUMN POST_EVENT_TEMPLATE;