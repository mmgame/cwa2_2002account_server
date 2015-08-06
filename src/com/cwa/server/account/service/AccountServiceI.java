package com.cwa.server.account.service;

import serverice.account.AMD_IAccountService_checkLogin;
import serverice.account.AMD_IAccountService_chooseRegion;
import serverice.account.AMD_IAccountService_login;
import serverice.account.AMD_IAccountService_loginOut;
import serverice.account.ChooseRegionResult;
import serverice.account.LoginResult;
import serverice.account._IAccountServiceDisp;
import Ice.Current;

import com.cwa.server.account.IAccountService;

public class AccountServiceI extends _IAccountServiceDisp {
	private static final long serialVersionUID = 1L;

	private IAccountService accountService;

	@Override
	public void login_async(AMD_IAccountService_login __cb, String account, int channel, String password, Current __current) {
		try {
			LoginResult result = accountService.login(account, channel, password);
			__cb.ice_response(result);
		} catch (Exception ex) {
			__cb.ice_exception(ex);
		}
	}

	@Override
	public void checkLogin_async(AMD_IAccountService_checkLogin __cb, String token, int rid, long userId, Current __current) {
		try {
			long result = accountService.checkLogin(token, rid, userId);
			__cb.ice_response(result);
		} catch (Exception ex) {
			__cb.ice_exception(ex);
		}
	}

	@Override
	public void loginOut_async(AMD_IAccountService_loginOut __cb, long userId, Current __current) {
		try {
			accountService.loginOut(userId);
			__cb.ice_response();
		} catch (Exception ex) {
			__cb.ice_exception(ex);
		}
	}

	@Override
	public void chooseRegion_async(AMD_IAccountService_chooseRegion __cb, String token, int rid, Current __current) {
		try {
			ChooseRegionResult result = accountService.chooseRegion(token, rid);
			__cb.ice_response(result);
		} catch (Exception ex) {
			__cb.ice_exception(ex);
		}
	}

	// --------------------------
	public void setAccountService(IAccountService accountService) {
		this.accountService = accountService;
	}
}
