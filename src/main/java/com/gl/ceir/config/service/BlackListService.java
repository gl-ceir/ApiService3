package com.gl.ceir.config.service;

import com.gl.ceir.config.model.app.BlackList;
import com.gl.ceir.config.model.app.ImeiMsisdnIdentity;

public interface BlackListService extends RestServices<BlackList> {
	public BlackList getByMsisdn(Long msisdn);

	public BlackList getByImei(Long imei);

	public BlackList getByMsisdnAndImei(ImeiMsisdnIdentity imeiMsisdnIdentity);

	public void deleteByMsisdnAndImei(ImeiMsisdnIdentity imeiMsisdnIdentity);
}
