package com.morningtech.eth.server.util;

import java.util.concurrent.TimeUnit;

/**
* @Description: 项目基本工具类
* @author xuchunlin
* @date 2017/11/28 14:27
* @version V1.0
*/
public class Utils {

	/**
	 * 给定时间与当前时间比较，是否超时
	 * @param preTime
	 * @param duration
	 * @param timeUnit
	 * @return
	 */
	public static boolean timeoutNowTime(Long preTime, int duration, TimeUnit timeUnit){
		return timeout(preTime, System.currentTimeMillis(), duration, timeUnit);
	}

	/**
	 * 当前时间是否超时
	 * 两个时间比较，是否超时
	 * @param preTime 前面时间
	 * @param nextTime 最新时间
	 * @param duration 超时时间
	 * @param timeUnit 超时单位
	 * @return
	 */
	public static boolean timeout(Long preTime,Long nextTime, int duration, TimeUnit timeUnit){
		if(preTime==null || nextTime==null){
			return true;
		}
		long pretime=preTime.longValue()/1000;
		long nexttime=nextTime/1000;
		long outtime=timeUnit.toSeconds(duration);

		if( nexttime > (pretime+outtime)){
			return true;
		}
		return false;
	}

}
