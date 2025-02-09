DROP ALIAS IF EXISTS SP_GOODACTION_UPDATE;
CREATE ALIAS SP_GOODACTION_UPDATE FOR "qaservice.DBServer.database.storedProcedure.GoodActionProcedure.updateGoodActMapCasheAndTable";
DROP ALIAS IF EXISTS SP_UPDATE_USER_INTRODUCTION;
CREATE ALIAS SP_UPDATE_USER_INTRODUCTION FOR "qaservice.DBServer.database.storedProcedure.userInfo.UserInfoUpdateProcedure.updateUserIntroduction";
DROP ALIAS IF EXISTS SP_UPDATE_USER_PICTUREDATA;
CREATE ALIAS SP_UPDATE_USER_PICTUREDATA FOR "qaservice.DBServer.database.storedProcedure.userInfo.UserInfoUpdateProcedure.updateUserPictureData";
DROP ALIAS IF EXISTS SP_GET_USER_PICTUREDATA;
CREATE ALIAS SP_GET_USER_PICTUREDATA FOR "qaservice.DBServer.database.storedProcedure.userInfo.UserInfoGetProcedure.getUserPictureData";