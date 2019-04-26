CREATE TABLE chats
(
    id      int         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    chat_id varchar(15) NOT NULL,
    UNIQUE (chat_id)
);