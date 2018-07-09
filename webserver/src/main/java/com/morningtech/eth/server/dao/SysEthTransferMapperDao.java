package com.morningtech.eth.server.dao;


import com.morningtech.eth.server.entity.SysEthTransfer;
import org.apache.poi.ss.formula.functions.T;

import java.io.Serializable;

public interface SysEthTransferMapperDao extends BaseDao<SysEthTransfer,Integer> {

    int insert(String statement, Object obj) throws Exception;
}