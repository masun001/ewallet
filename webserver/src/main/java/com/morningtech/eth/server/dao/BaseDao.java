package com.morningtech.eth.server.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: BaseDao
 * @Package com.hucheng.dao
 * @Description: TODO
 * @date 2017/11/28 15:33
 */
public interface BaseDao<T extends Serializable , ID extends Serializable> {


    int delete(ID id) throws Exception;

    int insert(T record) throws Exception;

    int insertSelective(T record) throws Exception;

    T find(ID id) throws Exception;

    T find(String statement, T t) throws Exception;

    Map find(String statement, Map t) throws Exception;

    <M,R> R find(String statement, M t) throws Exception;

    List<T> findAll() throws Exception;

    List<T> findAll(String statement, T t) throws Exception;

    <M,R> List<R> findAll(String statement, M t) throws Exception;

    int updateSelective(T record) throws Exception;

    int update(T record) throws Exception;

    int update(String statement, T record) throws Exception;

    void update(String statement, List<T> t) throws Exception;

}
