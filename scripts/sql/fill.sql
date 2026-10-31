INSERT INTO users (login, salt_hex, password_hash_hex) VALUES 
('alice', 'df72903c72cccf1b259fab6274ae8b5d', '4b6b1a66e71e4489498eef4375dd4eff1ed6e809db10da98a38629a19051d5cd'),
('bob', '4908214be0f7448a01072a1d2aa6ed5d', '82d7485d1e84936b193fd9ed6e324698597acdfce7a16b6ca0a2597c286b4d64'),
('carol', 'fbe50e9e4ed358e57e73a7d64eae36ad', '1b9d94c334c74569fb927f5c3ee7937f302b7a1e7f03558a6757a98e98ae7751');

INSERT INTO resources (name, parent_path, max_volume) VALUES 
('A', '', 100),
('B', 'A', 50),
('C', 'A.B', 25),
('D', 'A', 10),
('E', '', 30);
