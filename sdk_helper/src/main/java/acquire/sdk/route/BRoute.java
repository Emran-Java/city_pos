package acquire.sdk.route;

import android.util.ArrayMap;

import com.newland.nsdk.core.api.common.ModuleType;
import com.newland.nsdk.core.api.common.exception.NSDKException;
import com.newland.nsdk.core.api.internal.routemanager.NetWorkType;
import com.newland.nsdk.core.api.internal.routemanager.RouteInfo;
import com.newland.nsdk.core.api.internal.routemanager.RouteManager;
import com.newland.nsdk.core.internal.NSDKModuleManagerImpl;

import java.util.List;
import java.util.Map;

import acquire.base.utils.LoggerUtils;

/**
 * Set system route list.
 *
 * @author Janson
 * @date 2023/5/9 9:24
 * @since 3.7
 */
public class BRoute {
    private final RouteManager routeManager;

    public BRoute() {
        routeManager = (RouteManager) NSDKModuleManagerImpl.getInstance().getModule(ModuleType.ROUTE_MANAGER);
    }

    /**
     * The data sent to this IP will use mobile network.
     */
    public void setMobile(String ip){
        try {
            routeManager.addRoute(ip, NetWorkType.NET_WORK_MOBILE);
            routeManager.enableMultiPath();
        } catch(NSDKException e) {
            LoggerUtils.e("BRoute setMobile failed!",e);
        }
    }
    /**
     * The data sent to this IP will use wifi.
     */
    public void setWifi(String ip){
        try {
            routeManager.addRoute(ip, NetWorkType.NET_WORK_WIFI);
            routeManager.enableMultiPath();
        } catch(NSDKException e) {
            LoggerUtils.e("BRoute setWifi["+ip+"]failed!",e);
        }
    }

    /**
     * Remove the IP {@link #setMobile(String)}configuration
     */
    public void removeMobile(String ip){
        try {
            routeManager.removeRoute(ip, NetWorkType.NET_WORK_MOBILE);
            routeManager.enableMultiPath();
        } catch(NSDKException e) {
            LoggerUtils.e("BRoute removeMobile["+ip+"]failed!",e);
        }
    }
    /**
     * Remove the IP {@link #setWifi(String)}configuration
     */
    public void removeWifi(String ip){
        try {
            routeManager.removeRoute(ip, NetWorkType.NET_WORK_WIFI);
            routeManager.enableMultiPath();
        } catch(NSDKException e) {
            LoggerUtils.e("BRoute removeWifi["+ip+"]failed!",e);
        }
    }

    /**
     * remove all IP route configuration
     */
    public void removeAll(){
        try {
            routeManager.removeAllRoute();
        } catch(NSDKException e) {
            LoggerUtils.e("BRoute removeAll failed!",e);
        }
    }

    /**
     * get all IP route information
     */
    public Map<String,Integer> getRouteList(){
        try {
            List<RouteInfo> routeInfos= routeManager.getRouteList();
            Map<String,Integer> map = new ArrayMap<>();
            for (RouteInfo routeInfo : routeInfos) {
                map.put(routeInfo.getAddress(),routeInfo.getNetworkType());
            }
            return map;
        } catch(NSDKException e) {
            LoggerUtils.e("BRoute getRouteList failed!",e);
            return null;
        }
    }
}
