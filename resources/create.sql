create table subject (id integer PRIMARY KEY autoincrement, name text, message text, average real, unique (name));
create table score (id integer PRIMARY KEY autoincrement, subjectId integer, name text, score text, weight real, created date, note text, foreign key(subjectId) references subject(id));
insert into subject(name, message, average) values ('Anglický jazyk', 'Well...', 4.1);
