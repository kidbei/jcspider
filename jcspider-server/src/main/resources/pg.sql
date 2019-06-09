create schema public;

comment on schema public is 'standard public schema';

alter schema public owner to kidbei;

create table if not exists project
(
	id bigserial not null
		constraint project_pk
			primary key,
	name varchar(50) not null,
	start_url varchar(1000) not null,
	script_text text,
	status varchar(10) not null,
	rate_unit varchar(10) not null,
	rate_number integer default 0 not null,
	dispatcher char(20) default '127.0.0.1'::bpchar not null,
	created_at timestamp,
	updated_at timestamp,
	schedule_type varchar(10),
	schedule_value bigint,
	rate_unit_multiple integer default 0
);

alter table project owner to root;

create table if not exists project_process_node
(
	id bigserial not null
		constraint project_process_node_pk
			primary key,
	project_id bigint not null,
	process_node varchar(32) not null,
	created_at timestamp not null
);

alter table project_process_node owner to root;

create table if not exists result
(
	id bigserial not null
		constraint result_pk
			primary key,
	project_id bigint not null,
	task_id varchar(32) not null,
	result_text text not null,
	created_at timestamp not null
);

alter table result owner to root;

create table if not exists task
(
	id varchar(32) not null
		constraint task_pk
			primary key,
	status varchar(10) not null,
	method varchar(50) not null,
	source_url varchar(1000) not null,
	schedule_type varchar(10) not null,
	stack text,
	project_id bigint not null,
	schedule_value bigint,
	headers varchar(500),
	extra varchar(500),
	fetch_type varchar(10) not null,
	proxy varchar(64),
	charset varchar(10),
	created_at timestamp,
	updated_at timestamp
);

alter table task owner to root;

