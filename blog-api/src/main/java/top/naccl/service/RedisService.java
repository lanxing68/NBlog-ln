package top.naccl.service;

import top.naccl.model.vo.BlogInfo;
import top.naccl.model.vo.PageResult;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface RedisService {
	PageResult<BlogInfo> getBlogInfoPageResultByHash(String hash, Integer pageNum);

	void saveKVToHash(String hash, Object key, Object value);

	void saveMapToHash(String hash, Map map);

	Map getMapByHash(String hash);

	Object getValueByHashKey(String hash, Object key);

	void incrementByHashKey(String hash, Object key, int increment);

	void deleteByHashKey(String hash, Object key);

	<T> List<T> getListByValue(String key);

	<T> void saveListToValue(String key, List<T> list);

	<T> Map<String, T> getMapByValue(String key);

	<T> void saveMapToValue(String key, Map<String, T> map);

	<T> T getObjectByValue(String key, Class t);

	void incrementByKey(String key, int increment);

	void saveObjectToValue(String key, Object object);

	void saveValueToSet(String key, Object value);

	int countBySet(String key);

	void deleteValueBySet(String key, Object value);

	boolean hasValueInSet(String key, Object value);

	void deleteCacheByKey(String key);

	boolean hasKey(String key);

	void expire(String key, long time);
    void incrementByZSet(String key, Object value, int increment);
    List<Object> getTopByZSet(String key, int limit);
    String getStringByKey(String key);
    void saveStringWithExpireTime(String key, String value, long timeout,java.util.concurrent.TimeUnit timeUnit);
    // 互斥锁（防击穿）
    boolean tryLock(String key, long expireSeconds);
    void releaseLock(String key);

    // 存空值标记（防穿透）
    void saveNullValue(String key, long timeout, TimeUnit unit);
    boolean isNullValue(String key);

    // 带随机过期时间的缓存写入（防雪崩）
    <T> void saveListToValueWithRandomExpire(String key, List<T> list, long baseSeconds, long randomRange);
    void lPushToList(String key, String value);
    List<String> lRangeList(String key, int count);
    void lPushWithLimit(String key, String value, int limit, long timeout, TimeUnit unit);

}
