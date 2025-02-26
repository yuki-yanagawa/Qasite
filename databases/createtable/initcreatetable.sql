DROP TABLE IF EXISTS TABLELISTMAST;
CREATE TABLE TABLELISTMAST(TABLEID INTEGER NOT NULL, TABLENAME VARCHAR(255) NOT NULL, PRIMARY KEY(TABLEID));
INSERT INTO TABLELISTMAST (TABLEID, TABLENAME) VALUES('9999', 'USERTABLE');
INSERT INTO TABLELISTMAST (TABLEID, TABLENAME) VALUES('9998', 'QUESTIONTABLE');
INSERT INTO TABLELISTMAST (TABLEID, TABLENAME) VALUES('9997', 'ANSWERTABLE');
INSERT INTO TABLELISTMAST (TABLEID, TABLENAME) VALUES('9996', 'QUESTTYPE');
INSERT INTO TABLELISTMAST (TABLEID, TABLENAME) VALUES('9995', 'NUMBERINGTABLE');
DROP TABLE IF EXISTS USERTABLE;
-- CREATE TABLE USERTABLE(USERID INTEGER NOT NULL, USERNAME VARCHAR(255) NOT NULL, USERPASSWORD VARCHAR(255) NOT NULL, MAILADDRESS VARCHAR(255) NOT NULL, USERLEVEL INTEGER NOT NULL, PRIMARY KEY(USERID));
-- INSERT INTO USERTABLE (USERID, USERNAME, USERPASSWORD, MAILADDRESS, USERLEVEL) VALUES('4999', 'UNKOWN', '', 'unknown@test', 0);
-- INSERT INTO USERTABLE (USERID, USERNAME, USERPASSWORD, MAILADDRESS, USERLEVEL) VALUES('4998', 'TestUser1', 'KukjUCdZLJ7ZmuSdum3mDBi3KuP8cjrjWXe+hLw9OWo=', 'testuser1@test', 1);
-- INSERT INTO USERTABLE (USERID, USERNAME, USERPASSWORD, MAILADDRESS, USERLEVEL) VALUES('4997', 'TestUser2', 'Aa4qIZJlCGFgsdevwoaxAjsU2G3m/dlCPrbsXpL1sso=', 'testuser2@test', 2);
CREATE TABLE USERTABLE(USERID INTEGER NOT NULL, USERNAME VARCHAR(255) NOT NULL, USERPASSWORD VARCHAR(255) NOT NULL, MAILADDRESS VARCHAR(255) NOT NULL, USERPOINT INTEGER NOT NULL, PRIMARY KEY(USERID));
-- INSERT INTO USERTABLE (USERID, USERNAME, USERPASSWORD, MAILADDRESS, USERPOINT) VALUES('4998', 'TestUser1', 'dGVzdHVzZXIx', 'testuser1@test', 1);
-- INSERT INTO USERTABLE (USERID, USERNAME, USERPASSWORD, MAILADDRESS, USERPOINT) VALUES('4997', 'TestUser2', 'dGVzdHVzZXIy', 'testuser2@test', 2);
INSERT INTO USERTABLE (USERID, USERNAME, USERPASSWORD, MAILADDRESS, USERPOINT) VALUES('4998', 'TestUser1', 'KukjUCdZLJ7ZmuSdum3mDBi3KuP8cjrjWXe+hLw9OWo=', 'testuser1@test', 1);
INSERT INTO USERTABLE (USERID, USERNAME, USERPASSWORD, MAILADDRESS, USERPOINT) VALUES('4997', 'TestUser2', 'Aa4qIZJlCGFgsdevwoaxAjsU2G3m/dlCPrbsXpL1sso=', 'testuser2@test', 2);
DROP TABLE IF EXISTS USERPICTURETABLE
CREATE TABLE USERPICTURETABLE(USERID INTEGER NOT NULL, PICTUREDATA BLOB NOT NULL, PRIMARY KEY(USERID));
DROP TABLE IF EXISTS USERINTRODUCTIONTABLE;
CREATE TABLE USERINTRODUCTIONTABLE(USERID INTEGER NOT NULL, INTRODUCTION BLOB NOT NULL, PRIMARY KEY(USERID));
DROP TABLE IF EXISTS QUESTIONTABLE;
-- CREATE TABLE QUESTIONTABLE (QUESTIONID INTEGER NOT NULL, QUESTION_TITLE BLOB NOT NULL, QUESTION_DETAIL_DATA BLOB NOT NULL, QUESTTYPE INTEGER, USERID INTEGER NOT NULL, QUESTVALID BOOLEAN NOT NULL, UPDATE_DATE TIMESTAMP, PRIMARY KEY(QUESTIONID));
CREATE TABLE QUESTIONTABLE (QUESTIONID INTEGER NOT NULL, QUESTION_TITLE VARCHAR NOT NULL, QUESTION_DETAIL_DATA VARCHAR NOT NULL, QUESTTYPE INTEGER, USERID INTEGER NOT NULL, QUESTVALID BOOLEAN NOT NULL, UPDATE_DATE TIMESTAMP, PRIMARY KEY(QUESTIONID));
DROP INDEX IF EXISTS QUESTIONTABLE_IDX;
CREATE INDEX QUESTIONTABLE_IDX ON QUESTIONTABLE(QUESTIONID);
DROP TABLE IF EXISTS QUESTIONSUBTABLEIMAGE;
CREATE TABLE QUESTIONSUBTABLEIMAGE (QUESTIONID INTEGER NOT NULL, QUESTION_IMAGEDATA BLOB NOT NULL, PRIMARY KEY(QUESTIONID));
DROP TABLE IF EXISTS QUESTIONSUBTABLELINKFILE;
CREATE TABLE QUESTIONSUBTABLELINKFILE (QUESTIONID INTEGER NOT NULL, QUESTION_LINKFILEID INTEGER NOT NULL, QUESTION_LINKFILENAME BLOB NOT NULL, QUESTION_LINKFILEDATA BLOB NOT NULL, PRIMARY KEY(QUESTIONID, QUESTION_LINKFILEID));
DROP TABLE IF EXISTS ANSWERTABLE;
-- CREATE TABLE ANSWERTABLE (ANSWERID INTEGER NOT NULL, QUESTIONID INTEGER NOT NULL, ANSWER_DETAIL_DATA BLOB NOT NULL, USERID INTEGER NOT NULL, ANSWERVALID BOOLEAN NOT NULL, UPDATE_DATE TIMESTAMP, PRIMARY KEY(ANSWERID));
CREATE TABLE ANSWERTABLE (ANSWERID INTEGER NOT NULL, QUESTIONID INTEGER NOT NULL, ANSWER_DETAIL_DATA VARCHAR NOT NULL, USERID INTEGER NOT NULL, ANSWERVALID BOOLEAN NOT NULL, UPDATE_DATE TIMESTAMP, PRIMARY KEY(ANSWERID));
DROP INDEX IF EXISTS ANSWERTABLE_IDX;
CREATE INDEX ANSWERTABLE_IDX ON ANSWERTABLE(QUESTIONID);
DROP TABLE IF EXISTS ANSWERSUBTABLEIMAGE;
CREATE TABLE ANSWERSUBTABLEIMAGE (ANSWERID INTEGER NOT NULL, ANSWER_IMAGEDATA BLOB NOT NULL, PRIMARY KEY(ANSWERID));
DROP TABLE IF EXISTS ANSWERSUBTABLELINKFILE;
CREATE TABLE ANSWERSUBTABLELINKFILE (ANSWERID INTEGER NOT NULL, ANSWER_LINKFILEID INTEGER NOT NULL, ANSWER_LINKFILENAME BLOB NOT NULL, ANSWER_LINKFILEDATA BLOB NOT NULL, PRIMARY KEY(ANSWERID, ANSWER_LINKFILEID));
-- DROP TABLE IF EXISTS QUESTTYPE;
-- CREATE TABLE QUESTTYPE(TYPEID INTEGER, TYPENAME VARCHAR(255), PRIMARY KEY(TYPEID));
-- INSERT INTO QUESTTYPE (TYPEID, TYPENAME) VALUES('1', 'generalstatistics');
-- INSERT INTO QUESTTYPE (TYPEID, TYPENAME) VALUES('2', 'medicalstatistics');
-- INSERT INTO QUESTTYPE (TYPEID, TYPENAME) VALUES('99', 'others');
DROP TABLE IF EXISTS NUMBERINGTABLE;
CREATE TABLE NUMBERINGTABLE(TABLEID INTEGER NOT NULL, NEXTNUMBER INTEGER NOT NULL, PRIMARY KEY(TABLEID, NEXTNUMBER));
INSERT INTO NUMBERINGTABLE (TABLEID, NEXTNUMBER) VALUES('9999', '5001');
INSERT INTO NUMBERINGTABLE (TABLEID, NEXTNUMBER) VALUES('9998', '2001');
INSERT INTO NUMBERINGTABLE (TABLEID, NEXTNUMBER) VALUES('9997', '2001');
DROP TABLE IF EXISTS ANSWERGOODPOINTTABLE;
CREATE TABLE ANSWERGOODPOINTTABLE(ANSWERID INTEGER NOT NULL, GOOD_ACTION_USER_ID INTEGER NOT NULL, GOOD_ACTION BOOLEAN ,PRIMARY KEY(ANSWERID, GOOD_ACTION_USER_ID));
DROP INDEX IF EXISTS ANSWERGOODPOINTTABLE_IDX;
CREATE INDEX ANSWERGOODPOINTTABLE_IDX ON ANSWERGOODPOINTTABLE(ANSWERID, GOOD_ACTION_USER_ID);
DROP TABLE IF EXISTS ANSWERHELPFULPOINTTABLE;
CREATE TABLE ANSWERHELPFULPOINTTABLE(ANSWERID INTEGER NOT NULL, HELPFUL_ACTION_USER_ID INTEGER NOT NULL, HELPFUL_ACTION BOOLEAN ,PRIMARY KEY(ANSWERID, HELPFUL_ACTION_USER_ID));
DROP INDEX IF EXISTS ANSWERHELPFULPOINTTABLE_IDX;
CREATE INDEX ANSWERHELPFULPOINTTABLE_IDX ON ANSWERHELPFULPOINTTABLE(ANSWERID, HELPFUL_ACTION_USER_ID);