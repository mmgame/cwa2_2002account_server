package com.cwa.server.account.dataFunction;

import com.cwa.data.entity.domain.AccountEntity;
import com.cwa.util.KeyUtil;

public class TokenBean {
	public final static int NullRid = -1;

	private AccountEntity entity; // 用户信息
	private String token; // token
	private long createTime; // 创建时间
	private int rid = NullRid; // 登陆的区
	private int lsid;// 逻辑服id

	public TokenBean(AccountEntity entity) {
		this.entity = entity;
		this.token = KeyUtil.getUUID();
		this.createTime = System.currentTimeMillis();
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public int getRid() {
		return rid;
	}

	public void setRid(int rid) {
		this.rid = rid;
	}

	public long getAccountId() {
		return entity.accountId;
	}

	public AccountEntity getEntity() {
		return entity;
	}

	public int getLsid() {
		return lsid;
	}

	public void setLsid(int lsid) {
		this.lsid = lsid;
	}
}
