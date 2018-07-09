package com.morningtech.eth.server.dao.impl;


import com.morningtech.eth.server.dao.BaseDao;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: BaseMapper
 * @Package com.hucheng.chinawkb.dao
 * @Description: 主从数据源操作接口
 * @date 2017/11/28 17:14
 */
@Service
public abstract class BaseMapper<T extends Serializable> implements BaseDao<T,Integer> {

    @Resource(name = "sqlSessionMaster")
    protected SqlSessionTemplate writeMapper;

    @Resource(name = "sqlSessionSlave")
    protected SqlSessionTemplate readMapper;

    public static final String SQLNAME_SEPARATOR = ".";

    private String sqlNamespace=getDefaultSqlNamespace();

    protected String getDefaultSqlNamespace() {
      //  Class<T> clazz = ReflectUtils.getClassGenricType(this.getClass());
        return this.getClass().getName();
    }

    protected String sqlName(String sqlName) {
        return sqlNamespace + SQLNAME_SEPARATOR + sqlName;
    }

    public void setSqlNamespace(String sqlNamespace) {
        this.sqlNamespace = sqlNamespace;
    }

    public T find(Integer id) throws Exception{
        return readMapper.selectOne(sqlName("selectByPrimaryKey"),id);
    }

    public T find(String statement,T t) throws Exception{
        return readMapper.selectOne(sqlName(statement),t);
    }

    public Map find(String statement, Map t) throws Exception {
        return readMapper.selectOne(sqlName(statement),t);
    }

    public <M, R> R find(String statement, M t) throws Exception {
        return readMapper.selectOne(sqlName(statement),t);
    }

    public List<T> findAll() throws Exception{
        return readMapper.selectList(sqlName("selectList"));
    }

    public List<T> findAll(String statement, T t) throws Exception{
        return readMapper.selectList(sqlName(statement),t);
    }

    public <M, R> List<R> findAll(String statement, M t) throws Exception {
        return readMapper.selectList(sqlName(statement),t);
    }

    public long count(String statement, T t) throws Exception{
        Map<String,Object> resultMap=  readMapper.selectOne(sqlName(statement),t);
        if(resultMap!=null && resultMap.get("counts")!=null && (Long)resultMap.get("counts")>0){
            return (Long)resultMap.get("counts");
        }
        return 0;
    }

    public int delete(Integer id) throws Exception{
        return writeMapper.delete(sqlName("deleteByPrimaryKey"),id);
    }

    public int delete(String statement,T t) throws Exception{
        return writeMapper.delete(sqlName(statement),t);
    }

    public int insert(T t) throws Exception{
        return writeMapper.insert(sqlName("insert"), t);
    }

    public int update(T t) throws Exception{
        return writeMapper.update(sqlName("updateByPrimaryKey"),t);
    }

    public int update(String statement,T t) throws Exception{
        return writeMapper.update(sqlName(statement),t);
    }

    public void update(String statement,List<T> t) throws Exception{
        writeMapper.update(sqlName(statement),t);
    }

    public int insertSelective(T t) throws Exception {
        return writeMapper.insert(sqlName("insertSelective"),t);
    }

    public int updateSelective(T t) throws Exception {
        return writeMapper.update(sqlName("updateByPrimaryKeySelective"),t);
    }

}