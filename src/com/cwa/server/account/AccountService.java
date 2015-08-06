package com.cwa.server.account;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import serverice.account.AccountStateEnum;
import serverice.account.ChooseRegionResult;
import serverice.account.LoginResult;
import serverice.account.StateEnum;
import serverice.logic.ILogicServicePrx;
import baseice.constant.ErrorValue;
import baseice.service.FunctionAddress;
import baseice.service.FunctionMenu;
import baseice.service.FunctionTypeEnum;

import com.cwa.component.data.IDBService;
import com.cwa.component.data.IDBSession;
import com.cwa.component.data.function.IDataFunctionManager;
import com.cwa.component.functionmanage.IFunctionCluster;
import com.cwa.component.functionmanage.IFunctionService;
import com.cwa.component.functionmanage.node.FunctionLeafNode;
import com.cwa.component.membermanager.IMemberService;
import com.cwa.component.membermanager.MemberTypeEnum;
import com.cwa.component.membermanager.UserMemberData;
import com.cwa.data.entity.domain.AccountEntity;
import com.cwa.data.entity.domain.AccountcountEntity;
import com.cwa.data.entity.domain.RegionEntity;
import com.cwa.server.account.context.IAccountContext;
import com.cwa.server.account.dataFunction.AccountDataFunction;
import com.cwa.server.account.dataFunction.AccountcountDataFunction;
import com.cwa.server.account.dataFunction.RegionDataFunction;
import com.cwa.server.account.dataFunction.TokenBean;
import com.cwa.server.account.dataFunction.TokenDataFunction;
import com.cwa.server.account.manager.AccountFunctionManager;
import com.cwa.service.constant.ServiceConstant;
import com.cwa.service.context.IGloabalContext;
import com.cwa.service.init.services.IceService;
import com.cwa.util.idcreate.IdCreater;

public class AccountService implements IAccountService, IAccountContext {
	protected static final Logger logger = LoggerFactory.getLogger(IAccountService.class);

	private int fidLength = 2;
	private int cidLength = 4;
	private int ridLength = 2;

	// 逻辑服ip信息
	private ConcurrentHashMap<Integer, FunctionAddress> functionAddressMap = new ConcurrentHashMap<Integer, FunctionAddress>();

	private String IpTemplate = "%s:%d";

	private String address;

	private IGloabalContext gloabalContext;

	private IdCreater idCreater;

	private AccountFunctionManager functionManager;

	@Override
	public void startup(IGloabalContext gloabalContext) {
		this.gloabalContext = gloabalContext;

		// 初始化数据功能
		functionManager = new AccountFunctionManager();
		functionManager.init(this);
		functionManager.initData();

		// 获取本分ip地址
		IceService service = (IceService) gloabalContext.getCurrentService(ServiceConstant.IceServerKey);
		FunctionMenu functionMenu = service.getFunctionMenu();
		address = String.format(IpTemplate, functionMenu.fa.ip, functionMenu.fa.port);

		// 初始化id生成器
		idCreater = new IdCreater(fidLength, cidLength, ridLength);
		AccountcountDataFunction adFunction = (AccountcountDataFunction) functionManager.getDataFunction(AccountcountEntity.class);
		adFunction.initData(false);
		AccountcountEntity entity = adFunction.getEntity();
		idCreater.reinit(entity.aid, entity.count);

		// 初始化用区
		RegionDataFunction rdFunction = (RegionDataFunction) functionManager.getDataFunction(RegionEntity.class);
		rdFunction.initData(false);
	}

	@Override
	public void shutdown() throws Exception {
	}

	private AccountcountEntity createNewAC() {
		AccountcountDataFunction acdFunction = (AccountcountDataFunction) functionManager.getDataFunction(AccountcountEntity.class);
		acdFunction.createNewEntity();

		AccountcountEntity entity = acdFunction.getEntity();
		idCreater.reinit(entity.aid, entity.count);
		return entity;
	}

	@Override
	public String getAddress() {
		return address;
	}

	@Override
	public IDataFunctionManager getDataFunctionManager() {
		return functionManager;
	}

	@Override
	public IdCreater getIdCreater() {
		return idCreater;
	}

	// 登陆账号（如果没有自动创建并登陆，有就登陆）
	@Override
	public LoginResult login(String account, int channel, String password) {
		try {
			AccountDataFunction adFunction = (AccountDataFunction) functionManager.getDataFunction(AccountEntity.class);
			AccountcountDataFunction acdFunction = (AccountcountDataFunction) functionManager.getDataFunction(AccountcountEntity.class);

			UserMemberData userMemberData = null;

			AccountEntity entity = adFunction.getEntity(account, channel);
			if (entity == null) {
				// 新账户
				// 创建id
				long[] ids = idCreater.createAccountId();
				if (ids == null) {
					synchronized (this) {
						createNewAC();
					}
					ids = idCreater.createAccountId();
				}
				entity = adFunction.createEntity(ids[0], account, password, channel);
				acdFunction.modifyCount(ids[1]);
			} else {
				if (!entity.password.equals(password)) {
					LoginResult result = new LoginResult();
					result.state = StateEnum.PasswordError;
					result.accountId = entity.accountId;
					return result;
				}
				IMemberService memberService = gloabalContext.getCurrentMemberService();
				if (memberService == null) {
					return null;
				}
				userMemberData = (UserMemberData) memberService.selectMemberData(MemberTypeEnum.MT_User.value(), entity.accountId);
			}
			TokenDataFunction tdFunction = (TokenDataFunction) functionManager.getDataFunction(RegionEntity.class);
			TokenBean tokenBean = tdFunction.createToken(entity);

			LoginResult result = new LoginResult();
			result.state = StateEnum.Success;
			result.accountId = entity.accountId;
			result.rids = entity.rids;
			result.token = tokenBean.getToken();
			if (userMemberData != null) {
				// 如果rid大于-1客户端下一个指令直接进知道区
				tokenBean.setRid(userMemberData.getRid());
				tokenBean.setLsid(userMemberData.getServerIds().get(0));
			}
			result.rid = tokenBean.getRid();
			return result;
		} catch (Exception ex) {
			logger.error("", ex);
		}
		return null;
	}

	@Override
	public ChooseRegionResult chooseRegion(String token, int rid) {
		RegionDataFunction rdFunction = (RegionDataFunction) functionManager.getDataFunction(RegionEntity.class);
		if (!rdFunction.checkRid(rid)) {
			return null;
		}
		TokenDataFunction tdFunction = (TokenDataFunction) functionManager.getDataFunction(RegionEntity.class);
		TokenBean tokenBean = tdFunction.getToken(token);
		if (tokenBean == null) {
			return null;
		}
		FunctionAddress address = getLogicFunctionAddress(tokenBean);
		if (address == null) {
			return null;
		}
		ChooseRegionResult result = new ChooseRegionResult();
		result.state = StateEnum.Success;
		result.ip = address.ip;
		result.port = address.port;
		return result;
	}

	private FunctionAddress getLogicFunctionAddress(TokenBean tokenBean) {
		// 选取逻辑服
		IFunctionService functionService = gloabalContext.getCurrentFunctionService();
		IFunctionCluster functionCluster = functionService.getFunctionCluster(gloabalContext.getGid(), FunctionTypeEnum.Logic);
		if (functionCluster == null) {
			return null;
		}
		FunctionMenu functionMenu = null;
		FunctionAddress address = null;
		if (tokenBean.getRid() > TokenBean.NullRid) {
			// 已经登录的
			functionMenu = functionCluster.getFunctionMenu(tokenBean.getLsid());
			if (functionMenu == null) {
				return null;
			}
			address = functionAddressMap.get(tokenBean.getLsid());
		} else {
			// 随机
			functionMenu = functionCluster.getRandomFunctionMenu(tokenBean.getAccountId());
			if (functionMenu == null) {
				return null;
			}
			address = functionAddressMap.get(functionMenu.fid.fkey);
		}
		if (address == null) {
			// 从逻辑服获取ip
			ILogicServicePrx servicePrx = FunctionLeafNode.getService(functionMenu, ILogicServicePrx.class);
			if (servicePrx == null) {
				return null;
			}
			try {
				address = servicePrx.getAddress();
				functionAddressMap.put(functionMenu.fid.fkey, address);
				tokenBean.setLsid(functionMenu.fid.fkey);
				return address;
			} catch (Exception ex) {
				logger.error("", ex);
			}
			return null;
		} else {
			tokenBean.setLsid(functionMenu.fid.fkey);
			return address;
		}
	}

	@Override
	public long checkLogin(String token, int rid, long userId) {
		RegionDataFunction rdFunction = (RegionDataFunction) functionManager.getDataFunction(RegionEntity.class);
		if (!rdFunction.checkRid(rid)) {
			return ErrorValue.value;
		}
		TokenDataFunction tdFunction = (TokenDataFunction) functionManager.getDataFunction(RegionEntity.class);
		TokenBean tokenBean = tdFunction.removeToken(token);
		if (tokenBean == null) {
			return ErrorValue.value;
		}
		if (tokenBean.getRid() != rid) {
			return ErrorValue.value;
		}
		AccountEntity entity = tokenBean.getEntity();
		if (entity == null) {
			return ErrorValue.value;
		}
		IMemberService memberService = gloabalContext.getCurrentMemberService();
		if (memberService == null) {
			return ErrorValue.value;
		}

		AccountDataFunction adFunction = (AccountDataFunction) functionManager.getDataFunction(AccountEntity.class);
		userId = adFunction.getUserId(entity, tokenBean, rid);

		UserMemberData memberData = new UserMemberData();
		memberData.setUserId(userId);
		memberData.setCreateTime(System.currentTimeMillis());
		memberData.setGroupId(gloabalContext.getGid());
		memberData.addServerIds(tokenBean.getLsid());
		memberData.setState(AccountStateEnum.Online.value());
		memberService.insertMemberData(memberData);
		return userId;
	}

	@Override
	public void loginOut(long userId) {

	}

	@Override
	public IGloabalContext getGloabalContext() {
		return gloabalContext;
	}

	@Override
	public IDBSession getDbSession(int rid) {
		IDBService service = (IDBService) gloabalContext.getCurrentService(ServiceConstant.DatabaseKey);
		if (service == null) {
			return null;
		}
		return service.getDBSession(rid);
	}

	@Override
	public IFunctionService getFunctionService() {
		return gloabalContext.getCurrentFunctionService();
	}

	@Override
	public int getGid() {
		return gloabalContext.getGid();
	}

	@Override
	public void newRegion(int rid) {
		RegionDataFunction rdFunction = (RegionDataFunction) functionManager.getDataFunction(RegionEntity.class);
		if (rdFunction != null) {
			rdFunction.newRegion(rid);
		}
	}

	// -----------------------------------------
	public void setFidLength(int fidLength) {
		this.fidLength = fidLength;
	}

	public void setCidLength(int cidLength) {
		this.cidLength = cidLength;
	}

	public void setRidLength(int ridLength) {
		this.ridLength = ridLength;
	}
}
