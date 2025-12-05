INSERT INTO application (name_application)
VALUES ('toxic-bet')
    ON CONFLICT (name_application) DO NOTHING;

INSERT INTO application (name_application)
VALUES ('pata-amiga')
    ON CONFLICT (name_application) DO NOTHING;

INSERT INTO application (name_application)
VALUES ('smbuilder')
    ON CONFLICT (name_application) DO NOTHING;