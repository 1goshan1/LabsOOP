-- User CRUD
INSERT INTO users (login, role, password) VALUES (?, ?::role_enum, ?);
SELECT * FROM users WHERE id = ?;
SELECT * FROM users WHERE login = ?;
UPDATE users SET password = ? WHERE id = ?;
UPDATE users SET role = ?::role_enum WHERE id = ?;
DELETE FROM users WHERE id = ?;

--Functions CRUD
INSERT INTO functions (u_id, name, signature) VALUES (?, ?, ?) RETURNING id;
SELECT * FROM functions WHERE id = ?;
SELECT * FROM functions WHERE u_id = ?;
UPDATE functions SET name = ? WHERE id = ?;
UPDATE functions SET signature = ? WHERE id = ?;
DELETE FROM functions WHERE id = ?;

--Points CRUD
INSERT INTO points (f_id, x_value, y_value) VALUES (?, ?, ?);
SELECT * FROM points WHERE f_id = ?;
UPDATE points SET x_value = ?, y_value = ? WHERE id = ?;
DELETE FROM points WHERE id = ?;
DELETE FROM points WHERE f_id = ?;