package com.cwa.server.account.dataFunction;

import java.util.HashMap;
import java.util.Map;

import com.cwa.component.data.function.IDataFunction;
import com.cwa.component.datatimeout.IDataTimeoutCallBlack;
import com.cwa.component.datatimeout.IDataTimeoutManager;
import com.cwa.component.datatimeout.IDataTimeoutService;
import com.cwa.data.entity.domain.AccountEntity;
import com.cwa.server.account.context.IAccountContext;
import com.cwa.service.constant.ServiceConstant;

public class TokenDataFunction implements IDataFunction {
	private IDataTimeoutManager dataTimeoutManager;
	// {token:bean}
	private Map<String, TokenBean> tokenMap = new HashMap<String, TokenBean>();
	// {accountId:bean}
	private Map<Long, TokenBean> accountIdMap = new HashMap<Long, TokenBean>();

	public TokenDataFunction(IAccountContext context) {
		IDataTimeoutService dataTimeoutService = (IDataTimeoutService) context.getGloabalContext().getCurrentService(
				ServiceConstant.DataTimeoutKey);
		if (dataTimeoutService != null) {
			dataTimeoutManager = dataTimeoutService.createTask("data_timeoutcheck_" + context.getGloabalContext().getGid());
		}
	}

	@Override
	public boolean isInited() {
		return false;
	}

	@Override
	public boolean initData(boolean newRegister) {
		return false;
	}

	public TokenBean createToken(AccountEntity entity) {
		TokenBean tokenBean = null;
		synchronized (accountIdMap) {
			tokenBean = accountIdMap.get(entity.accountId);
			if (tokenBean == null) {
				tokenBean = new TokenBean(entity);
				tokenMap.put(tokenBean.getToken(), tokenBean);
				accountIdMap.put(entity.accountId, tokenBean);
			}
		}
		if (dataTimeoutManager != null) {
			// 添加超时数据检查
			dataTimeoutManager.insertTimeoutCheck(tokenBean.getToken(), tokenBean, new IDataTimeoutCallBlack() {
				@Override
				public void callblack(Object obj) {
					TokenBean tokenBean = (TokenBean) obj;
					// token超时
					synchronized (accountIdMap) {
						accountIdMap.remove(tokenBean.getToken());
					}
				}
			});
		}
		return tokenBean;
	}

	public TokenBean removeToken(String token) {
		TokenBean tokenBean = null;
		synchronized (accountIdMap) {
			tokenBean = tokenMap.remove(token);
			if (tokenBean == null) {
				return null;
			}
			accountIdMap.remove(tokenBean.getAccountId());
		}
		if (dataTimeoutManager != null) {
			// 移除超时数据检查
			dataTimeoutManager.removeTimeoutCheck(tokenBean.getToken());
		}
		return tokenBean;
	}

	public synchronized TokenBean getToken(String token) {
		return tokenMap.get(token);
	}
}
