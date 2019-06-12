create schema if not exists jcspider collate utf8mb4_general_ci;

create table if not exists project
(
    id bigint auto_increment
        primary key,
    name varchar(50) not null,
    start_url varchar(1000) not null,
    script_text text not null,
    status varchar(10) not null,
    rate_unit varchar(10) not null,
    rate_number int default 1 not null,
    dispatcher varchar(20) not null,
    created_at timestamp default CURRENT_TIMESTAMP not null,
    updated_at timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    schedule_type varchar(10) not null,
    schedule_value bigint default 0 not null
);

create table if not exists project_process_node
(
    id bigint auto_increment
        primary key,
    project_id bigint not null,
    process_node varchar(20) not null,
    created_at timestamp default CURRENT_TIMESTAMP not null,
    constraint uniq_project_id_process_node
        unique (project_id, process_node)
);

create table if not exists result
(
    id bigint auto_increment
        primary key,
    project_id bigint not null,
    task_id varchar(32) not null,
    result_text text not null,
    created_at timestamp default CURRENT_TIMESTAMP not null
);

create table if not exists task
(
    id varchar(32) not null
        primary key,
    status varchar(10) not null,
    method varchar(64) not null,
    source_url varchar(1000) not null,
    schedule_type varchar(10) not null,
    stack text null,
    project_id bigint not null,
    schedule_value int not null,
    headers varchar(500) null,
    extra varchar(500) null,
    fetch_type varchar(10) not null,
    proxy varchar(64) null,
    created_at timestamp default CURRENT_TIMESTAMP not null,
    updated_at timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    charset varchar(10) null
);

