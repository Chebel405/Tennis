-- Créer la séquence seulement si elle n'existe pas déjà
CREATE SEQUENCE IF NOT EXISTS player_id_seq;

-- Créer la table seulement si elle n'existe pas déjà
CREATE TABLE IF NOT EXISTS player
(
    id integer NOT NULL DEFAULT nextval('player_id_seq'),
    last_name character varying(50) NOT NULL,
    first_name character varying(50) NOT NULL,
    birth_date date NOT NULL,
    points integer NOT NULL,
    rank integer NOT NULL,
    PRIMARY KEY (id)
);

-- Assurez-vous que la séquence est liée à la colonne id de la table player
ALTER SEQUENCE IF EXISTS player_id_seq OWNED BY player.id;

-- Définir le propriétaire de la table player
ALTER TABLE IF EXISTS player OWNER TO postgres;

-- Insérer des données dans la table player, seulement si elles n'existent pas déjà
DO
$$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM player WHERE last_name = 'Nadal' AND first_name = 'Rafael') THEN
        INSERT INTO player (last_name, first_name, birth_date, points, rank)
        VALUES ('Nadal', 'Rafael', '1986-06-03', 5000, 1);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM player WHERE last_name = 'Djokovic' AND first_name = 'Novak') THEN
        INSERT INTO player (last_name, first_name, birth_date, points, rank)
        VALUES ('Djokovic', 'Novak', '1987-05-22', 4000, 2);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM player WHERE last_name = 'Federer' AND first_name = 'Roger') THEN
        INSERT INTO player (last_name, first_name, birth_date, points, rank)
        VALUES ('Federer', 'Roger', '1981-08-08', 3000, 3);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM player WHERE last_name = 'Murray' AND first_name = 'Andy') THEN
        INSERT INTO player (last_name, first_name, birth_date, points, rank)
        VALUES ('Murray', 'Andy', '1987-05-15', 2000, 4);
    END IF;
END

