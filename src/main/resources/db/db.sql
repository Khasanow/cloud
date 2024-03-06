create table files
(
    id          bigserial not null PRIMARY KEY,
    file_name   varchar(255),
    size        integer   not null,
    its_removed boolean   not null,
    user_login  varchar(255)
);

create table users
(
    id       bigserial not null,
    login    varchar(255) unique,
    password varchar(255),
    roles    varchar(255) check (roles in ('USER', 'ADMIN')),
    primary key (id)
);

insert into users(login, password, roles)
VALUES ( 'user', 'user', 'USER'),
       ('admin', 'admin', 'ADMIN')
