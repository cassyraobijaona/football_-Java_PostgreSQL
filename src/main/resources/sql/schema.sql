CREATE TYPE position_enum AS ENUM ('GK', 'DEF', 'MIDF', 'STR');
CREATE TYPE continent_enum AS ENUM ('AFRICA', 'EUROPA', 'ASIA', 'AMERICA');


CREATE TABLE Team (
                      id SERIAL CONSTRAINT team_pkey PRIMARY KEY,
                      name VARCHAR(100) NOT NULL,
                      continent continent_enum NOT NULL
);

CREATE TABLE Player (
                        id SERIAL CONSTRAINT player_key PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        age INT NOT NULL,
                        position position_enum NOT NULL,
                        id_team INT NULL,
                        CONSTRAINT fk_team FOREIGN KEY(id_team) REFERENCES Team(id) ON DELETE SET NULL
);
