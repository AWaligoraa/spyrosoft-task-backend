ALTER TABLE charger ADD COLUMN city VARCHAR(100);

UPDATE charger SET location = 'Canary Wharf', city = 'London' WHERE name = 'CP-01';
UPDATE charger SET location = 'MediaCity', city = 'Salford' WHERE name = 'CP-02';
UPDATE charger SET location = 'Temple Meads', city = 'Bristol' WHERE name = 'CP-03';

ALTER TABLE charger ALTER COLUMN city SET NOT NULL;

INSERT INTO charger (name, location, city) VALUES
    ('CP-04', 'Greenwich', 'London'),
    ('CP-05', 'King''s Cross', 'London');
