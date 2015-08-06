package com.cwa.server.account;

import com.cwa.service.IModuleServer;

import serverice.account.ChooseRegionResult;
import serverice.account.LoginResult;

public interface IAccountService extends IModuleServer {
	LoginResult login(String account, int channel, String password);

	ChooseRegionResult chooseRegion(String token, int rid);

	long checkLogin(String token, int rid, long userId);

	void loginOut(long userId);

	void newRegion(int rid);
}
