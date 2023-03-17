create table sessions (
    id serial,
    session uuid default gen_random_uuid(),
    created TIMESTAMP WITH TIME ZONE default now(),
    expiry TIMESTAMP WITH TIME ZONE  default 'infinity',
    ip inet
);
--;;
create table sessions_params (
    id serial,
    session_id INTEGER,
    name varchar not null,
    num_val double precision,
    str_val varchar
);
--;;
create table worlds_generation_requests (
    id serial,
    created TIMESTAMP WITH TIME ZONE default now(),
    params jsonb,
    routine varchar,
    comment varchar
);