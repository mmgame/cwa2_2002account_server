package com.cwa.server.account.dataFunction;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import baseice.basedao.IEntity;

import com.cwa.component.data.function.IDataFunction;
import com.cwa.data.entity.IRegionEntityDao;
import com.cwa.data.entity.domain.RegionEntity;
import com.cwa.server.account.context.IAccountContext;
import com.cwa.service.constant.ServiceConstant;

public class RegionDataFunction implements IDataFunction {
	private IAccountContext context;

	private IRegionEntityDao dao;

	// 可用区id
	private Set<Integer> availableRids = new HashSet<Integer>();

	private boolean isInited;

	public RegionDataFunction(IAccountContext context) {
		this.context = context;
		dao = (IRegionEntityDao) context.getDataFunctionManager().getDbSession().getEntityDao(RegionEntity.class);
	}

	@Override
	public boolean initData(boolean newRegister) {
		if (isInited) {
			return false;
		}
		isInited = true;

		List<? extends IEntity> entitys = dao.selectAllEntity(createParams());
		if (entitys == null) {
			throw new RuntimeException("RegionEntity list is null!");
		}
		for (IEntity e : entitys) {
			RegionEntity entity = (RegionEntity) e;
			availableRids.add(entity.rid);
		}
		return false;
	}

	@Override
	public boolean isInited() {
		return isInited;
	}

	public boolean checkRid(int rid) {
		return availableRids.contains(rid);
	}

	public void newRegion(int rid) {
		availableRids.add(rid);
	}

	private Map<String, Object> createParams() {
		return context.getDataFunctionManager().getDbSession().getParams(ServiceConstant.General_Rid);
	}
}
