create table subject (id integer PRIMARY KEY autoincrement, name text, message text, average real, unique (name));
create table score (id integer PRIMARY KEY autoincrement, subjectId integer, value integer, weight integer, foreign key(subjectId) references subject(id));
insert into subject(name, message, average) values ('Anglický jazyk', 'Well...', 4.1);