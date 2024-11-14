CREATE OR REPLACE FUNCTION calculate_total_health()
    RETURNS BIGINT AS $$
DECLARE
    total_health BIGINT;
BEGIN
    SELECT COALESCE(SUM(health), 0) INTO total_health FROM SpaceMarine;
    RETURN total_health;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION get_space_marines_with_health_less_than(max_health BIGINT)
    RETURNS SETOF SpaceMarine AS $$
BEGIN
    RETURN QUERY
        SELECT sm.*
        FROM SpaceMarine sm
        WHERE sm.health < max_health;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION get_unique_loyal_values()
    RETURNS TABLE(loyal BOOLEAN) AS $$
BEGIN
    RETURN QUERY SELECT DISTINCT sm.loyal FROM SpaceMarine sm;
END;
$$ LANGUAGE plpgsql;
