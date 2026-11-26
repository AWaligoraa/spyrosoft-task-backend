CREATE TABLE charger (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(200) NOT NULL
);

INSERT INTO charger (name, location) VALUES
    ('CP-01', 'Canary Wharf, London'),
    ('CP-02', 'MediaCity, Salford'),
    ('CP-03', 'Temple Meads, Bristol');
