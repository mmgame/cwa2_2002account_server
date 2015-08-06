package com.cwa.server.account.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import baseice.basedao.IEntity;

import com.cwa.component.data.IDBSession;
import com.cwa.component.data.function.IDataFunction;
import com.cwa.component.data.function.IDataFunctionManager;
import com.cwa.data.entity.domain.AccountEntity;
import com.cwa.data.entity.domain.AccountcountEntity;
import com.cwa.data.entity.domain.RegionEntity;
import com.cwa.server.account.context.IAccountContext;
import com.cwa.server.account.dataFunction.AccountDataFunction;
import com.cwa.server.account.dataFunction.AccountcountDataFunction;
import com.cwa.server.account.dataFunction.RegionDataFunction;
import com.cwa.server.account.dataFunction.TokenBean;
import com.cwa.server.account.dataFunction.TokenDataFunction;
import com.cwa.service.constant.ServiceConstant;

public class AccountFunctionManager implements IDataFunctionManager {
	// 数据session
	private IDBSession dbSession;
	// 数据功能map
	private Map<String, IDataFunction> dataFunctionMap = new HashMap<String, IDataFunction>();

	public void init(IAccountContext context) {
		// 获得公共区
		dbSession = context.getDbSession(ServiceConstant.General_Rid);

		dataFunctionMap.put(AccountcountEntity.class.getSimpleName(), new AccountcountDataFunction(context));
		dataFunctionMap.put(AccountEntity.class.getSimpleName(), new AccountDataFunction(context));
		dataFunctionMap.put(RegionEntity.class.getSimpleName(), new RegionDataFunction(context));

		dataFunctionMap.put(TokenBean.class.getSimpleName(), new TokenDataFunction(context));
	}

	@Override
	public IDBSession getDbSession() {
		return dbSession;
	}

	@Override
	public IDataFunction getDataFunction(Class<? extends IEntity> cla) {
		return dataFunctionMap.get(cla.getSimpleName());
	}

	@Override
	public void initData() {
		for (Entry<String, IDataFunction> entry : dataFunctionMap.entrySet()) {
			entry.getValue().initData(false);
		}
	}

	@Override
	public void initData(Class<? extends IEntity> cla) {
		IDataFunction dataFunction = dataFunctionMap.get(cla.getSimpleName());
		if (dataFunction != null) {
			dataFunction.initData(false);
		}
	}

	@Override
	public void insertDataTimeout() {
	}

	@Override
	public void removeDataTimeout() {
	}

	@Override
	public void resetDataTimeout() {
	}
}
