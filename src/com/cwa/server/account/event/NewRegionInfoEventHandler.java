package com.cwa.server.account.event;

import serverice.config.NewRegionEvent;
import baseice.event.IEvent;

import com.cwa.component.event.IEventHandler;
import com.cwa.server.account.IAccountService;

/**
 * 新区信息事件相应
 * 
 * @author mausmars
 *
 */
public class NewRegionInfoEventHandler implements IEventHandler {
	private IAccountService service;

	@Override
	public void eventHandler(IEvent event) {
		NewRegionEvent e = new NewRegionEvent();
		service.newRegion(e.rid);
	}

	// --------------------------------------------
	public void setService(IAccountService service) {
		this.service = service;
	}

}
