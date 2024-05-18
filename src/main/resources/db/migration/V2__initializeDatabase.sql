CREATE TABLE account
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    email       VARCHAR(255)          NOT NULL,
    first_name  VARCHAR(255)          NOT NULL,
    last_name   VARCHAR(255)          NULL,
    avatar_link VARCHAR(255)          NULL,
    `role`      VARCHAR(20)           NOT NULL,
    status      VARCHAR(20)           NOT NULL,
    CONSTRAINT pk_account PRIMARY KEY (id)
);

CREATE TABLE address
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    house_number     VARCHAR(255)          NOT NULL,
    street_name      VARCHAR(255)          NOT NULL,
    ward_id          BIGINT                NOT NULL,
    district_id      BIGINT                NOT NULL,
    city_province_id BIGINT                NOT NULL,
    `description`    TEXT                  NULL,
    institution_id   BIGINT                NULL,
    CONSTRAINT pk_address PRIMARY KEY (id)
);

CREATE TABLE admission_major
(
    id                            BIGINT AUTO_INCREMENT NOT NULL,
    name                          VARCHAR(255)          NULL,
    `description`                 TEXT                  NULL,
    quota                         INT                   NOT NULL,
    admission_plan_id             BIGINT                NOT NULL,
    major_id                      BIGINT                NULL,
    admission_training_program_id BIGINT                NOT NULL,
    CONSTRAINT pk_admission_major PRIMARY KEY (id)
);

CREATE TABLE admission_major_method
(
    id                  BIGINT AUTO_INCREMENT NOT NULL,
    name                VARCHAR(255)          NULL,
    admission_major_id  BIGINT                NOT NULL,
    admission_method_id BIGINT                NULL,
    CONSTRAINT pk_admission_major_method PRIMARY KEY (id)
);

CREATE TABLE admission_major_method_subject_group
(
    admission_major_method_id BIGINT NOT NULL,
    subject_group_id          BIGINT NOT NULL,
    CONSTRAINT pk_admission_major_method_subject_group PRIMARY KEY (admission_major_method_id, subject_group_id)
);

CREATE TABLE admission_method
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    name          VARCHAR(255)          NOT NULL,
    code          VARCHAR(20)           NOT NULL,
    `description` TEXT                  NULL,
    CONSTRAINT pk_admission_method PRIMARY KEY (id)
);

CREATE TABLE admission_plan
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    name           VARCHAR(255)          NOT NULL,
    `description`  TEXT                  NULL,
    year           INT                   NOT NULL,
    institution_id BIGINT                NOT NULL,
    CONSTRAINT pk_admission_plan PRIMARY KEY (id)
);

CREATE TABLE admission_training_program
(
    id                  BIGINT AUTO_INCREMENT NOT NULL,
    name                VARCHAR(255)          NULL,
    training_program_id BIGINT                NOT NULL,
    admission_plan_id   BIGINT                NOT NULL,
    CONSTRAINT pk_admission_training_program PRIMARY KEY (id)
);

CREATE TABLE certificate
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    name          VARCHAR(255)          NOT NULL,
    `description` TEXT                  NULL,
    link          VARCHAR(255)          NOT NULL,
    student_id    BIGINT                NOT NULL,
    CONSTRAINT pk_certificate PRIMARY KEY (id)
);

CREATE TABLE city_province
(
    id   BIGINT AUTO_INCREMENT NOT NULL,
    name VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_city_province PRIMARY KEY (id)
);

CREATE TABLE department
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    name          VARCHAR(255)          NOT NULL,
    code          VARCHAR(255)          NOT NULL,
    `description` TEXT                  NULL,
    school_id     BIGINT                NOT NULL,
    CONSTRAINT pk_department PRIMARY KEY (id)
);

CREATE TABLE district
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    name             VARCHAR(255)          NOT NULL,
    city_province_id BIGINT                NOT NULL,
    CONSTRAINT pk_district PRIMARY KEY (id)
);

CREATE TABLE high_school
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    name             VARCHAR(255)          NOT NULL,
    `description`    TEXT                  NULL,
    city_province_id BIGINT                NOT NULL,
    CONSTRAINT pk_high_school PRIMARY KEY (id)
);

CREATE TABLE institution
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    name            VARCHAR(255)          NOT NULL,
    code            VARCHAR(20)           NOT NULL,
    `description`   TEXT                  NULL,
    avatar_link     VARCHAR(255)          NULL,
    website_1       VARCHAR(255)          NULL,
    website_title_1 VARCHAR(255)          NULL,
    website_2       VARCHAR(255)          NULL,
    website_title_2 VARCHAR(255)          NULL,
    website_3       VARCHAR(255)          NULL,
    website_title_3 VARCHAR(255)          NULL,
    email_1         VARCHAR(255)          NULL,
    email_title_1   VARCHAR(255)          NULL,
    email_2         VARCHAR(255)          NULL,
    email_title_2   VARCHAR(255)          NULL,
    email_3         VARCHAR(255)          NULL,
    email_title_3   VARCHAR(255)          NULL,
    phone_1         VARCHAR(255)          NULL,
    phone_title_1   VARCHAR(255)          NULL,
    phone_2         VARCHAR(255)          NULL,
    phone_title_2   VARCHAR(255)          NULL,
    phone_3         VARCHAR(255)          NULL,
    phone_title_3   VARCHAR(255)          NULL,
    CONSTRAINT pk_institution PRIMARY KEY (id)
);

CREATE TABLE login
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    password   VARCHAR(255)          NULL,
    method     VARCHAR(20)           NOT NULL,
    account_id BIGINT                NOT NULL,
    CONSTRAINT pk_login PRIMARY KEY (id)
);

CREATE TABLE major
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    name          VARCHAR(255)          NOT NULL,
    code          VARCHAR(255)          NOT NULL,
    `description` TEXT                  NULL,
    department_id BIGINT                NOT NULL,
    CONSTRAINT pk_major PRIMARY KEY (id)
);

CREATE TABLE school
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    name          VARCHAR(255)          NOT NULL,
    code          VARCHAR(255)          NOT NULL,
    `description` TEXT                  NULL,
    CONSTRAINT pk_school PRIMARY KEY (id)
);

CREATE TABLE student
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    birth_date     date                  NULL,
    phone          VARCHAR(20)           NULL,
    high_school_id BIGINT                NULL,
    account_id     BIGINT                NOT NULL,
    CONSTRAINT pk_student PRIMARY KEY (id)
);

CREATE TABLE student_record
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    student_id BIGINT                NOT NULL,
    subject_id BIGINT                NOT NULL,
    mark       DECIMAL(4, 2)         NOT NULL,
    CONSTRAINT pk_student_record PRIMARY KEY (id)
);

CREATE TABLE subject
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    name          VARCHAR(255)          NOT NULL,
    `description` TEXT                  NULL,
    CONSTRAINT pk_subject PRIMARY KEY (id)
);

CREATE TABLE subject_group
(
    id   BIGINT AUTO_INCREMENT NOT NULL,
    code VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_subject_group PRIMARY KEY (id)
);

CREATE TABLE subject_group_subject
(
    subject_group_id BIGINT NOT NULL,
    subject_id       BIGINT NOT NULL,
    CONSTRAINT pk_subject_group_subject PRIMARY KEY (subject_group_id, subject_id)
);

CREATE TABLE training_program
(
    id   BIGINT AUTO_INCREMENT NOT NULL,
    name VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_training_program PRIMARY KEY (id)
);

CREATE TABLE ward
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    name        VARCHAR(255)          NOT NULL,
    district_id BIGINT                NOT NULL,
    CONSTRAINT pk_ward PRIMARY KEY (id)
);

ALTER TABLE account
    ADD CONSTRAINT uc_account_email UNIQUE (email);

ALTER TABLE admission_method
    ADD CONSTRAINT uc_admission_method_code UNIQUE (code);

ALTER TABLE city_province
    ADD CONSTRAINT uc_city_province_name UNIQUE (name);

ALTER TABLE department
    ADD CONSTRAINT uc_department_code UNIQUE (code);

ALTER TABLE district
    ADD CONSTRAINT uc_district_city_province UNIQUE (name, city_province_id);

ALTER TABLE login
    ADD CONSTRAINT uc_login_account UNIQUE (account_id);

ALTER TABLE login
    ADD CONSTRAINT uc_login_password UNIQUE (password);

ALTER TABLE major
    ADD CONSTRAINT uc_major_code UNIQUE (code);

ALTER TABLE school
    ADD CONSTRAINT uc_school_code UNIQUE (code);

ALTER TABLE student
    ADD CONSTRAINT uc_student_account UNIQUE (account_id);

ALTER TABLE student_record
    ADD CONSTRAINT uc_student_record UNIQUE (student_id, subject_id);

ALTER TABLE subject_group_subject
    ADD CONSTRAINT uc_subject_group_subject UNIQUE (subject_group_id, subject_id);

ALTER TABLE subject
    ADD CONSTRAINT uc_subject_name UNIQUE (name);

ALTER TABLE ward
    ADD CONSTRAINT uc_ward_district UNIQUE (name, district_id);

ALTER TABLE address
    ADD CONSTRAINT FK_ADDRESS_ON_CITY_PROVINCE FOREIGN KEY (city_province_id) REFERENCES city_province (id);

ALTER TABLE address
    ADD CONSTRAINT FK_ADDRESS_ON_DISTRICT FOREIGN KEY (district_id) REFERENCES district (id);

ALTER TABLE address
    ADD CONSTRAINT FK_ADDRESS_ON_INSTITUTION FOREIGN KEY (institution_id) REFERENCES institution (id);

ALTER TABLE address
    ADD CONSTRAINT FK_ADDRESS_ON_WARD FOREIGN KEY (ward_id) REFERENCES ward (id);

ALTER TABLE admission_major_method
    ADD CONSTRAINT FK_ADMISSION_MAJOR_METHOD_ON_ADMISSION_MAJOR FOREIGN KEY (admission_major_id) REFERENCES admission_major (id);

ALTER TABLE admission_major_method
    ADD CONSTRAINT FK_ADMISSION_MAJOR_METHOD_ON_ADMISSION_METHOD FOREIGN KEY (admission_method_id) REFERENCES admission_method (id);

ALTER TABLE admission_major
    ADD CONSTRAINT FK_ADMISSION_MAJOR_ON_ADMISSION_PLAN FOREIGN KEY (admission_plan_id) REFERENCES admission_plan (id);

ALTER TABLE admission_major
    ADD CONSTRAINT FK_ADMISSION_MAJOR_ON_ADMISSION_TRAINING_PROGRAM FOREIGN KEY (admission_training_program_id) REFERENCES admission_training_program (id);

ALTER TABLE admission_major
    ADD CONSTRAINT FK_ADMISSION_MAJOR_ON_MAJOR FOREIGN KEY (major_id) REFERENCES major (id);

ALTER TABLE admission_plan
    ADD CONSTRAINT FK_ADMISSION_PLAN_ON_INSTITUTION FOREIGN KEY (institution_id) REFERENCES institution (id);

ALTER TABLE admission_training_program
    ADD CONSTRAINT FK_ADMISSION_TRAINING_PROGRAM_ON_ADMISSION_PLAN FOREIGN KEY (admission_plan_id) REFERENCES admission_plan (id);

ALTER TABLE admission_training_program
    ADD CONSTRAINT FK_ADMISSION_TRAINING_PROGRAM_ON_TRAINING_PROGRAM FOREIGN KEY (training_program_id) REFERENCES training_program (id);

ALTER TABLE certificate
    ADD CONSTRAINT FK_CERTIFICATE_ON_STUDENT FOREIGN KEY (student_id) REFERENCES student (id);

ALTER TABLE department
    ADD CONSTRAINT FK_DEPARTMENT_ON_SCHOOL FOREIGN KEY (school_id) REFERENCES school (id);

ALTER TABLE district
    ADD CONSTRAINT FK_DISTRICT_ON_CITY_PROVINCE FOREIGN KEY (city_province_id) REFERENCES city_province (id);

ALTER TABLE high_school
    ADD CONSTRAINT FK_HIGH_SCHOOL_ON_CITY_PROVINCE FOREIGN KEY (city_province_id) REFERENCES city_province (id);

ALTER TABLE login
    ADD CONSTRAINT FK_LOGIN_ON_ACCOUNT FOREIGN KEY (account_id) REFERENCES account (id);

ALTER TABLE major
    ADD CONSTRAINT FK_MAJOR_ON_DEPARTMENT FOREIGN KEY (department_id) REFERENCES department (id);

ALTER TABLE student
    ADD CONSTRAINT FK_STUDENT_ON_ACCOUNT FOREIGN KEY (account_id) REFERENCES account (id);

ALTER TABLE student
    ADD CONSTRAINT FK_STUDENT_ON_HIGH_SCHOOL FOREIGN KEY (high_school_id) REFERENCES high_school (id);

ALTER TABLE student_record
    ADD CONSTRAINT FK_STUDENT_RECORD_ON_STUDENT FOREIGN KEY (student_id) REFERENCES student (id);

ALTER TABLE student_record
    ADD CONSTRAINT FK_STUDENT_RECORD_ON_SUBJECT FOREIGN KEY (subject_id) REFERENCES subject (id);

ALTER TABLE ward
    ADD CONSTRAINT FK_WARD_ON_DISTRICT FOREIGN KEY (district_id) REFERENCES district (id);

ALTER TABLE admission_major_method_subject_group
    ADD CONSTRAINT fk_admmajmetsubgro_on_admission_major_method FOREIGN KEY (admission_major_method_id) REFERENCES admission_major_method (id);

ALTER TABLE admission_major_method_subject_group
    ADD CONSTRAINT fk_admmajmetsubgro_on_subject_group FOREIGN KEY (subject_group_id) REFERENCES subject_group (id);

ALTER TABLE subject_group_subject
    ADD CONSTRAINT fk_subgrosub_on_subject FOREIGN KEY (subject_id) REFERENCES subject (id);

ALTER TABLE subject_group_subject
    ADD CONSTRAINT fk_subgrosub_on_subject_group FOREIGN KEY (subject_group_id) REFERENCES subject_group (id);