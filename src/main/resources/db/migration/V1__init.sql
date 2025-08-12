create table if not exists sla_instance (
    task_id varchar(100) primary key,
    priority varchar(10),
    first_response_deadline timestamptz,
    first_response_at timestamptz,
    status varchar(20),
    updated_at timestamptz
);

create table if not exists sla_action_log (
    id bigserial primary key,
    task_id varchar(100),
    action varchar(50),
    reason varchar(50),
    ts timestamptz
);
