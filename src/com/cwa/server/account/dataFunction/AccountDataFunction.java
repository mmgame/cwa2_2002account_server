package com.cwa.server.account.dataFunction;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.cwa.component.data.function.IDataFunction;
import com.cwa.data.entity.IAccountEntityDao;
import com.cwa.data.entity.domain.AccountEntity;
import com.cwa.server.account.context.IAccountContext;
import com.cwa.service.constant.ServiceConstant;

public class AccountDataFunction implements IDataFunction {
	private IAccountContext context;

	private IAccountEntityDao dao;

	private boolean isInited;

	public AccountDataFunction(IAccountContext context) {
		this.context = context;
		dao = (IAccountEntityDao) context.getDataFunctionManager().getDbSession().getEntityDao(AccountEntity.class);
	}

	@Override
	public boolean isInited() {
		return isInited;
	}

	@Override
	public boolean initData(boolean newRegister) {
		return false;
	}

	public AccountEntity getEntity(String account, int channel) {
		return dao.selectEntityByAccountAndChannel(account, channel, createParams());
	}

	public AccountEntity createEntity(long accountId, String account, String password, int channel) {
		AccountEntity entity = new AccountEntity();
		entity.accountId = accountId;
		entity.account = account;
		entity.password = password;
		entity.channel = channel;
		entity.createTime = System.currentTimeMillis();
		entity.rids = "";
		// 插入账号
		insertEntity(entity);

		return entity;
	}

	public long getUserId(AccountEntity entity, TokenBean tokenBean, int rid) {
		List<Long> userIds = entity.getRidsMap().get(String.valueOf(rid));

		long userId = 0;
		if (userIds == null || userIds.isEmpty()) {
			// 当前用户在rid区没有账号，这时才可以新建账号
			userIds = new LinkedList<Long>();

			userId = context.getIdCreater().getUserId(tokenBean.getAccountId(), rid);
			userIds.add(userId);
			updateEntity(entity);
		} else if (!userIds.contains(userId)) {
			// 用户没有该账号
			if (userId <= 0) {
				userId = context.getIdCreater().getUserId(tokenBean.getAccountId(), rid);
				userIds.add(userId);

				updateEntity(entity);
			} else {
				// 获取本区第一个账号
				userId = userIds.get(0);
			}
		}
		return userId;
	}

	private void updateEntity(AccountEntity entity) {
		dao.updateEntity(entity, context.getDataFunctionManager().getDbSession().getParams(ServiceConstant.General_Rid));
	}

	private void insertEntity(AccountEntity entity) {
		dao.insertEntity(entity, context.getDataFunctionManager().getDbSession().getParams(ServiceConstant.General_Rid));
	}

	private Map<String, Object> createParams() {
		return context.getDataFunctionManager().getDbSession().getParams(ServiceConstant.General_Rid);
	}
}
