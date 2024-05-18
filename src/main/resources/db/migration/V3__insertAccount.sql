INSERT INTO account (email, first_name, last_name, `role`, status)
VALUES ('admin@uniportal.com', 'John', 'Doe', 'ADMIN', 'ACTIVE'),
       ('staff@uniportal.com', 'Jane', 'Doe', 'STAFF', 'ACTIVE'),
       ('student@uniportal.com', 'Bye', 'Doe', 'STUDENT', 'ACTIVE');

INSERT INTO login (method, account_id, password)
VALUES ('DEFAULT', 1, '$2a$10$yMcxlivpe0Q0UBZ0Y0DR4O0.uQognN5rG99XgZGJqHyCqPx4wNXmC'),
       ('DEFAULT', 2, '$2a$10$63crgtsZeoMrnDjrh.WJG.90u5uX3.CxER.fUMJjndQ9Ujx2h1JqW'),
       ('DEFAULT', 3, '$2a$10$nMzZ1XVF/BYU/BUpnWwDu.mE73SFGc.lRuc6jjXa9oDcfot2n0IAK');