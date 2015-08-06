package com.cwa.server.account.context;

import com.cwa.component.data.IDBSession;
import com.cwa.component.data.function.IDataFunctionManager;
import com.cwa.component.functionmanage.IFunctionService;
import com.cwa.service.context.IGloabalContext;
import com.cwa.util.idcreate.IdCreater;

/**
 * 逻辑服务上下文
 * 
 * @author tzy
 * 
 */
public interface IAccountContext {
	/**
	 * ip地址
	 * 
	 * @return
	 */
	String getAddress();

	/**
	 * 
	 * @return
	 */
	IGloabalContext getGloabalContext();

	/**
	 * 
	 * @param rid
	 * @return
	 */
	IDBSession getDbSession(int rid);

	/**
	 * 
	 * @return
	 */
	IFunctionService getFunctionService();

	/**
	 * 
	 * @return
	 */
	int getGid();

	IDataFunctionManager getDataFunctionManager();

	IdCreater getIdCreater();
}
