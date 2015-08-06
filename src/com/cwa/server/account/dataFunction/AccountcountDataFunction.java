package com.cwa.server.account.dataFunction;

import java.util.List;
import java.util.Map;

import baseice.basedao.IEntity;

import com.cwa.component.data.function.IDataFunction;
import com.cwa.data.entity.IAccountcountEntityDao;
import com.cwa.data.entity.domain.AccountcountEntity;
import com.cwa.data.entity.spread.AccountcountEntityDao;
import com.cwa.server.account.context.IAccountContext;
import com.cwa.service.constant.ServiceConstant;

/**
 * 数据封装
 * 
 * @author mausmars
 *
 */
public class AccountcountDataFunction implements IDataFunction {
	private IAccountContext context;

	private IAccountcountEntityDao dao;

	private volatile AccountcountEntity entity = null;

	private boolean isInited;

	public AccountcountDataFunction(IAccountContext context) {
		this.context = context;
		dao = (AccountcountEntityDao) context.getDataFunctionManager().getDbSession().getEntityDao(AccountcountEntity.class);
	}

	@Override
	public boolean isInited() {
		return isInited;
	}

	@Override
	public boolean initData(boolean newRegister) {
		if (isInited) {
			return false;
		}
		isInited = true;
		List<? extends IEntity> entitys = dao.selectAllEntity(createParams());
		int maxid = 0;
		for (IEntity e : entitys) {
			AccountcountEntity te = (AccountcountEntity) e;
			if (te.aid > maxid) {
				maxid = te.aid;
			}
			if (te.count >= context.getIdCreater().getCidMaxValue()) {
				continue;
			}
			if (te.address.equals(context.getAddress())) {
				entity = te;
			}
		}
		if (entity == null) {
			createNewAccountcount(maxid);
		}
		return false;
	}

	public void createNewEntity() {
		List<AccountcountEntity> entitys = dao.selectAllEntity(createParams());
		int maxid = 0;
		for (AccountcountEntity e : entitys) {
			if (e.aid > maxid) {
				maxid = e.aid;
			}
		}
		createNewAccountcount(maxid);
	}

	public AccountcountEntity getEntity() {
		return entity;
	}

	private void createNewAccountcount(int maxId) {
		entity = new AccountcountEntity();
		entity.aid = ++maxId;
		entity.count = 0;
		entity.address = context.getAddress();
		entity.version = context.getGloabalContext().getConfigVersion();
		insertEntity(entity);
	}

	public void modifyCount(long count) {
		if (count > entity.count) {
			entity.count = count;
			updateEntity(entity);
		}
	}

	private void updateEntity(AccountcountEntity entity) {
		dao.updateEntity(entity, context.getDataFunctionManager().getDbSession().getParams(ServiceConstant.General_Rid));
	}

	private void insertEntity(AccountcountEntity entity) {
		dao.insertEntity(entity, context.getDataFunctionManager().getDbSession().getParams(ServiceConstant.General_Rid));
	}

	private Map<String, Object> createParams() {
		return context.getDataFunctionManager().getDbSession().getParams(ServiceConstant.General_Rid);
	}
}
